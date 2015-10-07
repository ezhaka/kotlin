internal open class My

// valid, internal from internal
internal open class Your: My() {
    // valid, effectively internal
    fun foo() = My()
}

// error, public from internal
open class His: <!EXPOSED_SUPER_CLASS!>Your()<!> {
    protected open class Inner
    // error, public from internal
    <!EXPOSED_PROPERTY_TYPE!>val x = My()<!>
    // valid, private from internal
    private fun bar() = My()
    // error, protected from internal
    protected fun <!EXPOSED_FUNCTION_RETURN_TYPE!>baz<!>() = Your()
}

internal class Their: His() {
    // error, effectively internal from protected
    class InnerDerived: <!EXPOSED_SUPER_CLASS!>His.Inner()<!>
}