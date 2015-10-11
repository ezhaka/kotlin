fun test(a: Any?, flag: Boolean, x: Any?) {
    if (a !is String) return
    <!DEBUG_INFO_SMARTCAST!>a<!>.size

    val b: Any?

    if (flag) {
        b = a
        <!DEBUG_INFO_SMARTCAST!>b<!>.size
    }
    else {
        b = x
        b.<!UNRESOLVED_REFERENCE!>length<!>()
    }
}