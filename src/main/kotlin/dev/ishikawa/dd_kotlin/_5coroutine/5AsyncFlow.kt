package dev.ishikawa.dd_kotlin._5coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import kotlin.system.measureTimeMillis


fun main() {
    runBlocking {
        /**
         * # flow
         * suspend functionが非同期に(別場所で処理しつつ都合がいいときに)単一の値を返すもの、としたとき
         * flow = 非同期に複数の値を順次返すもの。
         * suspend function == Mono, flow == Flux
         * note: 結果のlistが出来上がるまでsuspendしつつまとめてmono<List<T>>で返せばいいのでは?と思わなくもないがその間下流作業は進まない。
         * 全体効率としては悪い。
         *
         * ## ref: Sequence
         * sequence { repeat(100) { yield(it) } } でStreamっぽいものを作れる。が、これはflowではない。
         * なんでここで紹介されているんだろうね。似ているから?
         *
         *
         * ## flow is cold
         * collectを呼び出すまで中間処理含め、何も処理は動き出さない。
         * val f = intFlow(10)
              .onEach { println("1. it = $it") }
              .map { it * 20 }
              .onEach { println("2. it = $it") }
           println("hello") // 先に出力
           f.collect { println(it) }
         *
         *
         * ## cancel / timeout
         * flowがsuspend中にのみcancel可能
         *
         * val r = measureTimeMillis {
         *     withTimeoutOrNull(250) { // Timeout after 250ms
         *         flow {
         *             repeat(5) {
         *                 delay(100)
         *                 emit(it)
         *             }
         *         }.collect { value -> println(value) }
         *     }
         * }
         * println("time = $r") // 301
         *
         *
         * ## Flow builders
         * flow {} と asFlow()
         * intFlow()
         *
         *
         * ## 中間operators
         *
         * ### transform
         * map filter
         * transform: emitが使える。そうするとmapとfilter(とflow要素を増やす)が同時にできる。上流からの値をitで受けつつ下流に渡したい値のみを渡したい形でemitする。
         *
         * intFlow(10)
         *     .transform { it ->
         *         // ≒ filter
         *         if (it > 3) {
         *             // ≒ map
         *             emit(it * 10)
         *         }
         *         // 要素を増やすこともできる
         *         emit(-1 * it)
         *     }
         *
         *
         * ## size limitting
         * take:
         * - 上流から指定した個数の要素しか受け取らない。
         * - 受け取った時点で上流に対してcancelを実行する。cancelされたflowは内部でスローされる
         *     - これによりflow内部でtry catchできるのでリソース開放などができる
         *     - おそらくだが、coroutineのswitch文に入ったときcancel済みだとエラーが出てそこでのcontext objectが捨てられるとかそんな感じなのでは
         * - flow内部で特にcatchする必要はない
         *     - が、実験のためにcatchすると kotlinx.coroutines.flow.internal.AbortFlowException: Flow was aborted, no more elements needed を観察できる
         *
         * fun numbers(): Flow<Int> = flow {
         *   try {
         *     emit(1)
         *     emit(2)
         *     println("this line won't execute")
         *     emit(3)
         *   } finally {
         *     println("finally pass here")
         *   }
         * }
         *
         * numbers().take(2).collect
         *
         *
         * ## Terminal operators
         * collect
         * toList, toSet
         * first
         * reduce fold
         *
         *
         * ## Flows are sequential
         * 原則、flowの各要素は順次sequentialに処理される。
         * 要素1がpipelineを全て通り終わったら次の要素が先頭からまた処理を開始する
         *
         * TODO: これ本当? 無駄では?下の実験ではそのとおりなんだけど... Usercase2で結構実験したけどたしかにそのように見える..
         *
         * intFlow(4)
         *     .filter {
         *         println("Filter $it")
         *         it % 2 == 0
         *     }
         *     .map {
         *         println("Map $it")
         *         "stringified $it"
         *     }
         *     .collect { println(it) }
         *
         * Filter 0
         * Map 0
         * stringified 0
         * Filter 1
         * Filter 2
         * Map 2
         * stringified 2
         * Filter 3
         * Filter 4
         * Map 4
         * stringified 4

         *
         * ## Flow context
         * collectのcoroutineはinvokerがその時に持つcontextをそのまま使う = context preservation と呼ばれる
         *     - この場でのinvokerはcollectの呼び出しもと or downstreamの処理
         *     - contextは下から上へでんぱしていく
         * downstream側がupstream側の実装詳細を気にせずに別途開発できるようにするためのもの
         *
         * しかし、例えば「あるI/Oを伴う処理を行ったあとにUIを更新したい」ようなとき、この２つで別のContext(Thread)を使いたい。
         * だが、flowの中で別contextからのemitを行うのは禁じられている
         *
         * そこでflowOn, flow contextを変更する唯一の方法がflowOn.
         *
         *
         * ## buffering
         * contextは変えないがbufferよりも上流を別coroutineで走らせ、下流が待たずとも上流が動けるようにする
         * Note: これがsequential flowの挙動を帰るやつだ。Usecase2のsequentialFlowWithBuffer2がわかりやすい。
         * Emit/Collectの順序がばらばらになる
         *
         *
         * ## conflate
         * 下流の方が遅いとき、下流が取得できるタイミングで上流がemitした最新の値のみを受け取る。それまでにemitされたものは捨てる
         * つまりこれもbuffer同様上流が下流を待たずにemitする挙動になる
         * Note: いつ使うんだろう...
         *
         *
         * ## xxxLatest
         * slow collectorをcancel & restartしてスピードアップする(ということは常にslowな場合は何も処理できない)
         * collectLatest {}
         * - collect関数の代わり。この処理中に新しい値が上流からemitされるとそれまでの処理を捨ててまた一からやり直す
         *
         * val time = measureTimeMillis {
         * simple()
         *     .collectLatest { value -> // cancel & restart on the latest value
         *          println("Collecting $value")
         *          delay(300)
         *          println("Done $value") // 最後の要素でしか表示されない。それまでは常にemitの方がconsumeよりも早いのでdelay中にcancel & restartされている
         *     }
         * }
         * println("Collected in $time ms")
         *
         *
         * ## Composing multi flows
         * ### zip
         * val a = (1..3).asFlow()
         * val b = (-10..-5).asFlow()
         * a.zip(b) {s, t -> s + t}.collect { println(it) }
         * 余った要素は無視される(短い方に長さ揃う)
         *
         * ### combine
         * ２つのflowを入力としてもち、片方から新しい値がemitされるたびにその時点での最新の値の組み合わせで出力
         *
        /*
        1 -> one at 447         // t=400 で初ペア生成
        2 -> one at 682         // t=600 でnumsが再度emit. 値の更新があったのでその時点の最新の値の組み合わせで出力
        2 -> two at 856         // t=800 でstrsが再度emit. 同上
        3 -> two at 985         // t=900
        3 -> three at 1262      // t=1200
        4 -> three at 1288      // t-1200
        5 -> three at 1592      // t=1500 この時点でnumsは出し切った。
        5 -> four at 1667       // t-1600
        5 -> five at 2072       // t=2000
        5 -> size at 2478
        5 -> seven at 2881
         * */
        private fun combineFlow() {
        runBlocking {
        val nums = (1..5).asFlow().onEach { delay(300) }
        // 余った要素は捨てられる
        val strs = flowOf("one", "two", "three", "four", "five", "size", "seven").onEach { delay(400) }
        val startTime = System.currentTimeMillis()
        nums.combine(strs) { a, b -> "$a -> $b" }.collect { println("$it at ${System.currentTimeMillis() - startTime}") }
        }
        }
         *
         * ## Flatten flows
         *
         * fun requestFlow(i: Int): Flow<String> = flow {
         *   emit("$i: First")
         *   delay(500)
         *   emit("$i: Second")
         * }
         *
         * があるとする。
         *
         * ### flatMapConcat
         * - flatMapConcat
         *   - 中のflowが終わるまで待つ
         * - flattneConcat
         *
         * (1..4).asFlow().onEach { delay(300) }
         *     .flatMapConcat { requestFlow(it) } // 1に対して二度emitされるのを待つ
         *     .collect
         *
         * 1: First at 121 ms from start
         * 1: Second at 622 ms from start
         * 2: First at 727 ms from start
         * 2: Second at 1227 ms from start
         * 3: First at 1328 ms from start
         * 3: Second at 1829 ms from start
         *
         *
         * ### flatMapMerge
         * - flatMapMerge
         *   - 順序関係なしに全てまとめて並行処理する
         *
         * 1: First at 136 ms from start
         * 2: First at 231 ms from start
         * 3: First at 333 ms from start
         * 1: Second at 639 ms from start
         * 2: Second at 732 ms from start
         * 3: Second at 833 ms from start
         *
         *
         * ### flatMapLatest
         * - flatMapLatest
         *   - 新しい値がemitされるとキャンセルされる
         *
         *
         * ## Exception
         * 中間処理とcollectorの中でのエラーをキャッチする
         * 基本的に中間処理/collectorの中でのエラーは外に出すべき
         * そして外でtry-catchできるように。
         *
         * try {
         *   number()
         *       .onEach {...}
         *       .collect {...}
         * } catch {
         *   // error handling
         * }
         *
         * ### Exception transparency
         * しかし上流(emitter)は例外をカプセル化したい
         * = flow内部のエラーをそのflowの呼び出して向けのエラーに変換したい
         * そこで .catch {}
         *
         * 1. .catch { ex -> throw ExternalException(ex) } で外向けのエラーに翻訳して外へでる
         * 2. .catch { ex ->  emit("some value from $ex") } で下流を継続。といっても上流の値はもう流れてこない. exceptionで止まる
         * 3. .catch { ex -> log.info(ex) } でエラー自体は握りつぶしてただログを残すなどもできる
         *
         * catchは上流に対して有効
         *
         *
         * ## Flow completion
         * flow処理の完了時に絶対に行いたい処理の記述
         *
         * 1. imperative
         * try {
         *     numbers().collect {}
         * } finally {
         *     // do something
         * }
         *
         * 2. declarative
         * numbers()
         *     .onCompletion { println("Done") } // 先頭で宣言する。
         *     .collect
         *
         * ## Launching flow
         * addEventListener的なものはどうしたいか
         * - intermediate なら onEach.
         * - terminal なら launchIn
         *     - collect使うとflow全体が終わるまで後続処理が待ってしまうので、launchInで別coroutineにそのflowの処理を渡してしまう
         *     - launhcInで渡されたCoroutineScopeのもとでそのflowはlaunchされる
         *
         * 実際のappでは何かしら寿命をもつentityをもとにしたscopeが渡されるので、そのentityがterminateされると
         * そのflowもキャンセルされる
         *
         *
         * ## Flow cancellation check
         * 下流でcancelしたときに上流がどう振る舞うか
         * - 上流のemitを止める場合もあるが
         * - 大半の場合は「cancelあともemitしきった後に最後にexceptionをthrow」という挙動
         * - この挙動を変更するのが
         *   - .onEach { currentCoroutineContext().ensureActive() }
         *   - そのshort-handとしての .cancellable()
         *
         *
      * */

        /*
        * map, filter
        * zip
        * take
        *
        * // reduce operators
        * collect
        * reduce, fold
        * toList, toSet
        * first
        *
        * // 実行contextの変更
        * flowOn:
        *
        * // Dispatherの使い分け
        * Dispatcher.Default:
        *     - cpu consuming task
        * Dispatcher.Main:
        *     - (Android) UI
        * Dispatchers.IO:
        *     - blockingタスクをoffloadするためのdispatcher
        *     - 64またはcore数が上限
        *
        *
        * */

        fun numbers(): Flow<Int> = flow {
          try {
            emit(1)
            emit(2)
            println("this line won't execute")
            emit(3)
          } finally {
            println("finally pass here")
          }
        }

//        intFlow(4)
//            .filter {
//                println("Filter $it")
//                it % 2 == 0
//            }
//            .map {
//                println("Map $it")
//                "stringified $it"
//            }
//            .collect { println(it) }


        // NOTE: Dispatchers.Main は UI用. kotlinx-coroutines-android ないと使えない
        val newFixedThreadPool = Executors.newFixedThreadPool(10) {
            val t = Thread(it)
            t.name = "my custom"
            t
        }
        withContext(newFixedThreadPool.asCoroutineDispatcher()) {
            intFlow(4)
                .map {
                    // get data from user server...
                    delay(100)
                    dev.ishikawa.dd_coroutine.util.showDebug("Map")
                    "User(id=$it)"
                }
                .flowOn(Dispatchers.IO)
                .collect {
                    dev.ishikawa.dd_coroutine.util.showDebug("Collect")
                }
        }
        /*
     Map [54635]current at: id:  14 name: DefaultDispatcher-worker-1 @coroutine#2
 Collect [54639]current at: id:  18 name: my custom @coroutine#1
     Map [55247]current at: id:  14 name: DefaultDispatcher-worker-1 @coroutine#2
 Collect [55252]current at: id:  19 name: my custom @coroutine#1
     Map [55859]current at: id:  14 name: DefaultDispatcher-worker-1 @coroutine#2
 Collect [55860]current at: id:  20 name: my custom @coroutine#1
     Map [56469]current at: id:  14 name: DefaultDispatcher-worker-1 @coroutine#2
 Collect [56470]current at: id:  21 name: my custom @coroutine#1
     Map [57078]current at: id:  14 name: DefaultDispatcher-worker-1 @coroutine#2
 Collect [57079]current at: id:  22 name: my custom @coroutine#1
        * */


//        t1 = DefaultDispatcher-worker-1 @coroutine#4
//        t2 = DefaultDispatcher-worker-1 @coroutine#4
//        t3 = DefaultDispatcher-worker-1 @coroutine#4
//        flow {
//            println("t1 = ${Thread.currentThread().name}")
//            for(i in 0..10) {
//                delay(100)
//                println("t2 = ${Thread.currentThread().name}")
//                emit(1)
//            }
//        }.onEach {
//            println("t3 = ${Thread.currentThread().name}")
//        }.flowOn(Dispatchers.Default
//        ).onEach {
//            // t4だけt4 = main @coroutine#1
//            println("t4 = ${Thread.currentThread().name}")
//        }.collect {  }



//        example1()
//        example2()
//        example3()
//        example4()
//        example5()
    }
}

