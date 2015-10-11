// "Remove cast" "true"
fun test(x: Any): Int {
    if (x is String) {
        return (x <caret>as String).size
    }
    return -1
}
