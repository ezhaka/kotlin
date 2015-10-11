public interface A {
    public val x: Any
}

public class B(override public val x: Any) : A {
    fun foo(): Int {
        if (x is String) {
            return <!DEBUG_INFO_SMARTCAST!>x<!>.size
        } else {
            return 0
        }
    }
}