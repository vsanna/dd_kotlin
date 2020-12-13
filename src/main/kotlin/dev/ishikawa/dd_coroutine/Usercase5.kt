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
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.resume
import kotlin.coroutines.intrinsics.*

fun main() {
    runBlocking {
        launch {
            suspendCoroutineUninterceptedOrReturn { continuation ->
                thread {
                    Thread.sleep(100)
                    continuation.resume(Unit)
                }
                COROUTINE_SUSPENDED
            }
        }
    }
}

