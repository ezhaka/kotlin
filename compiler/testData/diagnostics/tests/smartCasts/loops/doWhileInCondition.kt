fun foo(s: String?): Int {
    do {
    } while (s!!.size > 0)
    return <!DEBUG_INFO_SMARTCAST!>s<!>.size
}