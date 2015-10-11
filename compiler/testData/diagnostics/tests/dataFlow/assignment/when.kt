fun test(a: Any?) {
    when (a) {
        is String -> {
            val s = a
            <!DEBUG_INFO_SMARTCAST!>s<!>.size
        }
        "" -> {
            val s = a
            <!DEBUG_INFO_SMARTCAST!>s<!>.hashCode()
        }
    }
}