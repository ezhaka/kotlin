public class X {
    public var x : String? = null
    private var y: String? = "abc"
    public fun fn(): Int {
        if (x != null)
            // Smartcast is not possible for variable properties
            return x<!UNSAFE_CALL!>.<!>size
        else if (y != null)
            // Even if they are private
            return y<!UNSAFE_CALL!>.<!>size
        else
            return 0
    }
}

