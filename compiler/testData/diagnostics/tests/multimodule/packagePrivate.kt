// MODULE: m1
// FILE: a.kt

package p

private val a = 1

// FILE: b.kt

package p

val b = <!INVISIBLE_FILE_MEMBER!>a<!> // same package, same module

// MODULE: m2(m1)
// FILE: c.kt

package p

val c = <!INVISIBLE_MEMBER!>a<!> // same package, another module
