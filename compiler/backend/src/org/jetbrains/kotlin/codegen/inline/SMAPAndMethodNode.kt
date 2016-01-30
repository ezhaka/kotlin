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

package org.jetbrains.kotlin.codegen.inline

import org.jetbrains.kotlin.codegen.optimization.common.InsnSequence
import org.jetbrains.org.objectweb.asm.tree.LineNumberNode
import org.jetbrains.org.objectweb.asm.tree.MethodNode
import java.util.*

//TODO comment
class SMAPAndMethodNode(val node: MethodNode, val classSMAP: SMAP) {
    private val lineNumbers =
        InsnSequence(node.instructions.first, null).filterIsInstance<LineNumberNode>().map { node ->
            val index = classSMAP.intervals.binarySearch(RangeMapping(node.line, node.line, 1), Comparator {
                value, key ->
                if (key.dest in value) 0 else RangeMapping.Comparator.compare(value, key)
            })
            if (index < 0) {
                error("Unmapped label in inlined function $node ${node.line}")
            }
            LabelAndMapping(node, classSMAP.intervals[index])
        }.toList()

    val ranges = lineNumbers.asSequence().map { it.mapper }.distinct().toList()
}

class LabelAndMapping(val lineNumberNode: LineNumberNode, val mapper: RangeMapping)
