package dev.ishikawa.dd_coroutine

import dev.ishikawa.dd_coroutine.util.showDebug
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.intrinsics.startCoroutineCancellable
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.intrinsics.startCoroutineUninterceptedOrReturn



/*
       1 [74525]current at: id:   1 name: main @coroutine#1
       2 [74526]current at: id:   1 name: main @coroutine#1
       5 [74526]current at: id:   1 name: main @coroutine#1
       3 [74540]current at: id:   1 name: main @coroutine#1
       4 [74648]current at: id:   1 name: main @coroutine#1
       6 [74650]current at: id:   1 name: main @coroutine#1

* */
@OptIn(InternalCoroutinesApi::class)
fun main() {
    runBlocking {
        // startCoroutineUninterceptedOrReturn は suspendCoroutineUninterceptedOrReturnの中で使われる想定
        // 目的としては suspendCoroutineUninterceptedOrReturn によりsuspendされたcoroutineを再開させるため
        // suspendCoroutineの中の重たい処理からの再開処理を記述するため

        showDebug("1")
        suspendCoroutineUninterceptedOrReturn { currentcont: Continuation<Int> ->
            showDebug("2")
            // coroutineの外の世界の道具に対し、そいつの終了時にcurrentcontを再開させる処理を記述する。
            suspend {
                showDebug("3")
                delay(100)
                showDebug("4")
                234
            }.startCoroutineCancellable(currentcont)


            showDebug("5")
            COROUTINE_SUSPENDED
        }
        showDebug("6")
    }
}

fun longRunningJob(cont: Continuation<Int>) {
    Thread.sleep(100)
    cont.resumeWith(Result.success(200))
}

suspend fun longRunningJobCoroutine(): Int {
    delay(100)
    return 20
}