package dev.ishikawa.dd_coroutine

import dev.ishikawa.dd_coroutine.mockapiio.*
import dev.ishikawa.dd_coroutine.service.Service
import dev.ishikawa.dd_coroutine.util.showDebug
import dev.ishikawa.dd_kotlin._5coroutine.intFlow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis
import java.util.concurrent.ConcurrentHashMap

fun main() {
//    standardFlow()
//    standardFlowWithFlatmap()
//    standardOnFlow()
//    sequentialFlow()
//    sequentialFlowWithDefferentContext()
//    sequentialFlowWithOnFlow()
//    sequentialFlowWithoutBuffer2()
//    sequentialFlowWithBuffer2()
//    zipFlow()
    combineFlow()
}


/*
* producer > consumer のケース
* buffer
* */

/*
[31171]current at: id:   1 name: main @coroutine#1
[32361]current at: id:   1 name: main @coroutine#1
[33384]current at: id:   1 name: main @coroutine#1
[34389]current at: id:   1 name: main @coroutine#1
[35393]current at: id:   1 name: main @coroutine#1
[36398]current at: id:   1 name: main @coroutine#1
[37404]current at: id:   1 name: main @coroutine#1
sequential/thread: size = 0 result = 7249
* * */
private fun standardFlow() {
    runBlocking {
        val service = Service()

        val postCommentMap = mutableMapOf<Long, Pair<Post, List<Comment>>>()
        val measureTimeMillis = measureTimeMillis {
            // producer
            val user = service.getUser(userId = 1)
            val posts = service.getPosts(userId = user.id)
            val producer = flow {
                for (post in posts) {
                   emit(post)
                }
            } // == posts.asFlow()

            // consumer
            val consumer: suspend (Post) -> Unit = { it ->
                service.getComments(postId = it.id)
            }

            producer
                .onEach { println(it) }
                .collect(consumer)
        }
        println("sequential/thread: size = ${postCommentMap.size} result = $measureTimeMillis")
    }
}

/*
[7183]current at: id:   1 name: main @coroutine#1
[8365]current at: id:   1 name: main @coroutine#1
[9411]current at: id:   1 name: main @coroutine#1
[10413]current at: id:   1 name: main @coroutine#1
[11418]current at: id:   1 name: main @coroutine#1
[12420]current at: id:   1 name: main @coroutine#1
[13426]current at: id:   1 name: main @coroutine#1
[14429]current at: id:   1 name: main @coroutine#1
[15432]current at: id:   1 name: main @coroutine#1
[16434]current at: id:   1 name: main @coroutine#1
[17440]current at: id:   1 name: main @coroutine#1
[18445]current at: id:   1 name: main @coroutine#1
sequential/thread: size = 0 result = 12282
* */
private fun standardFlowWithFlatmap() {
    runBlocking {
        val service = Service()

        val postCommentMap = mutableMapOf<Long, Pair<Post, List<Comment>>>()
        val measureTimeMillis = measureTimeMillis {
            // producer
            val user = service.getUser(userId = 1)
            val posts = service.getPosts(userId = user.id)

            // consumer
            val consumer: (Post) -> List<Comment> = { it ->
                service.getComments(postId = it.id)
            }

            posts.asFlow()
                .flatMapConcat { listOf(it, it).asFlow() }
                .collect {
                    val comments = service.getCommentsSus(postId = it.id)
                    // do nothing
//                    println(comments)
                }
        }
        println("sequential/thread: size = ${postCommentMap.size} result = $measureTimeMillis")
    }
}

