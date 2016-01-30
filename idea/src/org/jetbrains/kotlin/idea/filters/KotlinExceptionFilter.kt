/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.idea.filters

import com.intellij.execution.filters.ExceptionFilter
import com.intellij.execution.filters.Filter
import com.intellij.execution.filters.HyperlinkInfo
import com.intellij.execution.filters.OpenFileHyperlinkInfo
import com.intellij.openapi.compiler.CompilerPaths
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.codegen.inline.FileMapping
import org.jetbrains.kotlin.codegen.inline.InlineCodegenUtil
import org.jetbrains.kotlin.codegen.inline.SMAPParser
import org.jetbrains.kotlin.idea.refactoring.getLineCount
import org.jetbrains.kotlin.idea.refactoring.toPsiFile
import org.jetbrains.kotlin.idea.util.DebuggerUtils
import org.jetbrains.kotlin.idea.util.ProjectRootsUtil
import org.jetbrains.kotlin.load.kotlin.JvmVirtualFileFinder
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.tail
import org.jetbrains.kotlin.resolve.jvm.JvmClassName
import org.jetbrains.kotlin.utils.addToStdlib.check
import org.jetbrains.org.objectweb.asm.ClassReader
import org.jetbrains.org.objectweb.asm.ClassVisitor
import java.io.File
import java.util.regex.Pattern

class KotlinExceptionFilter(private val searchScope: GlobalSearchScope) : Filter {
    private val exceptionFilter = ExceptionFilter(searchScope)

    private fun createHyperlinkInfo(line: String): HyperlinkInfo? {
        val project = searchScope.project ?: return null

        val element = parseStackTraceLine(line) ?: return null

        // All true classes should be handled correctly in the default ExceptionFilter. Special cases:
        // - static facades;
        // - package facades / package parts (generated by pre-M13 compiled);
        // - local classes (and closures) in top-level function and property declarations.
        val fileName = element.fileName
        // fullyQualifiedName is of format "package.Class$Inner"
        val fullyQualifiedName = element.className
        val lineNumber = element.lineNumber - 1

        val internalName = fullyQualifiedName.replace('.', '/')
        val jvmClassName = JvmClassName.byInternalName(internalName)

        val file = DebuggerUtils.findSourceFileForClass(project, searchScope, jvmClassName, fileName) ?: return null

        val virtualFile = file.virtualFile ?: return null

        return virtualFileForInlineCall(jvmClassName, virtualFile, lineNumber + 1, project) ?:
                                OpenFileHyperlinkInfo(project, virtualFile, lineNumber)
    }

    private fun virtualFileForInlineCall(jvmName: JvmClassName, file: VirtualFile, lineNumber: Int, project: Project): OpenFileHyperlinkInfo? {
        val fqNameWithInners = jvmName.fqNameForClassNameWithoutDollars.tail(jvmName.packageFqName)

        if (ProjectRootsUtil.isLibrarySourceFile(project, file)) {
            val classId = ClassId(jvmName.packageFqName, Name.identifier(fqNameWithInners.asString()))

            val fileFinder = JvmVirtualFileFinder.SERVICE.getInstance(project)
            val classFile = fileFinder.findVirtualFileWithHeader(classId) ?: return null
            return readDebugInfoForInlineFun(classFile.contentsToByteArray(), lineNumber, project)
        }

        if (!ProjectRootsUtil.isProjectSourceFile(project, file)) return null

        val linesInFile = file.toPsiFile(project)?.getLineCount() ?: return null
        if (lineNumber <= linesInFile) return null


        val module = ProjectFileIndex.SERVICE.getInstance(project).getModuleForFile(file)
        val outputDir = CompilerPaths.getModuleOutputDirectory(module, /*forTests = */ false) ?: return null

        val className = fqNameWithInners.asString().replace('.', '$')
        val classByByDirectory = findClassFileByPath(jvmName.packageFqName.asString(), className, outputDir) ?: return null

        return readDebugInfoForInlineFun(classByByDirectory.readBytes(), lineNumber, project)
    }

    private fun findClassFileByPath(packageName: String, className: String, outputDir: VirtualFile): File? {
        val outDirFile = File(outputDir.path).check { it.exists() } ?: return null

        val parentDirectory = File(outDirFile, packageName.replace(".", File.separator))
        if (!parentDirectory.exists()) return null

        val classFile = File(parentDirectory, className + ".class")
        if (classFile.exists()) {
            return classFile
        }

        return null
    }

    private fun readDebugInfoForInlineFun(bytes: ByteArray, line: Int, project: Project): OpenFileHyperlinkInfo? {
        val debugInfo = readDebugInfo(bytes) ?: return null

        val mappings = SMAPParser.parse(debugInfo)

        val mappingInfo = mappings.fileMappings.firstOrNull {
            it.getIntervalIfContains(line) != null
        } ?: return null

        val newJvmName = JvmClassName.byInternalName(mappingInfo.path)
        val newSourceFile = DebuggerUtils.findSourceFileForClass(project, searchScope, newJvmName, mappingInfo.name) ?: return null
        return OpenFileHyperlinkInfo(project, newSourceFile.virtualFile, mappingInfo.getIntervalIfContains(line)!!.map(line) - 1)
    }

    private fun readDebugInfo(bytes: ByteArray): String? {
        val cr = ClassReader(bytes);
        var debugInfo: String? = null
        cr.accept(object : ClassVisitor(InlineCodegenUtil.API) {
            override fun visitSource(source: String?, debug: String?) {
                debugInfo = debug
            }
        }, ClassReader.SKIP_FRAMES and ClassReader.SKIP_CODE)
        return debugInfo
    }

    private fun FileMapping.getIntervalIfContains(destLine: Int) = lineMappings.firstOrNull { it.contains(destLine) }

    private fun patchResult(result: Filter.Result, line: String): Filter.Result {
        val newHyperlinkInfo = createHyperlinkInfo(line) ?: return result

        return Filter.Result(result.resultItems.map {
                Filter.ResultItem(it.getHighlightStartOffset(), it.getHighlightEndOffset(), newHyperlinkInfo, it.getHighlightAttributes())
        })

    }

    override fun applyFilter(line: String, entireLength: Int): Filter.Result? {
        val result = exceptionFilter.applyFilter(line, entireLength)
        return if (result == null) null else patchResult(result, line)
    }

    companion object {

        // Matches strings like "\tat test.TestPackage$foo$f$1.invoke(a.kt:3)\n"
        //                   or "\tBreakpoint reached at test.TestPackage$foo$f$1.invoke(a.kt:3)\n"
        private val STACK_TRACE_ELEMENT_PATTERN = Pattern.compile("^[\\w|\\s]*at\\s+(.+)\\.(.+)\\((.+):(\\d+)\\)\\s*$")

        private fun parseStackTraceLine(line: String): StackTraceElement? {
            val matcher = STACK_TRACE_ELEMENT_PATTERN.matcher(line)
            if (matcher.matches()) {
                val declaringClass = matcher.group(1)
                val methodName = matcher.group(2)
                val fileName = matcher.group(3)
                val lineNumber = matcher.group(4)
                //noinspection ConstantConditions
                return StackTraceElement(declaringClass, methodName, fileName, Integer.parseInt(lineNumber))
            }
            return null
        }
    }
}
