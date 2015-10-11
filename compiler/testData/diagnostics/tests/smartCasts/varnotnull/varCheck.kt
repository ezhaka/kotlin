fun get(): String? {
    return ""
}

fun foo(): Int {
    var c: String? = get()
    c!!.size
    return <!DEBUG_INFO_SMARTCAST!>c<!>.size // Previous line should make !! unnecessary here.
}