/*
 [5929]current at: id:   1 name: main @coroutine#1
 [7111]current at: id:   1 name: main @coroutine#1
[0] [7161]current at: id:  15 name: DefaultDispatcher-worker-2 @coroutine#3
[1] [7161]current at: id:  15 name: DefaultDispatcher-worker-2 @coroutine#3
[0] [7161]current at: id:  15 name: DefaultDispatcher-worker-2 @coroutine#3
[1] [7161]current at: id:  15 name: DefaultDispatcher-worker-2 @coroutine#3
[0] [7161]current at: id:  15 name: DefaultDispatcher-worker-2 @coroutine#3
[1] [7162]current at: id:  15 name: DefaultDispatcher-worker-2 @coroutine#3
[0] [7162]current at: id:  15 name: DefaultDispatcher-worker-2 @coroutine#3
[1] [7162]current at: id:  15 name: DefaultDispatcher-worker-2 @coroutine#3
[0] [7162]current at: id:  15 name: DefaultDispatcher-worker-2 @coroutine#3
[1] [7162]current at: id:  15 name: DefaultDispatcher-worker-2 @coroutine#3

[2] [7167]current at: id:  14 name: DefaultDispatcher-worker-1 @coroutine#2
[3] [7167]current at: id:  14 name: DefaultDispatcher-worker-1 @coroutine#2
[2] [7167]current at: id:  14 name: DefaultDispatcher-worker-1 @coroutine#2
[4] [7167]current at: id:   1 name: main @coroutine#1
[3] [7167]current at: id:  14 name: DefaultDispatcher-worker-1 @coroutine#2
[5] [7167]current at: id:   1 name: main @coroutine#1
[2] [7168]current at: id:  14 name: DefaultDispatcher-worker-1 @coroutine#2
[4] [7168]current at: id:   1 name: main @coroutine#1
[3] [7168]current at: id:  14 name: DefaultDispatcher-worker-1 @coroutine#2
[5] [7168]current at: id:   1 name: main @coroutine#1
[2] [7168]current at: id:  14 name: DefaultDispatcher-worker-1 @coroutine#2
[3] [7168]current at: id:  14 name: DefaultDispatcher-worker-1 @coroutine#2
[4] [7168]current at: id:   1 name: main @coroutine#1
[2] [7168]current at: id:  14 name: DefaultDispatcher-worker-1 @coroutine#2
[5] [7168]current at: id:   1 name: main @coroutine#1
[3] [7168]current at: id:  14 name: DefaultDispatcher-worker-1 @coroutine#2
[4] [7168]current at: id:   1 name: main @coroutine#1
[5] [7168]current at: id:   1 name: main @coroutine#1
[4] [7168]current at: id:   1 name: main @coroutine#1
[5] [7168]current at: id:   1 name: main @coroutine#1
sequential/thread: size = 0 result = 2284
* */
private fun standardOnFlow() {
    runBlocking {
        val service = Service()

        val postCommentMap = mutableMapOf<Long, Pair<Post, List<Comment>>>()
        val measureTimeMillis = measureTimeMillis {
            // producer
            val user = service.getUser(userId = 1)
            val posts = service.getPosts(userId = user.id)

            // consumer
            val consumer: (Post) -> List<Comment> = { it ->
                service.getComments(postId = it.id)
            }

            /*
            * subscribeOn -> flowOn
            * observeOn/publishOn -> withContext
            *
            * flowOn affects upstream
            * */
            posts.asFlow()
                .onEach {
                    showDebug("[0]")
                }
                .onEach {
                    showDebug("[1]")
                }
                .flowOn(Dispatchers.IO)
                .onEach {
                    showDebug("[2]")
                }
                .onEach {
                    showDebug("[3]")
                }
                .flowOn(Dispatchers.Default)
                .onEach {
                    showDebug("[4]")
                }
                .onEach {
                    showDebug("[5]")
                }
                .collect {
                    // do nothing
                    println(it.id)
                }
        }
        println("sequential/thread: size = ${postCommentMap.size} result = $measureTimeMillis")
    }
}

/*
Filter 0
Map 0
stringified 0
Filter 1
Filter 2
Map 2
stringified 2
Filter 3
Filter 4
Map 4
stringified 4
sequential/thread: result = 2565
* */
private fun sequentialFlow() {
    runBlocking {
        val measureTimeMillis = measureTimeMillis {
            intFlow(4)
                .filter {
                    println("Filter $it")
                    it % 2 == 0
                }
                .map {
                    println("Map $it")
                    "stringified $it"
                }
                .collect { println(it) }
        }
        println("sequential/thread: result = $measureTimeMillis")
    }
}