fun intFlow(n: Int): Flow<Int> = flow {
    for (i in 0..n) {
        delay(500)
        emit(i)
    }
}

fun slowPublisher(n: Int, publishPace: Long = 600): Flow<Int> = flow {
    for (i in 0..n) {
        delay(publishPace)
        println("published $i at ${Thread.currentThread().name}")
        emit(i)
    }
}

suspend fun slowConsumer(consumePace: Long = 300): suspend (Int) -> Unit {
    return { v:Int ->
        delay(consumePace)
        println("consumed $v at ${Thread.currentThread().name}")
    }
}


suspend fun example1() {
    println("======= example1")
    // consumerの方が早い
    // 600 * 10 = 6sかかるはず
    measureTimeMillis {
        println("start pub/sub")
        slowPublisher(10).collect(slowConsumer())
    }.let {
        // time = 10001
        println("time = $it")
    }
}

/*
* ======= example1
start pub/sub
published 0 at main @coroutine#1
consumed 0 at main @coroutine#1
published 1 at main @coroutine#1
consumed 1 at main @coroutine#1
published 2 at main @coroutine#1
consumed 2 at main @coroutine#1
published 3 at main @coroutine#1
consumed 3 at main @coroutine#1
published 4 at main @coroutine#1
consumed 4 at main @coroutine#1
published 5 at main @coroutine#1
consumed 5 at main @coroutine#1
published 6 at main @coroutine#1
consumed 6 at main @coroutine#1
published 7 at main @coroutine#1
consumed 7 at main @coroutine#1
published 8 at main @coroutine#1
consumed 8 at main @coroutine#1
published 9 at main @coroutine#1
consumed 9 at main @coroutine#1
published 10 at main @coroutine#1
consumed 10 at main @coroutine#1
time = 10033
* */

