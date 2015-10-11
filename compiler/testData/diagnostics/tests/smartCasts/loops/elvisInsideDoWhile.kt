public fun foo(x: String?): Int {
    do {
        // After the check, smart cast should work
        x ?: x!!.size
        // x is not null in both branches
        if (<!DEBUG_INFO_SMARTCAST!>x<!>.size == 0) break
    } while (true)
    return <!DEBUG_INFO_SMARTCAST!>x<!>.size
}