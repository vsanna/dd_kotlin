package dev.ishikawa.dd_kotlin.basic

import dev.ishikawa.dd_kotlin.packagesample.Message
import dev.ishikawa.dd_kotlin.packagesample.printmsg
import hello

fun main() {
    val message = Message("hello")
    printmsg(message)
    hello()
}