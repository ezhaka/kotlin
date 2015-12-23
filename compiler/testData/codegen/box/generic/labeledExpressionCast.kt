class BehaviourSubject<T>(val value: T)

fun box() : String {
    val sub = BehaviourSubject<Long>(x@ (1 + 2))
    val expected: Long? = 3L
    return if (sub.value == expected) "OK" else "fail"
}