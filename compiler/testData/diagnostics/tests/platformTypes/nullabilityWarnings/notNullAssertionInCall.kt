// !DIAGNOSTICS: -UNUSED_PARAMETER

// FILE: p/J.java
package p;

import org.jetbrains.annotations.*;

public class J {
    @NotNull
    public static J staticNN;
}

// FILE: k.kt

import p.*

fun test() {
    // @NotNull platform type
    val platformNN = J.staticNN

    foo(platformNN<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>)
//    foo(platformNN ?: "")
}

fun foo(a: Any) {}