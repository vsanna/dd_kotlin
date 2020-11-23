package dev.ishikawa.dd_kotlin.`object`

fun main() {

    val box: Box<Int> = Box(1)
    println(box)
}

/*
https://kotlinlang.org/docs/reference/generics.html
TODO
*/

class Box<T>(t: T) {
    var value = t
}

