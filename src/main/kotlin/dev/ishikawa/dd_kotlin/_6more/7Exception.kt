@file:Suppress("UNREACHABLE_CODE")

package dev.ishikawa.dd_kotlin._6more

import java.lang.IllegalArgumentException

fun main() {
    val a: Int? = try {
        2222
        throw Exception("dummy")
        123
    } catch(ex: Exception) {
        12212121
    } finally {
        0
    }

    println(a) // 12212121
}

/*
* Exception
*
* tryは式. val hoge = try {...} finally { null }
* catchは対象、finallyの値は返されない
*
* checked exception
* kotlinにchecked exceptionない
*
* Nothing型
* TypeScriptにもあったnever型。決してそこにたどり着かない
* */


fun mustError(): Nothing {
    throw IllegalArgumentException("hoge")
}