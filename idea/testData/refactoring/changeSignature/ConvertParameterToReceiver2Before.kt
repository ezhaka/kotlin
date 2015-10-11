fun <caret>foo(s: String, x: X, k: Int): Boolean {
    return x.k + s.size - k > 0
}

class X(val k: Int)

fun test() {
    foo("1", X(0), 2)
}