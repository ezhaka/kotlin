fun foo() {
    var v: String? = "xyz"
    // It is possible in principle to provide smart cast here
    v<!UNSAFE_CALL!>.<!>size
    v = null
    v<!UNSAFE_CALL!>.<!>size
}