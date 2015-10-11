fun foo() {
    var v: String? = null
    v<!UNSAFE_CALL!>.<!>size
    v = "abc"
    <!DEBUG_INFO_SMARTCAST!>v<!>.size
    v = null
    v<!UNSAFE_CALL!>.<!>size
    v = "abc"
    <!DEBUG_INFO_SMARTCAST!>v<!>.size
}