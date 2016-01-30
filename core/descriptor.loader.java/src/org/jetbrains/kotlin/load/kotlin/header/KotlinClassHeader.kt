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

package org.jetbrains.kotlin.load.kotlin.header

import org.jetbrains.kotlin.load.java.JvmBytecodeBinaryVersion
import org.jetbrains.kotlin.load.kotlin.JvmMetadataVersion

class KotlinClassHeader(
        val kind: KotlinClassHeader.Kind,
        val metadataVersion: JvmMetadataVersion,
        val bytecodeVersion: JvmBytecodeBinaryVersion,
        val data: Array<String>?,
        val strings: Array<String>?,
        val multifileClassName: String?
) {
    // See kotlin.Metadata
    enum class Kind(val id: Int) {
        UNKNOWN(0),
        CLASS(1),
        FILE_FACADE(2),
        SYNTHETIC_CLASS(3),
        MULTIFILE_CLASS(4),
        MULTIFILE_CLASS_PART(5);

        companion object {
            private val entryById = values().associateBy(Kind::id)

            @JvmStatic
            fun getById(id: Int) = entryById[id] ?: UNKNOWN
        }
    }

    override fun toString() = "$kind version=$metadataVersion"
}
