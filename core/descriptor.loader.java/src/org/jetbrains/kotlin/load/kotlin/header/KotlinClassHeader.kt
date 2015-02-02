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

import org.jetbrains.kotlin.load.java.JvmAnnotationNames.KotlinSyntheticClass
import org.jetbrains.kotlin.load.java.AbiVersionUtil

public class KotlinClassHeader(
        public val kind: KotlinClassHeader.Kind,
        public val version: Int,
        public val annotationData: Array<String>?,
        public val syntheticClassKind: KotlinSyntheticClass.Kind?
) {
    public val isCompatibleAbiVersion: Boolean get() = AbiVersionUtil.isAbiVersionCompatible(version);

    {
        if (isCompatibleAbiVersion) {
            assert(annotationData != null || kind == Kind.SYNTHETIC_CLASS) {
                "Annotation data can be null only for SYNTHETIC_CLASS (kind=$kind, syntheticClassKind=$syntheticClassKind)"
            }
            assert((syntheticClassKind == null) == (kind != Kind.SYNTHETIC_CLASS)) {
                "Synthetic class kind should be present for SYNTHETIC_CLASS (kind=$kind, syntheticClassKind=$syntheticClassKind)"
            }
        }
    }

    public enum class Kind {
        CLASS
        PACKAGE_FACADE
        SYNTHETIC_CLASS
    }

    override fun toString() = "$kind " + (if (syntheticClassKind != null) "$syntheticClassKind " else " ") + "version=$version"
}

public fun KotlinClassHeader.isCompatibleClassKind(): Boolean = isCompatibleAbiVersion && kind == KotlinClassHeader.Kind.CLASS
public fun KotlinClassHeader.isCompatiblePackageFacadeKind(): Boolean = isCompatibleAbiVersion && kind == KotlinClassHeader.Kind.PACKAGE_FACADE
public fun KotlinClassHeader.isCompatibleSyntheticClassKind(): Boolean = isCompatibleAbiVersion && kind == KotlinClassHeader.Kind.SYNTHETIC_CLASS
