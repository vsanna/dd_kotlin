package dev.ishikawa.dd_coroutine

import dev.ishikawa.dd_coroutine.mockapiio.*
import dev.ishikawa.dd_coroutine.service.Service
import dev.ishikawa.dd_coroutine.util.showDebug
import dev.ishikawa.dd_kotlin._5coroutine.intFlow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis
import java.util.concurrent.ConcurrentHashMap

fun main() {
    nestedScope2()
}

/*
1. scopeのcancelは下流のscopeに対しても伝播するか
2. scopeは子scopeのcoroutineも待つのか
*/

/*
       0 [6278]current at: id:   1 name: main @coroutine#1
       4 [6283]current at: id:   1 name: main @coroutine#1
       1 [6286]current at: id:   1 name: main @coroutine#2
       2 [6293]current at: id:   1 name: main @coroutine#3
       3 [6293]current at: id:   1 name: main @coroutine#4

// 一番外のrunBlockingが作るscopeは待ってる
* */
fun nestedScope() = runBlocking {
    dev.ishikawa.dd_coroutine.util.showDebug("0")
    launch {
        dev.ishikawa.dd_coroutine.util.showDebug("1")
        coroutineScope {
            launch { dev.ishikawa.dd_coroutine.util.showDebug("2") }
            async { dev.ishikawa.dd_coroutine.util.showDebug("3") }
        }
    }
    dev.ishikawa.dd_coroutine.util.showDebug("4")
}

/*
       0 [70650]current at: id:   1 name: main @coroutine#1
       4 [70656]current at: id:   1 name: main @coroutine#1

つまりcoroutineScopeもキャンセルされている。
* */
fun nestedScope2() = runBlocking {
    dev.ishikawa.dd_coroutine.util.showDebug("0")
    val a = 1
    dev.ishikawa.dd_coroutine.util.showDebug(a.toString())
    launch {
        dev.ishikawa.dd_coroutine.util.showDebug("1")
        coroutineScope {
            launch { dev.ishikawa.dd_coroutine.util.showDebug("2") }
            async { dev.ishikawa.dd_coroutine.util.showDebug("3") }
        }
    }
    dev.ishikawa.dd_coroutine.util.showDebug("4")
}

