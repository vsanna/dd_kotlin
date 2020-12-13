package dev.ishikawa.dd_coroutine.usecases

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

/*
* goal
* - runBlocking, BlockingCoroutine, BlockingEventLoop, joinRunningを理解する
* - continuationの動き
* - completable = callerのcontinuation
*
* point
* - EventLoopのenqueueとContinuationImplのresumeWithをdebuggerpoint入れておくと良い
* - Dispatchers.Defaultを使う場合はdispatcherにもdebugger
*
* まずmainがrunBlockingないでdispatch
* mainが取り出されてresumeWith
*   longRunningIOOperationがdelayの中で[1] contをdispatcherにいれつつ、[2]susマーク返すのでreturn
* 次にcontが取り出され処理. 値を返してBaseCoroutineImplのwhileの中を一回進み、二階名のループで同上. 以下繰り返す
*
*
* */
fun main() {
    runBlocking {
        var msg = longRunningIOOperation("", "hello")
        msg = longRunningIOOperation(msg, "world")
        msg = longRunningIOOperation(msg, "!")
        msg = longRunningIOOperation(msg, "let's")
        msg = longRunningIOOperation(msg, "learn")
        println(msg)
    }
}

private suspend fun longRunningIOOperation(base: String, addition: String): String {
    // NOTE: delayの中をdebugしてdispatchの様子見る
    delay(100)
    if (base.isEmpty()) {
        return addition
    }
    return "$base:$addition"
}
