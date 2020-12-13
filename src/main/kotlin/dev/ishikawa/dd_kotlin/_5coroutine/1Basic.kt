package dev.ishikawa.dd_kotlin._5coroutine

import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

fun main() {

    GlobalScope.launch {
        delay(1000)
        println("world")
    }

    println("hello")
    Thread.sleep(2000)


    // thread {} はcoroutinを作らない。単にThreadを作るだけ
    // 故にdelayを中で呼べない
    thread {
//        delay(1000)
        Thread.sleep(1000)
        println("world")
    }

    println("hello")
    Thread.sleep(2000)


    GlobalScope.launch {
        delay(1000)
        println("hoge")
    }
    println("geho")

    // runBlockingはcoroutine builder のひとつ。
    // runBlockingは呼び出しthreadをBLOCKする!
    val a = runBlocking {
        delay(2000)
        100
    }
    println("a = $a")

    // mainをまるっとrunBlockingでwrapするidiomがある
    /*
    * fun main() = runBlocking<Unit> { ... }
    * suspend関数のテストを書く際のidiomでもある
    * */

    // coroutineの終わりを待つ
    GlobalScope.launch {
        delay(1000)
        println("world")
        val job = launch {
            delay(1000)
        }
        println("hello!")
        job.join() // suspend
        println("123123")
    }



    val c = GlobalScope.launch {
        val b = coroutineScope {
            delay(1000)
            100
        }
        println(b)
        delay(100)
        100
    }
    println("c = $c")




    GlobalScope.launch {
        val threadMap = ConcurrentHashMap<String, Int>()
        var total = 0
        // launchはいくつものスレッドで走っている!(今回のケースは)
        // 故に普通のint使うとinrementが使えない
        repeat(100_000) {
            launch {
                threadMap.compute(Thread.currentThread().id.toString()) { _, v -> if (v == null) 1 else v + 1 }
                total++
            }
        }
        println("total = $total")
        // total = 99795
        println(threadMap)
        // {22=17265, 23=17011, 13=16551, 24=16389, 20=17232, 21=15552}
    }

    GlobalScope.launch {
        val threadMap = ConcurrentHashMap<String, Int>()
        var total = AtomicInteger(0)
        repeat(100_000) {
            launch {
                threadMap.compute(Thread.currentThread().id.toString()) { _, v -> if (v == null) 1 else v + 1 }
                total.incrementAndGet().toString()
            }
        }
        delay(100)
        println("total(AtomicInteger) = ${total.get()}")
        // AtomicInteger使えば当然OK
        // total(AtomicInteger) = 100000
        println(threadMap)
        // {22=17437, 23=16798, 13=16242, 24=16229, 14=13, 20=17236, 21=16045}
    }

    GlobalScope.launch {
        val newSingleThreadContext = newSingleThreadContext("single!")
        val threadMap = ConcurrentHashMap<String, Int>()
        var total = 0
        repeat(100_000) {
            launch(context = newSingleThreadContext) {
                threadMap.compute(Thread.currentThread().id.toString()) { _, v -> if (v == null) 1 else v + 1 }
                total++
            }
        }
        println("total(int in single thread) = $total")
        // single threadに固定すればcoroutineはsequential!
        // total(int in single thread) = 100000
        println(threadMap)
        // {25=100000}
    }


    val r1 = measureTimeMillis {
        var a = 0
        repeat(1_000_000) {
            a += if(it%2 == 0) it else -1 * it
        }
        println("a = $a")
    }
    println("r1 = $r1")
//    a = -500000
//    r1 = 13

    GlobalScope.launch {
        val singleContext = newSingleThreadContext("single")
        var a = 0
        val r2 = measureTimeMillis {
            withContext(singleContext) {
                repeat(1_000_000) {
                    launch { a += if(it%2 == 0) it else -1 * it }
                }
            }
        }
        delay(1000)
        println("a(coroutine) = $a")
        println("r2 = $r2")
        // 値は同じだが遅い
//        a(coroutine) = -500000
//        r2 = 22348
    }

    GlobalScope.launch {
        var a = 0
        val r3 = measureTimeMillis {
            repeat(1_000_000) {
                launch { a += if(it%2 == 0) it else -1 * it }
            }
        }
        delay(1000)
        println("a(coroutine/several threads) = $a")
        println("r3 = $r3")
        // 当然ながら値もずれるし14秒かかる
//        a(coroutine/several threads) = -28271977
//        r3 = 14091
    }


    Thread.sleep(50000)
}

/*
## 疑問点
coroutine, scope, context
structured concurrency
cancelとexceptionの親子/兄弟間の関係がいまいちまだ...
launchの内容を待たずに次の処理に行くときと行かない時がある?
    - runScopingは行かない。なぜなら同じcoroutine上で処理しているから
    - launchは別coroutine. ではなぜlaunchInがいるの？
同一スレッド上で複数coroutineが走るとき、メモリアクセスはsequential?

## suspend functionとは
suspending function: 最低限の低レベルAPIを提供する
future/promiseよりもエラーを出しにくい抽象と言える。

kotlinx.coroutines: jetbrain製のハイレベルlib
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.1'

coroutine = light weight threads.
    - light: 100000個立ててもMMOしない

coroutineはある"scope"のもとで作られる
    - 原則、そのscope内のすべての処理が終わるまでそのscopeは閉じられない
    - GlobalScope: コルーチンの寿命はmainスレッドが終わるまで
    - TODO: scopeの単位で ThreadPoolを割り当てられる?

scope builder
    - launch {}
    - runBlocking {}
    - async {}
    - coroutineScope {}
    - ... これらはcontextとcoroutineを同時に作成する

"suspend" functions
    - threadをblockはしない
    - ただしcoroutinをsuspendはする
    - coroutineの中からのみ呼べる
    - suspend関数はsuspend関数を呼べる
    - つまりasync/awaitが組み込まれた関数というイメージ? そこで一旦coroutineはsuspendされて、それを担当していたthreadは他の処理へ映る


blockingの世界からnon-blockingの世界を行き来する
    - block -> non-blocking
        - coroutine builderを使ってcoroutineを生成する
        - launch, runBlocking, etc..
    - non-blocking -> block
        - 結論、常にこの遷移にはblock相当のものがひつよう
        - runBlocking, join..

Global scope のcoroutinesはdaemon threadsのようなもの
    - mainが終わるとおわる。


# ThreadPool/ExecutorServiceの責務はどう代替しているか
coroutine数上限(リソース上限)の管理
coroutineの死活管理 / エラー管理
coroutine間のデータ共有 or 受け渡し

タスク割当 + 割当のスケジューリング
# Reactorの責務はどう代替しているか
(stream記法(かなにか)を使った)宣言的な非同期処理の順序付き記述
非同期処理の様々な合成パターン


## Strucured Concurrency
- そのまま(GlobalScopeに) coroutineを使った場合の課題
    - 多くのcoroutineを立ち上げすぎてmemoryを食いつぶしたらどうなる?
    - 特定のcoroutineがhangしたらどうなる?
    - 起動した全てのcoroutineへの参照を手動で管理し続けるのは難しい
- そこでStructuredConcurrency instead of launching coroutines in the GlobalScope
    - 特定の"scope"の中でcoroutineを起動する
- 全てのcoroutine builderはそこでCoroutineScopeも生成する.
- あるcoroutineは、それが属するscopeの中でlaunchされた全てのcoroutineが完了するまで自信も追われない。
- さて、ではGlobalScopeに属するcoroutineはというと
    - daemon thread的なもの。呼び出し元はその終わりを待たない.
* */