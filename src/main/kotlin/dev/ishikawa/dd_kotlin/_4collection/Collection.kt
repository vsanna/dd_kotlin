package dev.ishikawa.dd_kotlin._4collection

fun main() {
    array()
}

fun array() {
    var arr = arrayOf(1,2,3, "a")
    for (any in arr) println(any)
    println(arr.get(0) == arr[0])

    Array(5) { i -> i + 2 }.forEach { println(it) }
}