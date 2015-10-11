public fun foo(x: String?, y: String?): Int {
    do {
        // After the check, smart cast should work
        if (x != null) {
            if (x == "abc") break
            y!!.size
        } else {
            y!!.size
        }
        // y!! in both branches
        <!DEBUG_INFO_SMARTCAST!>y<!>.size
    } while (true)
    // break is possible before so !! is necessary
    return y!!.size
}