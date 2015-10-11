// FILE: KotlinFile.kt
fun foo(javaClass: JavaClass) {
    javaClass.getSomething()<!UNSAFE_CALL!>.<!>size
    javaClass.something<!UNSAFE_CALL!>.<!>size
}

// FILE: JavaClass.java
import org.jetbrains.annotations.*;

public class JavaClass {
    @Nullable
    public String getSomething() { return null; }
}