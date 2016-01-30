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

package org.jetbrains.kotlin.idea.highlighter.markers

import com.intellij.codeHighlighting.Pass
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.daemon.impl.LineMarkerNavigator
import com.intellij.codeInsight.daemon.impl.MarkerType
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.searches.ClassInheritorsSearch
import org.jetbrains.kotlin.asJava.LightClassUtil
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.idea.util.ProjectRootsUtil
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.isOverridable
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.Icon

class KotlinLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<PsiElement>? {
        // all Kotlin markers are added in slow marker pass
        return null
    }

    override fun collectSlowLineMarkers(elements: List<PsiElement>, result: MutableCollection<LineMarkerInfo<*>>) {
        if (elements.isEmpty()) return

        val first = elements.first()
        if (DumbService.getInstance(first.project).isDumb || !ProjectRootsUtil.isInProjectOrLibSource(first)) return

        val functions = HashSet<KtNamedFunction>()
        val properties = HashSet<KtProperty>()

        for (element in elements) {
            ProgressManager.checkCanceled()

            when (element) {
                is KtClass -> {
                    collectInheritedClassMarker(element, result)
                }
                is KtNamedFunction -> {
                    functions.add(element)
                    collectSuperDeclarationMarkers(element, result)
                }
                is KtProperty -> {
                    properties.add(element)
                    collectSuperDeclarationMarkers(element, result)
                }
            }
        }

        collectOverriddenFunctions(functions, result)
        collectOverriddenPropertyAccessors(properties, result)
    }
}

private val OVERRIDING_MARK: Icon = AllIcons.Gutter.OverridingMethod
private val IMPLEMENTING_MARK: Icon = AllIcons.Gutter.ImplementingMethod
private val OVERRIDDEN_MARK: Icon = AllIcons.Gutter.OverridenMethod
private val IMPLEMENTED_MARK: Icon = AllIcons.Gutter.ImplementedMethod

private val SUBCLASSED_CLASS = MarkerType(
        { getPsiClass(it)?.let { MarkerType.getSubclassedClassTooltip(it) } },
        object : LineMarkerNavigator() {
            override fun browse(e: MouseEvent?, element: PsiElement?) {
                getPsiClass(element)?.let { MarkerType.navigateToSubclassedClass(e, it) }
            }
        })

private val OVERRIDDEN_FUNCTION = MarkerType(
        { getPsiMethod(it)?.let { getOverriddenMethodTooltip(it) } },
        object : LineMarkerNavigator() {
            override fun browse(e: MouseEvent?, element: PsiElement?) {
                getPsiMethod(element)?.let { navigateToOverriddenMethod(e, it) }
            }
        })

private val OVERRIDDEN_PROPERTY = MarkerType(
        { it?.let { getOverriddenPropertyTooltip(it.parent as KtProperty) } },
        object : LineMarkerNavigator() {
            override fun browse(e: MouseEvent?, element: PsiElement?) {
                element?.let { navigateToPropertyOverriddenDeclarations(e, it.parent as KtProperty) }
            }
        })

private fun isImplementsAndNotOverrides(descriptor: CallableMemberDescriptor, overriddenMembers: Collection<CallableMemberDescriptor>): Boolean {
    return descriptor.modality != Modality.ABSTRACT && overriddenMembers.all { it.modality == Modality.ABSTRACT }
}

private fun collectSuperDeclarationMarkers(declaration: KtDeclaration, result: MutableCollection<LineMarkerInfo<*>>) {
    assert(declaration is KtNamedFunction || declaration is KtProperty)

    if (!declaration.hasModifier(KtTokens.OVERRIDE_KEYWORD)) return

    val resolveWithParents = resolveDeclarationWithParents(declaration)
    if (resolveWithParents.overriddenDescriptors.isEmpty()) return

    val implements = isImplementsAndNotOverrides(resolveWithParents.descriptor!!, resolveWithParents.overriddenDescriptors)

    // NOTE: Don't store descriptors in line markers because line markers are not deleted while editing other files and this can prevent
    // clearing the whole BindingTrace.
    val marker = LineMarkerInfo(
            declaration,
            declaration.textOffset,
            if (implements) IMPLEMENTING_MARK else OVERRIDING_MARK,
            Pass.UPDATE_OVERRIDEN_MARKERS,
            SuperDeclarationMarkerTooltip,
            SuperDeclarationMarkerNavigationHandler()
    )

    result.add(marker)
}

private fun collectInheritedClassMarker(element: KtClass, result: MutableCollection<LineMarkerInfo<*>>) {
    val isTrait = element.isInterface()
    if (!(isTrait || element.hasModifier(KtTokens.OPEN_KEYWORD) || element.hasModifier(KtTokens.ABSTRACT_KEYWORD))) {
        return
    }

    val lightClass = element.toLightClass() ?: return

    if (ClassInheritorsSearch.search(lightClass, false).findFirst() == null) return

    val anchor = element.nameIdentifier ?: element

    result.add(LineMarkerInfo(
            anchor,
            anchor.textOffset,
            if (isTrait) IMPLEMENTED_MARK else OVERRIDDEN_MARK,
            Pass.UPDATE_OVERRIDEN_MARKERS,
            SUBCLASSED_CLASS.tooltip,
            SUBCLASSED_CLASS.navigationHandler
    ))
}

private fun collectOverriddenPropertyAccessors(properties: Collection<KtProperty>, result: MutableCollection<LineMarkerInfo<*>>) {
    val mappingToJava = HashMap<PsiMethod, KtProperty>()
    for (property in properties) {
        if (property.isOverridable()) {
            val accessorsPsiMethods = LightClassUtil.getLightClassPropertyMethods(property)
            for (psiMethod in accessorsPsiMethods) {
                mappingToJava.put(psiMethod, property)
            }
        }
    }

    val classes = collectContainingClasses(mappingToJava.keys)

    for (property in getOverriddenDeclarations(mappingToJava, classes)) {
        ProgressManager.checkCanceled()

        val anchor = property.nameIdentifier ?: property

        result.add(LineMarkerInfo(
                anchor,
                anchor.textOffset,
                if (isImplemented(property)) IMPLEMENTED_MARK else OVERRIDDEN_MARK,
                Pass.UPDATE_OVERRIDEN_MARKERS,
                OVERRIDDEN_PROPERTY.tooltip,
                OVERRIDDEN_PROPERTY.navigationHandler,
                GutterIconRenderer.Alignment.RIGHT
        ))
    }
}

private fun collectOverriddenFunctions(functions: Collection<KtNamedFunction>, result: MutableCollection<LineMarkerInfo<*>>) {
    val mappingToJava = HashMap<PsiMethod, KtNamedFunction>()
    for (function in functions) {
        if (function.isOverridable()) {
            val method = LightClassUtil.getLightClassMethod(function)
            if (method != null) {
                mappingToJava.put(method, function)
            }
        }
    }

    val classes = collectContainingClasses(mappingToJava.keys)

    for (function in getOverriddenDeclarations(mappingToJava, classes)) {
        ProgressManager.checkCanceled()

        val anchor = function.nameIdentifier ?: function

        result.add(LineMarkerInfo(
                anchor,
                anchor.textOffset,
                if (isImplemented(function)) IMPLEMENTED_MARK else OVERRIDDEN_MARK,
                Pass.UPDATE_OVERRIDEN_MARKERS, OVERRIDDEN_FUNCTION.tooltip,
                OVERRIDDEN_FUNCTION.navigationHandler,
                GutterIconRenderer.Alignment.RIGHT
        ))
    }
}
