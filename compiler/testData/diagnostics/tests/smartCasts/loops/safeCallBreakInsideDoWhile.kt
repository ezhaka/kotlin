fun foo(x: String): String? = x

fun calc(x: String?, y: String?): Int {
    do {
        // Smart cast because of x!! in receiver
        foo(x!!)?.subSequence(0, if (<!DEBUG_INFO_SMARTCAST!>x<!>.size > 0) 5 else break)
        y!!.size
        // x is not null in condition but we do not see it yet
    } while (x<!UNSAFE_CALL!>.<!>size > 0)
    // y is nullable because of break
    y<!UNSAFE_CALL!>.<!>size
    // x is not null, at least in theory
    return <!DEBUG_INFO_SMARTCAST!>x<!>.size
}
