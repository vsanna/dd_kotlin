package dev.ishikawa.dd_coroutine.usecases

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/*
* # goal
* - 低レベルの基底を理解する
*     - suspendCoroutine
*     - ContinuationImpl
*
*
* */
fun main() {
    runBlocking {
        repeat(3) {
            launch {
                // Dispatchers.Defaultになっているはず
                val result = longRunningIOOperation(it)
                println(result)
            }
        }
    }
}

private suspend fun longRunningIOOperation(idx: Int): String {
    delay(100)
    return "result:$idx"
}
