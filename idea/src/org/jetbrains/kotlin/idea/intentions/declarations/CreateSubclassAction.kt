/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.idea.intentions.declarations

import com.intellij.codeInsight.CodeInsightUtil
import com.intellij.codeInsight.daemon.impl.quickfix.CreateClassKind
import com.intellij.codeInsight.generation.OverrideImplementUtil
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.codeInsight.intention.impl.CreateClassDialog
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.Result
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.ex.IdeDocumentHistory
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.quickfix.createFromUsage.callableBuilder.CallableBuilderConfiguration
import org.jetbrains.kotlin.idea.quickfix.createFromUsage.callableBuilder.CallablePlacement
import org.jetbrains.kotlin.idea.quickfix.createFromUsage.callableBuilder.PrimaryConstructorInfo
import org.jetbrains.kotlin.idea.quickfix.createFromUsage.callableBuilder.createBuilder
import org.jetbrains.kotlin.idea.quickfix.createFromUsage.createClass.ClassKind
import org.jetbrains.kotlin.idea.refactoring.getOrCreateKotlinFile
import org.jetbrains.kotlin.idea.util.application.executeCommand
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtTypeParameterList
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.isAbstract
import org.jetbrains.kotlin.psi.psiUtil.isPrivate
import java.util.*

class CreateSubclassAction3 : BaseIntentionAction() {
    override fun getFamilyName(): String = "Implement abstract class or interface"
    private val IMPL_SUFFIX = "Impl"
    private var myText = "Implement abstract class"

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        val ktClass = getClass(editor, file)
        if (ktClass == null || ktClass.isEnum() || ktClass.isSealed() || ktClass.containingFile.containingDirectory == null) {
            return false
        }
        if (!isSupportedLanguage(ktClass)) return false

        // todo: check if constructors are private
        // todo: check if caret (element) is not after lbrace
        // todo: another strange check...
        return true
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        val ktClass = getClass(editor, file)!!

        if (ktClass.isPrivate() && ktClass.containingClassOrObject != null) {
            createInnerClass(ktClass)
        }
        else {
            createTopLevelClass(ktClass)
        }
    }

    override fun getText(): String {
        // todo: try to eliminate persistance of myText
        return myText
    }

    private fun createTopLevelClass(ktClass: KtClass) {
        val dlg = chooseSubclassToCreate(ktClass) ?: return
        createSubclass(ktClass, dlg.targetDirectory, dlg.className)
    }

    private fun createSubclass(ktClass: KtClass, targetDirectory: PsiDirectory?, className: String): KtClass? {
        val project = ktClass.project
        var targetClass: KtClass? = null

        object : WriteCommandAction<Unit>(project, getTitle(ktClass), getTitle(ktClass)) {
            override fun run(result: Result<Unit>) {
                IdeDocumentHistory.getInstance(project).includeCurrentPlaceAsChangePlace()
                val oldTypeParametersList = ktClass.typeParameterList

                targetClass = createClass(targetDirectory, className)
                // todo: call start template
            }
        }.execute()

        if (targetClass == null) return null
        if (!ApplicationManager.getApplication().isUnitTestMode /*&& !ktClass.hasTypeParameters()*/) {
            val editor = positionCursorAtLBrace(project, targetClass) ?: return targetClass
            chooseAndImplement(ktClass, project, targetClass!!, editor)
        }
        return targetClass
    }

    private fun chooseAndImplement(ktClass: KtClass, project: Project, targetClass: KtClass, editor: Editor) {
        // todo: check for non-trivial constructor
        OverrideImplementUtil.chooseAndImplementMethods(project, editor, targetClass)
    }

    private fun positionCursorAtLBrace(project: Project, targetClass: KtClass?): Editor? {
//        return CodeInsightUtil.positionCursorAtLBrace(project, targetClass?.getContainingFile(), targetClass)
    }

    private fun createClass(targetDirectory: PsiDirectory?, className: String): KtClass {
        val file = getOrCreateKotlinFile(className, targetDirectory!!)

        // copy-pasted from CreateClassFromUsageFix.kt
        val classInfo = ClassInfo()
        val constructorInfo = PrimaryConstructorInfo(classInfo, expectedTypeInfo)
        val builder = CallableBuilderConfiguration(
                Collections.singletonList(constructorInfo), element as KtElement, file, editor, false, kind == ClassKind.PLAIN_CLASS || kind == ClassKind.INTERFACE
        ).createBuilder()
        builder.placement = CallablePlacement.NoReceiver(targetParent)
        project.executeCommand(text) { builder.run() }
        return file
    }

    private fun chooseSubclassToCreate(ktClass: KtClass): CreateClassDialog? {
        val sourceDir = ktClass.containingFile.containingDirectory
        val aPackage = JavaDirectoryService.getInstance().getPackage(sourceDir)
        val dialog: CreateClassDialog = object : CreateClassDialog(
                ktClass.project, getTitle(ktClass),
                ktClass.name!! + IMPL_SUFFIX,
                if (aPackage != null) aPackage.qualifiedName else "",
                CreateClassKind.CLASS, true, ModuleUtilCore.findModuleForPsiElement(ktClass)) {
            override fun getBaseDir(packageName: String): PsiDirectory? {
                return sourceDir
            }

            override fun reportBaseInTestSelectionInSource(): Boolean {
                return true
            }
        }

        if (!dialog.showAndGet() || dialog.targetDirectory == null) return null
        return dialog;
    }

    private fun getTitle(ktClass: KtClass): String {
        return when {
            ktClass.isInterface() -> "Implement interface"
            ktClass.isAbstract() -> "Implement abstract class"
            else -> "Create subclass"
        }
    }

    private fun createInnerClass(ktClass: KtClass) {
        object : WriteCommandAction<Unit>(ktClass.project, getTitle(ktClass), getTitle(ktClass)) {
            @Throws(Throwable::class)
            protected override fun run(result: Result<Unit>) {
                val containingClass = ktClass.containingClassOrObject

                // todo:
                // LOG.assertTrue(containingClass != null)

                val oldTypeParameterList = ktClass.typeParameterList
                val psiFactory = KtPsiFactory(ktClass)
                var classFromText = psiFactory.createClass(ktClass.name!! + IMPL_SUFFIX)
                classFromText = containingClass!!.addAfter(classFromText, ktClass) as KtClass
                startTemplate(oldTypeParameterList, ktClass.project, ktClass, classFromText, true)
            }
        }.execute()
    }

    private fun startTemplate(
            oldTypeParameterList: KtTypeParameterList?,
            project: Project,
            ktClass: KtClass,
            targetClass: KtClass,
            includeClassName: Boolean) {
        val psiFactory = KtPsiFactory(ktClass)
        val nameIdentifier = ktClass.nameIdentifier!!

        targetClass.getSuperTypeList()?.add(nameIdentifier)

        if (ktClass.typeParameterList != null || includeClassName) {

        }

    }

    private fun getClass(editor: Editor?, file: PsiFile?): KtClass? {
        val offset = editor?.caretModel?.offset ?: return null
        val element = file?.findElementAt(offset)
        val ktClass = PsiTreeUtil.getParentOfType(element, KtClass::class.java)
        return ktClass
    }

    fun isSupportedLanguage(aClass: KtClass?): Boolean {
        return aClass?.language == KotlinLanguage.INSTANCE
    }
}
