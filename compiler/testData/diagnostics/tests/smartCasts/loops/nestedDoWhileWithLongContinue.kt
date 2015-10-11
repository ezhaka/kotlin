fun x(): Boolean { return true }

public fun foo(p: String?, r: String?): Int {
    outer@ do {
        do {
            p!!.size
            if (!x()) continue@outer
        } while (r == null)  
    } while (!x())
    // Auto cast NOT possible due to long continue
    r<!UNSAFE_CALL!>.<!>size
    // Auto cast possible
    return <!DEBUG_INFO_SMARTCAST!>p<!>.size
}