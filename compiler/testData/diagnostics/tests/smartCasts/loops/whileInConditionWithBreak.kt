fun bar(): Boolean { return true }

fun foo(s: String?): Int {
    while (s!!.size > 0) {
        <!DEBUG_INFO_SMARTCAST!>s<!>.size
        if (bar()) break
    }
    return <!DEBUG_INFO_SMARTCAST!>s<!>.size
}