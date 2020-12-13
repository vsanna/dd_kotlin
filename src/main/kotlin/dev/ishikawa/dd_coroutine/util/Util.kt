package dev.ishikawa.dd_coroutine.util

import java.time.LocalDateTime
import java.time.ZoneOffset


fun showDebug(prefix: String = "", prefixLen: Long = 8) {
    println("${if(prefix.isEmpty()) "" else "%${prefixLen}s".format(prefix) } [${LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli() % 100000}]current at: id:${"%4s".format(Thread.currentThread().id.toString())} name: ${Thread.currentThread().name}")
}