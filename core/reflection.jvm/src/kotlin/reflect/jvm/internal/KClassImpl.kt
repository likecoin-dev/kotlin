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

package kotlin.reflect.jvm.internal

import kotlin.reflect.*
import kotlin.jvm.internal.KotlinClass
import kotlin.jvm.internal.KotlinSyntheticClass
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.serialization.deserialization.findClassAcrossModuleDependencies
import org.jetbrains.kotlin.load.java.structure.reflect.classId

enum class KClassOrigin {
    BUILT_IN
    KOTLIN
    FOREIGN
}

private val KOTLIN_CLASS_ANNOTATION_CLASS = javaClass<KotlinClass>()
private val KOTLIN_SYNTHETIC_CLASS_ANNOTATION_CLASS = javaClass<KotlinSyntheticClass>()

class KClassImpl<T>(val jClass: Class<T>, isKnownToBeKotlin: Boolean = false) : KClass<T> {
    // Don't use kotlin.properties.Delegates here because it's a Kotlin class which will invoke KClassImpl() in <clinit>,
    // resulting in infinite recursion
    val descriptor by Delegates.lazySoft {(): ClassDescriptor ->
        val moduleData = jClass.getOrCreateModule()
        val outerClass = jClass.getDeclaringClass() as Class<Any>?

        val found = moduleData.module.findClassAcrossModuleDependencies(jClass.classId)
        if (found != null) return@lazySoft found

        // TODO: do something if class is not found
        error("Class not found: $jClass")
    }

    // TODO: write metadata to local classes
    private val origin: KClassOrigin =
            if (isKnownToBeKotlin ||
                jClass.isAnnotationPresent(KOTLIN_CLASS_ANNOTATION_CLASS) ||
                jClass.isAnnotationPresent(KOTLIN_SYNTHETIC_CLASS_ANNOTATION_CLASS)
            ) {
                KClassOrigin.KOTLIN
            }
            else {
                KClassOrigin.FOREIGN
                // TODO: built-in classes
            }

    fun memberProperty(name: String): KMemberProperty<T, *> =
            if (origin === KClassOrigin.KOTLIN) {
                KMemberPropertyImpl<T, Any>(name, this)
            }
            else {
                KForeignMemberProperty<T, Any>(name, this)
            }

    fun mutableMemberProperty(name: String): KMutableMemberProperty<T, *> =
            if (origin === KClassOrigin.KOTLIN) {
                KMutableMemberPropertyImpl<T, Any>(name, this)
            }
            else {
                KMutableForeignMemberProperty<T, Any>(name, this)
            }

    override fun equals(other: Any?): Boolean =
            other is KClassImpl<*> && jClass == other.jClass

    override fun hashCode(): Int =
            jClass.hashCode()

    override fun toString(): String =
            jClass.toString()
}
