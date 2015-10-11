// !DIAGNOSTICS: -UNUSED_PARAMETER
class A {
    constructor(x: Any, y: Any, z: Any)
    constructor(x: String?, y: String?): this(x!!, <!DEBUG_INFO_SMARTCAST!>x<!>.size.toString() + y!!, "") {
        <!DEBUG_INFO_SMARTCAST!>x<!>.size + <!DEBUG_INFO_SMARTCAST!>y<!>.size
    }
}
