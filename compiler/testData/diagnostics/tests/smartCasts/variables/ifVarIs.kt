public fun bar(s: String) {
    System.out.println("Length of $s is ${s.size}")
}

public fun foo() {
    var s: Any = "not null"
    if (s is String) 
        bar(<!DEBUG_INFO_SMARTCAST!>s<!>)
}
