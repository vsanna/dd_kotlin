package dev.ishikawa.dd_kotlin._5coroutine

import com.github.javafaker.Faker
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis


fun main() {
    runBlocking {
        callOuterServiceAndUseTheResult()
    }
}

suspend fun callOuterServiceAndUseTheResult(): Int {
    // sequentialに処理したい場合:
    // 普通に順番に呼び出すのみ
    val userId = callDb("username")
    val items = callOuterService(userId)
    val itemsAfterOps = doSomeOps(items)

    // concurrentに呼び出したいとき
    // asyncを使う。launchと似ている。新しいcorotutineを起動する
    // asyncはDeferredを返す(launchはjobを返す). Promiseみたいなもの(jobを継承)
    // デフォルトではasync呼び出した時点で中身走り出す
    var time = measureTimeMillis {
        val result1 = GlobalScope.async {
            println("=====1")
            callOuterService(1)
        }
        val result2 = GlobalScope.async { callOuterService(2) }
        println("=====2") // =====1のあとに走る
        println("the answer is ${result1.await()} ${result2.await()}")
    }
    println("in $time ms")

    // CoroutineStart.LAZYでstartを遅らせられる
    time = measureTimeMillis {
        val one = GlobalScope.async(start = CoroutineStart.LAZY) {
            println("======3")
            callOuterService(1)
        }
        val two = GlobalScope.async(start = CoroutineStart.LAZY) { callOuterService(2) }
        println("=======4")
        // some computation
        one.start() // start the first one
        two.start() // start the second one
        println("The answer is ${one.await() + two.await()}")
    }
    println("Completed in $time ms")


    // うーーーーーん、、、、reactorと比べるとcomposing suspending functionsよわくない?

    return 0
}

// I/O heavy task
// findUserIdByUserName
suspend fun callDb(userName: String): Int {
    delay(100) // simulate
    return Faker().number().randomNumber().toInt()
}

suspend fun callOuterService(userId: Int): List<Int> {
    delay(300) // simulate
    return listOf(1,2,3,4,5)
}

// CPU heavy task
suspend fun doSomeOps(list: List<Int>): List<Int> {
    delay(2000)
    return list
}

// async function
// TODO: asyncはGlobalScopeでいいのけ?
fun callOuterServiceAsync(userId: Int) = GlobalScope.async {
    callOuterService(userId)
}