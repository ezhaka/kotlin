fun x(): Boolean { return true }

public fun foo(pp: String?, rr: String?): Int {
    var p = pp
    var r = rr
    do {
        do {
            p!!.size
        } while (r == null)  
    } while (!x())
    // Auto cast possible
    <!DEBUG_INFO_SMARTCAST!>r<!>.size
    // Auto cast possible
    return <!DEBUG_INFO_SMARTCAST!>p<!>.size
}