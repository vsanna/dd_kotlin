package dev.ishikawa.dd_coroutine

import kotlinx.coroutines.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.intrinsics.*

@OptIn(InternalCoroutinesApi::class)
fun main() {
    // runBlockingの中でCOROUTINE_SUSPENDED返すとどうなる?(resumeする予定なし)
    runBlocking {
        // ここで起動しているcoroutine
        // (runBlockingの中のcoroutine) = "coroutine#1":BlockingCoroutine{Active}@6ee4d9ab
        // this = "coroutine#1":BlockingCoroutine{Active}@6ee4d9ab

        // 呼び出し元のcontinuationをsuspendする or 値を返す
        // ここでいうとrunBlockingが作るcontinuation自体
        val a = 1
        val result = suspendCoroutineUninterceptedOrReturn { it: Continuation<Int> ->
            // this = "coroutine#1":BlockingCoroutine{Active}@6ee4d9ab
            // it   = Continuation at dev.ishikawa.dd_coroutine.SuspendCoroutineUninterceptedOrReturnKt$main$1.invokeSuspend(suspendCoroutineUninterceptedOrReturn.kt:20)


            // これがonですぐに値返される
            // return@suspendCoroutineUninterceptedOrReturn 1000

            // これがonだとやはりmain関数終わらない
            // susマークが帰ってきたときswitchはどう動くのか.
            // 次のcoroutineを取り出そうとするが何も詰まっていなくて待つ...
            // そのコードはどこか -> switchを見る必要があるのでbyteコード見る必要がある
            val a = 1
            COROUTINE_SUSPENDED

            /*
            * 本当はこう使うはず
            *
            * if(hasCache) {
            *   // 即時return
            *   return@suspendCoroutineUninterceptedOrReturn cache
            * } else {
            *   // 結局別処理に更に任している?
            *   next.startCoroutineUninterceptedOrReturn3(me, me.subject, me.continuation)
            *   COROUTINE_SUSPENDED
            * }
            * */


            /*
            * suspendCoroutineUninterceptedOrReturn looks like this.
            * suspend fun foo(bar: suspend () -> Unit) = suspendCoroutineOrReturn { cont ->
            *   while (true) {
            *     if (tryToWaitWith(cont)) return SUSPENDED_MARKER // suscesfully added cont to waiters list, will get resumed later
            *     return bar.startCorotuineOrReturn(cont)
            *   }
            * }
            * */

            // suspendCoroutineUninterceptedOrReturnにわたすlambdaは通常func. delay呼べない
            // delay(100)
        }
        println(result)

    }

    //
}