/*
                 Filter 0 [38763]current at: id:   1 name: main @coroutine#2
                    Map 0 [38767]current at: id:   1 name: main @coroutine#1
    Collect stringified 0 [38768]current at: id:   1 name: main @coroutine#1
                 Filter 1 [39271]current at: id:   1 name: main @coroutine#2
                 Filter 2 [39776]current at: id:   1 name: main @coroutine#2
                    Map 2 [39777]current at: id:   1 name: main @coroutine#1
    Collect stringified 2 [39777]current at: id:   1 name: main @coroutine#1
                 Filter 3 [40282]current at: id:   1 name: main @coroutine#2
                 Filter 4 [40790]current at: id:   1 name: main @coroutine#2
                    Map 4 [40793]current at: id:   1 name: main @coroutine#1
    Collect stringified 4 [40793]current at: id:   1 name: main @coroutine#1
sequential/thread: result = 2624
* */
private fun sequentialFlowWithBuffer() {
    runBlocking {
        val measureTimeMillis = measureTimeMillis {
            intFlow(4)
                .filter {
                    showDebug("Filter $it", 25)
                    it % 2 == 0
                }.buffer(1000) // かわらなー
                .map {
                    showDebug("Map $it", 25)
                    "stringified $it"
                }
                .collect {
                    showDebug("Collect $it", 25)
                }
        }
        println("sequential/thread: result = $measureTimeMillis")
    }
}

/*
    Emit 0 [72816]current at: id:   1 name: main @coroutine#1
 Collect 0 [73128]current at: id:   1 name: main @coroutine#1
    Emit 1 [73234]current at: id:   1 name: main @coroutine#1
 Collect 1 [73540]current at: id:   1 name: main @coroutine#1
    Emit 2 [73645]current at: id:   1 name: main @coroutine#1
 Collect 2 [73951]current at: id:   1 name: main @coroutine#1
    Emit 3 [74057]current at: id:   1 name: main @coroutine#1
 Collect 3 [74362]current at: id:   1 name: main @coroutine#1
sequential/thread: result = 1692
(100 + 300) * 4
* */
private fun sequentialFlowWithoutBuffer2() {
    runBlocking {
        val measureTimeMillis = measureTimeMillis {
            flow { repeat(4) {
                delay(100)
                showDebug("Emit $it", 10)
                emit(it)
            } }.collect {
                delay(300)
                showDebug("Collect $it", 10)
            }
        }
        println("sequential/thread: result = $measureTimeMillis")
    }
}

/*
    Emit 0 [27371]current at: id:   1 name: main @coroutine#2
    Emit 1 [27482]current at: id:   1 name: main @coroutine#2
    Emit 2 [27587]current at: id:   1 name: main @coroutine#2
 Collect 0 [27683]current at: id:   1 name: main @coroutine#1
    Emit 3 [27688]current at: id:   1 name: main @coroutine#2
 Collect 1 [27990]current at: id:   1 name: main @coroutine#1
 Collect 2 [28295]current at: id:   1 name: main @coroutine#1
 Collect 3 [28598]current at: id:   1 name: main @coroutine#1
sequential/thread: result = 1408
100(wait for an initial value is emitted) + 300 * 4
* */
private fun sequentialFlowWithBuffer2() {
    runBlocking {
        val measureTimeMillis = measureTimeMillis {
            flow { repeat(4) {
                delay(100)
                showDebug("Emit $it", 10)
                emit(it)
            } }
                .buffer()
                .collect {
                    delay(300)
                    showDebug("Collect $it", 10)
                }
        }
        println("sequential/thread: result = $measureTimeMillis")
    }
}

