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

package org.jetbrains.kotlin.idea.intentions

import com.intellij.openapi.editor.Editor
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode

class ExpandToNormalAnnotationIntention : SelfTargetingIntention<KtValueArgument>(KtValueArgument::class.java, "Add \"value=\" to annotation attribute") {
    override fun isApplicableTo(element: KtValueArgument, caretOffset: Int): Boolean {
        val annotationEntry = element.getParentOfType<KtAnnotationEntry>(true) ?: return false
        val arguments = annotationEntry.valueArgumentList?.arguments ?: return false
        return arguments.size == 1 && !arguments.first().isNamed()
    }

    override fun applyTo(element: KtValueArgument, editor: Editor?) {
        val annotationEntry = element.getParentOfType<KtAnnotationEntry>(true)!!
        val bindingContext = element.analyze(BodyResolveMode.PARTIAL)
        val resolvedCall = annotationEntry.getResolvedCall(bindingContext)
        resolvedCall.valueArgumentsByIndex
        val argumentList = annotationEntry.valueArgumentList!!
        val argument = argumentList.arguments.first()
        val replacementText = "${argument.name ?: "value"} = ${argument.getArgumentExpression()?.text}"
        val psiFactory = KtPsiFactory(element)
        argumentList.replace(psiFactory.createAnnotationEntry("@A($replacementText)").valueArgumentList!!)
    }
}
