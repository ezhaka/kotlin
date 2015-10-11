public fun test(o: String?): Boolean {
    return when {
        // Data flow info should propagate from o == null to o.size
        o == null, <!DEBUG_INFO_SMARTCAST!>o<!>.size == 0 -> false
        else -> true
    }
}