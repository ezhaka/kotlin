// !CHECK_TYPE
interface Tr<T> {
    var v: Tr<T>
}

fun test(t: Tr<*>) {
    <!SETTER_PROJECTED_OUT!>t.v<!> = t
    t.v checkType { _<Tr<*>>() }
}