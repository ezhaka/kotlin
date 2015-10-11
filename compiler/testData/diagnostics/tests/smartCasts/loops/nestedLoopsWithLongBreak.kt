fun x(p: String): Boolean { return p == "abc" }

public fun foo(p: String?, r: String?, q: String?): Int {
    while(true) {
        q!!.size
        loop@ do {
            while(true) {
                p!!.size
                if (x(<!DEBUG_INFO_SMARTCAST!>p<!>)) break@loop
                if (x(<!DEBUG_INFO_SMARTCAST!>q<!>)) break
            }
        } while (r == null)
        if (!x(<!DEBUG_INFO_SMARTCAST!>p<!>)) break
    }
    // Long break allows r == null
    r<!UNSAFE_CALL!>.<!>size
    // Smart cast is possible
    <!DEBUG_INFO_SMARTCAST!>q<!>.size
    return <!DEBUG_INFO_SMARTCAST!>p<!>.size
}