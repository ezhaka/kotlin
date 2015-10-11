// IS_APPLICABLE: false
fun doSomething<T>(a: T) {}

fun main(args: Array<String>) {
    val foo: String? = "abc"
    if (foo != null<caret>) {
        doSomething ("Hello")
        foo.size
    }
    else null
}
