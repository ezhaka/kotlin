fun <T : Any?> foo(x: T) {
    if (x is String<!USELESS_NULLABLE_CHECK!>?<!>) {
        x<!UNSAFE_CALL!>.<!>size

        if (x != null) {
            <!DEBUG_INFO_SMARTCAST!>x<!>.size
        }
    }

    if (x is String) {
        <!DEBUG_INFO_SMARTCAST!>x<!>.size
    }
}
