public fun foo(p: String?, y: String?): Int {
    do {
        // After the check, smart cast should work
        if (y == null) break
        <!DEBUG_INFO_SMARTCAST!>y<!>.size
        p!!.size
    } while (true)
    return y?.size ?: -1
}