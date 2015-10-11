fun x(): Boolean { return true }

public fun foo(p: String?, r: String?, q: String?): Int {
    outer@ while(true) {
        q!!.size
        do {
            if (x()) continue@outer
            do {
                p!!.size
            } while (!x())
        } while (r == null)
        if (!x()) break
    }
    // Smart cast is possible only for q
    <!DEBUG_INFO_SMARTCAST!>q<!>.size
    // But not possible for the others
    r<!UNSAFE_CALL!>.<!>size
    return p<!UNSAFE_CALL!>.<!>size
}