fun get(): Any {
    return ""
}

fun foo(): Int {
    var c: Any = get()
    (c as String).size
    return <!DEBUG_INFO_SMARTCAST!>c<!>.size // Previous line should make as unnecessary here.
}