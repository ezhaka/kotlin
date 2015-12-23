class BehaviourSubject<T>(val value: T)

fun box() : String {
    val sub = BehaviourSubject<Long>((-1))
    val expected: Long? = -1L
    return if (sub.value == expected) "OK" else "fail"
}