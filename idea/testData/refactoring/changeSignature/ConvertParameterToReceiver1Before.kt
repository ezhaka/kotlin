fun <caret>foo(x: X, s: String, k: Int): Boolean {
    return x.k + s.size - k > 0
}

class X(val k: Int)

fun test() {
    foo(X(0), "1", 2)
}