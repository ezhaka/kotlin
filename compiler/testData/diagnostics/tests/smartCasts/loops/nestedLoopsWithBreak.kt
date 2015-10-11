fun x(): Boolean { return true }

public fun foo(p: String?, r: String?, q: String?): Int {
    while(true) {
        q!!.size
        do {
            while(true) {
                p!!.size
                if (x()) break
            }
        } while (r == null)
        if (!x()) break
    }
    // Smart cast is possible everywhere
    <!DEBUG_INFO_SMARTCAST!>r<!>.size
    <!DEBUG_INFO_SMARTCAST!>q<!>.size
    return <!DEBUG_INFO_SMARTCAST!>p<!>.size
}