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

package org.jetbrains.kotlin.idea.references

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.util.IncorrectOperationException
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.psi.KtDestructuringDeclaration
import org.jetbrains.kotlin.resolve.BindingContext

class KtDestructuringDeclarationReference(element: KtDestructuringDeclaration) : KtMultiReference<KtDestructuringDeclaration>(element) {
    override fun getTargetDescriptors(context: BindingContext): Collection<DeclarationDescriptor> {
        return expression.entries.mapNotNull { entry ->
            context.get(BindingContext.COMPONENT_RESOLVED_CALL, entry)?.candidateDescriptor
        }
    }

    override fun getRangeInElement(): TextRange? {
        val start = expression.lPar
        val end = expression.rPar
        if (start == null || end == null) return TextRange.EMPTY_RANGE
        return TextRange(start.startOffsetInParent, end.startOffsetInParent)
    }

    override fun canRename(): Boolean {
        val bindingContext = expression.analyze() //TODO: should it use full body resolve?
        return resolveToDescriptors(bindingContext).all { it is CallableMemberDescriptor && it.kind == CallableMemberDescriptor.Kind.SYNTHESIZED}
    }

    override fun handleElementRename(newElementName: String?): PsiElement? {
        if (canRename()) return expression
        throw IncorrectOperationException()
    }
}
