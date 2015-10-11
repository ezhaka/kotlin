public fun foo(p: String?, y: String?): Int {
    do {
        // After this !!, y. should be smartcasted in loop as well as outside
        y!!.size
        if (p == null) break
        <!DEBUG_INFO_SMARTCAST!>y<!>.size
    } while (true)
    return <!DEBUG_INFO_SMARTCAST!>y<!>.size
}