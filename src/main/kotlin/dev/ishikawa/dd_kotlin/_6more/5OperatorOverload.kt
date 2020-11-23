@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER")

package dev.ishikawa.dd_kotlin._6more


fun main() {
    val a: Int = 100
    println(+a)
    println(a + 20)

    val point = Point(100, 200)
    println(point + Point(-12, -23))

    println("hello " * 10)
}

/*
C++的な感じ

すでにあるものは書き換えられないっぽい

* */


// 効果なし
operator fun Int.unaryPlus(): Int {
    val a = 1
    return this + 100
}

operator fun Int.plus(other: Int): Int {
    return this + other + 10000
}


data class Point(val x: Int, val y: Int)

operator fun Point.plus(other: Point): Point {
    return Point(this.x + other.x, this.y + this.y)
}


operator fun String.times(n: Int): String {
    return this.repeat(n)
}