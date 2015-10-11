// !DIAGNOSTICS: -UNUSED_EXPRESSION,-UNUSED_VARIABLE

fun <T : CharSequence?> T.bar1() {}
fun CharSequence?.bar2() {}

fun <T : CharSequence> T.bar3() {}

fun <T, R> T.let(f: (T) -> R): R = f(this)

fun <T : String?> foo(x: T) {
    x<!UNSAFE_CALL!>.<!>size
    x?.size

    if (1 == 1) {
        x!!.size
    }


    x.bar1()
    x.bar2()

    x?.bar1()
    x?.bar2()

    x.<!TYPE_INFERENCE_UPPER_BOUND_VIOLATED!>bar3<!>()

    x?.let { it<!UNSAFE_CALL!>.<!>size }
}
