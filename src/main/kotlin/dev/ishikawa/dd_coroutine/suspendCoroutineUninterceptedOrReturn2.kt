package dev.ishikawa.dd_coroutine

import dev.ishikawa.dd_coroutine.util.showDebug
import kotlinx.coroutines.*
import kotlin.concurrent.thread
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.intrinsics.*
import kotlin.coroutines.resume


/*
       1 [58894]current at: id:   1 name: main @coroutine#1
       2 [58896]current at: id:  13 name: Thread-1
       3 [59901]current at: id:  13 name: Thread-1
       5 [59901]current at: id:  13 name: Thread-1
kotlin.Unit
       4 [59904]current at: id:  13 name: Thread-1

* */
@OptIn(InternalCoroutinesApi::class)
fun main() {
    runBlocking {
        // runBlockingの中なのでdispatcher = eventloop. 故にここでsuspendしたとき5に飛ばないはず。
        val result = suspendCoroutineUninterceptedOrReturn { currentcont: Continuation<Unit> ->
            // suspendCoroutineUninterceptedOrReturnの中では後でこれを再開できるような処理をしておく
            // httpclientの場合はsession開いて、そのsessionにresが来たら再開するconnectionを見つけられるようmapping/relationをもたせておく
            showDebug("1")
            thread {
                showDebug("2")
                Thread.sleep(1000)
                showDebug("3")
                currentcont.resume(Unit)
                showDebug("4")
            }
            COROUTINE_SUSPENDED
        }
        showDebug("5")
        println(result)
    }
}