suspend fun example2() {
    println("======= example2")
    // consumerの方が早い
    // 600 * 10 = 6sかかるはず
    measureTimeMillis {
        println("start pub/sub")
        slowPublisher(10, 300).collect(slowConsumer(600))
    }.let {
        // time = 10001
        println("time = $it")
    }
}
/*
*======= example2
start pub/sub
published 0 at main @coroutine#1
consumed 0 at main @coroutine#1
published 1 at main @coroutine#1
consumed 1 at main @coroutine#1
published 2 at main @coroutine#1
consumed 2 at main @coroutine#1
published 3 at main @coroutine#1
consumed 3 at main @coroutine#1
published 4 at main @coroutine#1
consumed 4 at main @coroutine#1
published 5 at main @coroutine#1
consumed 5 at main @coroutine#1
published 6 at main @coroutine#1
consumed 6 at main @coroutine#1
published 7 at main @coroutine#1
consumed 7 at main @coroutine#1
published 8 at main @coroutine#1
consumed 8 at main @coroutine#1
published 9 at main @coroutine#1
consumed 9 at main @coroutine#1
published 10 at main @coroutine#1
consumed 10 at main @coroutine#1
time = 9980
* */

suspend fun example3() {
    println("======= example3")
    // consumerの方が早い
    // 600 * 10 = 6sかかるはず
    measureTimeMillis {
        println("start pub/sub")
        slowPublisher(10, 300)
            .flowOn(Dispatchers.Default)
            .collect(slowConsumer(600))
    }.let {
        // time = 10001
        println("time = $it")
    }
}
/*
* ======= example3
start pub/sub
published 0 at DefaultDispatcher-worker-1 @coroutine#2
published 1 at DefaultDispatcher-worker-1 @coroutine#2
consumed 0 at main @coroutine#1
published 2 at DefaultDispatcher-worker-1 @coroutine#2
published 3 at DefaultDispatcher-worker-1 @coroutine#2
consumed 1 at main @coroutine#1
published 4 at DefaultDispatcher-worker-1 @coroutine#2
published 5 at DefaultDispatcher-worker-1 @coroutine#2
consumed 2 at main @coroutine#1
published 6 at DefaultDispatcher-worker-1 @coroutine#2
published 7 at DefaultDispatcher-worker-1 @coroutine#2
consumed 3 at main @coroutine#1
published 8 at DefaultDispatcher-worker-1 @coroutine#2
published 9 at DefaultDispatcher-worker-1 @coroutine#2
consumed 4 at main @coroutine#1
published 10 at DefaultDispatcher-worker-1 @coroutine#2
consumed 5 at main @coroutine#1
consumed 6 at main @coroutine#1
consumed 7 at main @coroutine#1
consumed 8 at main @coroutine#1
consumed 9 at main @coroutine#1
consumed 10 at main @coroutine#1
time = 6990
* */

