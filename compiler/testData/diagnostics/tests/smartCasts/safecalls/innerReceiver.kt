fun foo(x: String): String? = x

fun calc(x: String?, y: String?): Int {
    // Smart cast because of y!! in receiver
    x?.subSequence(y!!.subSequence(0, 1).size, <!DEBUG_INFO_SMARTCAST!>y<!>.size)
    // No smart cast possible
    return y<!UNSAFE_CALL!>.<!>size
}