/*
                 Filter 0 [7860]current at: id:  13 name: DefaultDispatcher-worker-1 @coroutine#1
                    Map 0 [7862]current at: id:  13 name: DefaultDispatcher-worker-1 @coroutine#1
    Collect stringified 0 [7863]current at: id:  13 name: DefaultDispatcher-worker-1 @coroutine#1
                 Filter 1 [8366]current at: id:  13 name: DefaultDispatcher-worker-1 @coroutine#1
                 Filter 2 [8870]current at: id:  13 name: DefaultDispatcher-worker-1 @coroutine#1
                    Map 2 [8871]current at: id:  13 name: DefaultDispatcher-worker-1 @coroutine#1
    Collect stringified 2 [8871]current at: id:  13 name: DefaultDispatcher-worker-1 @coroutine#1
                 Filter 3 [9376]current at: id:  13 name: DefaultDispatcher-worker-1 @coroutine#1
                 Filter 4 [9881]current at: id:  13 name: DefaultDispatcher-worker-1 @coroutine#1
                    Map 4 [9881]current at: id:  13 name: DefaultDispatcher-worker-1 @coroutine#1
    Collect stringified 4 [9882]current at: id:  13 name: DefaultDispatcher-worker-1 @coroutine#1
sequential/thread: result = 2610

* */
private fun sequentialFlowWithDefferentContext() {
    runBlocking {
        val measureTimeMillis = measureTimeMillis {
            // threadは変われど単一スレッド上での処理には変わらず
            withContext(Dispatchers.IO) {
                intFlow(4)
                    .filter {
                        showDebug("Filter $it", 25)
                        it % 2 == 0
                    }
                    .map {
                        showDebug("Map $it", 25)
                        "stringified $it"
                    }
                    .collect {
                        showDebug("Collect $it", 25)
                    }
            }
        }
        println("sequential/thread: result = $measureTimeMillis")
    }
}

/*
> Task :Usecase2Kt.main()
                 Filter 0 [12791]current at: id:  13 name: DefaultDispatcher-worker-1 @coroutine#2
                    Map 0 [12793]current at: id:  13 name: DefaultDispatcher-worker-1 @coroutine#2
    Collect stringified 0 [12796]current at: id:   1 name: main @coroutine#1
                 Filter 1 [13299]current at: id:  13 name: DefaultDispatcher-worker-1 @coroutine#2
                 Filter 2 [13805]current at: id:  13 name: DefaultDispatcher-worker-1 @coroutine#2
                    Map 2 [13805]current at: id:  13 name: DefaultDispatcher-worker-1 @coroutine#2
    Collect stringified 2 [13806]current at: id:   1 name: main @coroutine#1
                 Filter 3 [14309]current at: id:  13 name: DefaultDispatcher-worker-1 @coroutine#2
                 Filter 4 [14817]current at: id:  13 name: DefaultDispatcher-worker-1 @coroutine#2
                    Map 4 [14818]current at: id:  13 name: DefaultDispatcher-worker-1 @coroutine#2
    Collect stringified 4 [14818]current at: id:   1 name: main @coroutine#1
sequential/thread: result = 2623

* */
private fun sequentialFlowWithOnFlow() {
    runBlocking {
        val measureTimeMillis = measureTimeMillis {
            // threadは変われど単一スレッド上での処理には変わらず
            intFlow(4)
                .filter {
                    showDebug("Filter $it", 25)
                    it % 2 == 0
                }
                .map {
                    showDebug("Map $it", 25)
                    "stringified $it"
                }
                .flowOn(Dispatchers.IO)
                .collect {
                    showDebug("Collect $it", 25)
                }
        }
        println("sequential/thread: result = $measureTimeMillis")
    }
}


// buffer, conflate, lastCollect

// zip combine

private fun zipFlow() {
    runBlocking {
        val nums = (1..3).asFlow()
        // 余った要素は捨てられる
        val strs = flowOf("num", "of", "elements", "is", "different")
        nums.zip(strs) { a, b -> "$a -> $b"}
            .collect { println(it) }
    }
}

/*
1 -> num at 229
2 -> of at 439
3 -> elements at 645
* */
private fun combineFlowWithoutCombine() {
    runBlocking {
        val nums = (1..3).asFlow().onEach { delay(300) }
        // 余った要素は捨てられる
        val strs = flowOf("num", "of", "elements", "is", "different").onEach { delay(400) }
        val startTime = System.currentTimeMillis()
        nums.zip(strs) { a, b -> "$a -> $b" }.collect { println("$it at ${System.currentTimeMillis() - startTime}") }
    }
}

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


// flatMapMerge, flatMapConcat

