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

package org.jetbrains.kotlin.serialization

import org.jetbrains.kotlin.jvm.compiler.LoadDescriptorUtil
import org.jetbrains.kotlin.test.TestCaseWithTmpdir
import java.io.File
import org.jetbrains.kotlin.test.ConfigurationKind
import java.net.URLClassLoader
import org.jetbrains.kotlin.codegen.forTestCompile.ForTestCompileRuntime
import org.jetbrains.kotlin.test.JetTestUtils
import org.jetbrains.kotlin.cli.jvm.compiler.CliLightClassGenerationSupport
import org.jetbrains.kotlin.di.InjectorForJavaDescriptorResolverUtil
import org.jetbrains.kotlin.cli.jvm.compiler.JetCoreEnvironment
import org.jetbrains.kotlin.test.TestJdkKind
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.test.util.RecursiveDescriptorComparator
import org.jetbrains.kotlin.test.InTextDirectivesUtils
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

public abstract class AbstractLocalClassProtoTest : TestCaseWithTmpdir() {
    protected fun doTest(filename: String) {
        val source = File(filename)
        LoadDescriptorUtil.compileKotlinToDirAndGetAnalysisResult(listOf(source), tmpdir, getTestRootDisposable(), ConfigurationKind.ALL)

        val classNameSuffix = InTextDirectivesUtils.findStringWithPrefixes(source.readText(), "// CLASS_NAME_SUFFIX: ")
                              ?: error("CLASS_NAME_SUFFIX directive not found in test data")

        val classLoader = URLClassLoader(array(tmpdir.toURI().toURL(), ForTestCompileRuntime.runtimeJarForTests().toURI().toURL()), null)

        val classFile = tmpdir.listFiles().singleOrNull { it.getPath().endsWith("$classNameSuffix.class") }
                        ?: error("Local class with suffix `$classNameSuffix` is not found in: ${tmpdir.listFiles().toList()}")
        val clazz = classLoader.loadClass(classFile.name.substringBeforeLast(".class"))
        assertHasAnnotationData(clazz)

        val environment = JetCoreEnvironment.createForTests(
                getTestRootDisposable(),
                JetTestUtils.compilerConfigurationForTests(ConfigurationKind.ALL, TestJdkKind.MOCK_JDK, tmpdir),
                EnvironmentConfigFiles.JVM_CONFIG_FILES
        )
        val trace = CliLightClassGenerationSupport.NoScopeRecordCliBindingTrace()
        val injector = InjectorForJavaDescriptorResolverUtil.create(environment.getProject(), trace, true)
        val components = injector.getDeserializationComponentsForJava().components

        val classDescriptor = components.classDeserializer.deserializeClass(clazz.classId, local = true)

        RecursiveDescriptorComparator.validateAndCompareDescriptorWithFile(
                classDescriptor,
                RecursiveDescriptorComparator.DONT_INCLUDE_METHODS_OF_OBJECT,
                JetTestUtils.replaceExtension(source, "txt")
        )
    }

    private val Class<*>.classId: ClassId
        get() {
            if (getEnclosingMethod() != null || getEnclosingConstructor() != null) return ClassId.topLevel(FqName(getName()))
            return getDeclaringClass()?.classId?.createNestedClassId(Name.identifier(getSimpleName()))
                   ?: ClassId.topLevel(FqName(getName()))
        }

    private fun assertHasAnnotationData(clazz: Class<*>) {
        [suppress("UNCHECKED_CAST")]
        val annotation =
                clazz.getAnnotation(clazz.getClassLoader().loadClass("kotlin.jvm.internal.KotlinSyntheticClass") as Class<Annotation>)
        assert(annotation != null) { "KotlinSyntheticClass annotation is not found for class $clazz" }

        val dataMethod = annotation.annotationType().getDeclaredMethod("data")
        val data = dataMethod(annotation)
        assert(data is Array<String>) { "'data' should be an array of strings: $data" }
        assert((data as Array<String>).isNotEmpty()) { "'data' should be non-empty" }
    }
}
