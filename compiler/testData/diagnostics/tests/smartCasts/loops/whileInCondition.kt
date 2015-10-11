fun foo(s: String?): Int {
    while (s!!.size > 0) {
        <!DEBUG_INFO_SMARTCAST!>s<!>.size
    }
    return <!DEBUG_INFO_SMARTCAST!>s<!>.size
}