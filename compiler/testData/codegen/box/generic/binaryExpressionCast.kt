class BehaviourSubject<T>(val value: T)

fun box() : String {
    val sub = BehaviourSubject<Long>(2 * 3)
    val expected: Long? = 6L
    return if (sub.value == expected) "OK" else "fail"
}