class MyClass(var p: String?)

fun bar(s: String): Int {
    return s.size
}

fun foo(m: MyClass): Int {
    m.p = "xyz"
    return bar(<!SMARTCAST_IMPOSSIBLE!>m.p<!>)
}