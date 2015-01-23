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

package kotlin.jvm

object IntDefaultObjectImpl : IntDefaultObject, Parsable<Int> {
    override fun parse(str: String) = java.lang.Integer.parseInt(str)
}

public val IntDefaultObject.MIN_VALUE: Int get() = java.lang.Integer.MIN_VALUE
public val IntDefaultObject.MAX_VALUE: Int get() = java.lang.Integer.MAX_VALUE

object DoubleDefaultObjectImpl : DoubleDefaultObject, Parsable<Double> {
    val POSITIVE_INFINITY : Double = java.lang.Double.POSITIVE_INFINITY
    val NEGATIVE_INFINITY : Double = java.lang.Double.NEGATIVE_INFINITY
    val NaN : Double = java.lang.Double.NaN

    override fun parse(str: String) = java.lang.Double.parseDouble(str)
}

public val DoubleDefaultObject.MIN_VALUE: Double get() = java.lang.Double.MIN_VALUE
public val DoubleDefaultObject.MAX_VALUE: Double get() = java.lang.Double.MAX_VALUE