open class X: A() {
    fun foo(s: String): Int {
        return super.foo(s) + 1
    }
}

open class Y: B() {
    fun foo(s: String): Int {
        return s.size * 2
    }
}

open class Z: X() {
    fun foo(s: String): Int {
        return s.size
    }
}

fun test() {
    A().foo("")
    B().foo("")
    X().foo("")
    Y().foo("")
    Z().foo("")
}