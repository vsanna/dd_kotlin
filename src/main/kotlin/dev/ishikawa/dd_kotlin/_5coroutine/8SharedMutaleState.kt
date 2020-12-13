package dev.ishikawa.dd_kotlin._5coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.system.measureTimeMillis


fun main() {
    runBlocking {
/*
## Shared mutable state and concurrency
コルーチンはマルチスレッドで動くこともできる
その際にはいつものマルチスレッド上での工夫が必要になる


1. thread-safe data structuresを使う
    - AtomicInteger, ConcurrentHashMap
2. thread confinement fine-grained
    - 特定のshared stateへのアクセスはたった一つのスレッドから(に限ってsequentialに)する、というもの
    - 気をつけないとパフォーマンス悪くなる. 大きめの処理をsequentialにしなければならないときはスレッドのスイッチングコストも大したことないのでこれで行く
3. Thread confinement coarse-grained
    - 2の反省を生かしてthread swithingが起きる階数が減りつつも並行処理問題を回避できるギリギリまで同一contextを広げる
4. mutex
    - (1の中身でsynchronizedが使われているので4の応用が1とも言える気がするが) critical sectionをsequentialにする
    - つまりlockを使う
    - java: syncrhonized, ReentrantLock
    - kotlin: Mutex
        - 違いはMutex.lockはsuspend function. つまりそれを使いたいcoroutineがlock待ちをするさいに、その下のthreadは別coroutineを動かす
5. actor
    - actorとはentityで、1つのcoroutine / あるstate / 1つのchannelからなる。
    - functionとしても記述できるが、複雑ならクラスにする
    - 思い負荷のもとではactorの方がmutexよりも早い。なぜならcontext swithcないから
* */

        var counter = 0
        withContext(Dispatchers.Default) {
            massiveRun { counter++ }
        }
        println("counter = $counter")
//        Completed 100000 actions in 53 ms
//        counter = 82732


        var counter2 = 0
        val counterContext = newSingleThreadContext("counterContext")
        withContext(Dispatchers.Default) {
            massiveRun {
                // ここでDispathers.Defaultの指定するthreadからcounterContextの指定するthreadへのswitchが毎度発生する
                // ので遅い。
                withContext(counterContext) {
                    counter2++
                }
            }
        }
        println("counter2 = $counter2")
//        Completed 100000 actions in 1527 ms
//        counter2 = 100000

        var counter3 = 0
        val counterContext2 = newSingleThreadContext("counterContext2")
        withContext(counterContext2) {
            massiveRun {
                counter3++
            }
        }
        println("counter3 = $counter3")
//        Completed 100000 actions in 23 ms
//        counter3 = 100000


        val mutex = Mutex()
        var counter4 = 0
        withContext(Dispatchers.Default) {
            massiveRun {
                mutex.withLock { counter4++ }
            }
        }
        println("counter4 = $counter4")
//        Completed 100000 actions in 756 ms
//        counter4 = 100000


        val ctr = counterActor()
        withContext(Dispatchers.Default) {
            massiveRun { ctr.send(IncCounter) }
        }
        val response = CompletableDeferred<Int>()
        ctr.send(GetCounter(response)) // responseに値がセットされる
        println("Counter5 = ${response.await()}")
        ctr.close()
//        Completed 100000 actions in 342 ms
//        Counter5 = 100000

    }
}

suspend fun massiveRun(action: suspend () -> Unit) {
    val n = 100  // number of coroutines to launch
    val k = 1000 // times an action is repeated by each coroutine
    val time = measureTimeMillis {
        coroutineScope { // scope for coroutines
            repeat(n) {
                launch { // 100個のcoroutineを起動する
                    repeat(k) { action() } // その中で1000回同じ処理を行う
                }
            }
        }
    }
    println("Completed ${n * k} actions in $time ms")
}

sealed class CounterMsg
object IncCounter : CounterMsg()
class GetCounter(val response: CompletableDeferred<Int>): CounterMsg()

fun CoroutineScope.counterActor() = actor<CounterMsg> {
    var counter = 0
    for (msg in channel) {
        when(msg) {
            is IncCounter -> counter++
            is GetCounter -> msg.response.complete(counter)
        }
    }
}

