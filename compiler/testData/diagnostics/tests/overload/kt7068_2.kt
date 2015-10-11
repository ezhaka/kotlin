// !DIAGNOSTICS: -UNUSED_PARAMETER

fun withLambda(block : Int.(String) -> Unit) {
}

fun withLambda(block : Int.(String, String) -> Unit) {
}

fun test() {
    withLambda { r ->
        r.size
    }

    withLambda { x, y ->
        x.size + y.size
    }
}