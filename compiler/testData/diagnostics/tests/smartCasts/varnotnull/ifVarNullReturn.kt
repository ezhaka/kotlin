public fun fooNotNull(s: String) {
    System.out.println("Length of $s is ${s.size}")
}

public fun foo() {
    var s: String? = "not null"
    if (s == null) {
        return
    }
    fooNotNull(<!DEBUG_INFO_SMARTCAST!>s<!>)
}
