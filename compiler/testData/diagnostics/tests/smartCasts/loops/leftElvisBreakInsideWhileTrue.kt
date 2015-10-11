public fun foo(x: String?, y: String?): Int {
    while (true) {
        val z = (if (y == null) break else x) ?: <!DEBUG_INFO_SMARTCAST!>y<!>
        // z is not null in both branches
        z.size
        // y is not null in both branches
        <!DEBUG_INFO_SMARTCAST!>y<!>.size
    }
    // y is null because of the break
    return y<!UNSAFE_CALL!>.<!>size
}