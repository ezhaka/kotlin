//KT-2484 Type inferred for function literal is (...) -> Int instead of (...) -> Unit, when function literal parameter is explicit 

package a
//+JDK

fun <T> Array<T>.forEach(operation: (T) -> Unit) : Unit { for (element in this) operation(element) }

fun bar(operation: (String) -> Unit) = operation("")

fun main(args: Array<String>) {
    args.forEach { a : String -> a.size } // Type mismatch: (String) -> Unit required, (String) -> Int found
    args.forEach { a -> a.size } // Type mismatch: (String) -> Unit required, (String) -> Int found
    args.forEach { it.size }     // This works!

    bar { a: String -> a.size }
    bar { a -> a.size }
    bar { it.size }
}
