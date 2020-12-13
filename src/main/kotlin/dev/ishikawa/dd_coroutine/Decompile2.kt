package dev.ishikawa.dd_coroutine

import dev.ishikawa.dd_coroutine.util.showDebug
import kotlinx.coroutines.*
import java.util.*
import kotlin.concurrent.thread
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn


fun main() {
    runBlocking {
        launch {
            delay(10)
            var result = longRunningIOWork()
            result = longRunningIOWork(result)
            val finalResult = longRunningIOWork(result)
        }
        showDebug("finished")
    }
}

suspend fun longRunningIOWork(num: Int = 0): Int {
    return suspendCoroutineUninterceptedOrReturn { currentcont: Continuation<Int> ->
        thread {
            Thread.sleep(300)
            currentcont.resumeWith(Result.success(num + 10))
        }
        COROUTINE_SUSPENDED
    }
}