suspend fun example4() {
    println("======= example4")
    // consumerの方が早い
    // 600 * 10 = 6sかかるはず
    measureTimeMillis {
        println("start pub/sub")
        slowPublisher(10, 300)
            .buffer() // publisher側が待たなくなる?
            .collect(slowConsumer(600))
    }.let {
        // time = 10001
        println("time = $it")
    }
}
/*
*======= example4
start pub/sub
published 0 at main @coroutine#3
published 1 at main @coroutine#3
consumed 0 at main @coroutine#1
published 2 at main @coroutine#3
published 3 at main @coroutine#3
consumed 1 at main @coroutine#1
published 4 at main @coroutine#3
published 5 at main @coroutine#3
consumed 2 at main @coroutine#1
published 6 at main @coroutine#3
published 7 at main @coroutine#3
consumed 3 at main @coroutine#1
published 8 at main @coroutine#3
published 9 at main @coroutine#3
consumed 4 at main @coroutine#1
published 10 at main @coroutine#3
consumed 5 at main @coroutine#1
consumed 6 at main @coroutine#1
consumed 7 at main @coroutine#1
consumed 8 at main @coroutine#1
consumed 9 at main @coroutine#1
consumed 10 at main @coroutine#1
time = 6947
* */

suspend fun example5() {
    println("======= example5")
    // consumerの方が早い
    // 600 * 10 = 6sかかるはず
    measureTimeMillis {
        println("start pub/sub")
        slowPublisher(10, 300)
            .conflate() // 一つにまとめる、の意
            .collect(slowConsumer(600))
    }.let {
        // time = 10001
        println("time = $it")
    }
}
/*
* ======= example5
start pub/sub
published 0 at main @coroutine#2
published 1 at main @coroutine#2
consumed 0 at main @coroutine#1
published 2 at main @coroutine#2
published 3 at main @coroutine#2
consumed 1 at main @coroutine#1
published 4 at main @coroutine#2
published 5 at main @coroutine#2
consumed 3 at main @coroutine#1
published 6 at main @coroutine#2
published 7 at main @coroutine#2
consumed 5 at main @coroutine#1
published 8 at main @coroutine#2
published 9 at main @coroutine#2
consumed 7 at main @coroutine#1
published 10 at main @coroutine#2
consumed 9 at main @coroutine#1    // 8をskipしている. 途中の値をskipして自分が取れる"最新"の値だけをみる
consumed 10 at main @coroutine#1
time = 4623

* */


/*
* combine
* zip
* flatmapconcat
*
*
* exception
*
* */