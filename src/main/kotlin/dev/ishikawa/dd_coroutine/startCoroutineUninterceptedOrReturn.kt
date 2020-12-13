package dev.ishikawa.dd_coroutine

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.intrinsics.startCoroutineUninterceptedOrReturn


fun main() {
    runBlocking {
        // create promise
        val promise = Continuation<Int>(EmptyCoroutineContext) { result: Result<Int> ->
            if (result.isFailure) {
                println("failure...")
            }
            if (result.isSuccess) {
                println("success!")
            }
        }

        // receiverのsuspend functionが完了したらその結果とともにpromiseをinvoke
        // result == 100
//        val result = ::getNum.startCoroutineUninterceptedOrReturn(promise)

        // startCoroutineUninterceptedOrReturn はsusマークか返り値を返す
        // result == COROUTINE_SUSPENDED
        val b = 1
        val result = ::getNumWithDelay.startCoroutineUninterceptedOrReturn(promise)
        println(result)

        // 面白い! ここではUnitとして返される
        // a == kotlin.Unit
        val a = delay(20)
        println(a)

        // つまり、 startCoroutineUninterceptedOrReturn か suspendCoroutineUninterceptedOrReturn は
        // susマークをkotlinの世界で上へ返すことができる
        // -> これは誤り!!

        // 単に startCoroutineUninterceptedOrReturn が通常funで
        // suspendCoroutineUninterceptedOrReturnとdelay は suspend関数なだけ


        /*
https://github.com/JetBrains/kotlin/blob/master/libraries/stdlib/jvm/src/kotlin/coroutines/intrinsics/IntrinsicsJvm.kt

 * This function is designed to be used from inside of [suspendCoroutineUninterceptedOrReturn] to resume the execution of the suspended
 * coroutine using a reference to the suspending function.

@SinceKotlin("1.3")
@InlineOnly
public actual inline fun <T> (suspend () -> T).startCoroutineUninterceptedOrReturn(
    completion: Continuation<T>
): Any? = (this as Function1<Continuation<T>, Any?>).invoke(completion)


        * */
    }
}


suspend fun getNum(): Int {
    return 100
}

suspend fun getNumWithDelay(): Int {
    delay(10)
    return 100
}