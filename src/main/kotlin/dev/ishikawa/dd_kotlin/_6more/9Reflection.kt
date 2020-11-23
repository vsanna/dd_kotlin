package dev.ishikawa.dd_kotlin._6more

import dev.ishikawa.dd_kotlin._2object.User

fun main() {
    val kClass = User::class
    println(kClass.constructors)
    println(kClass.simpleName)
    println(kClass.java.simpleName)

    fun isOdd(x: Int): Boolean = x % 2 == 0
    val ints = listOf(1,2,3,4,4,5)
    println(ints.filter(::isOdd))

    val x = 1
//    println(::x.get()) // value
//    println(::x.name)  // property name
}

/*
org.jetbrains.kotlin:kotlin-reflect 必要

class reference
MyClass::class


callable reference
fun isOdd(x: Int): Boolean = x % 2 == 0
val ints = listOf(1,2,3,4,4,5)
listOf.filter(::isOdd)

property reference
::x.name. なんかつかえない not yet supportedとでる
* */