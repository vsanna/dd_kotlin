package dev.ishikawa.dd_coroutine

import dev.ishikawa.dd_coroutine.mockapiio.*
import dev.ishikawa.dd_coroutine.service.Service
import dev.ishikawa.dd_kotlin._5coroutine.intFlow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis
import java.util.concurrent.ConcurrentHashMap

fun main() {
//    test()
//    sequentialThread()
//    concurrentThread()
//    sequentialCoroutine()
//    concurrentCoroutine()
//    concurrentCoroutine2()
//    concurrentCoroutine3()
//    concurrentCoroutine4()
//    concurrentCoroutine5()
    concurrentCoroutine6()
}

private fun test() {
    println(Dispatchers.Main)
    println(Dispatchers.Default)
    val service = Service()
    val postCommentMap = mutableMapOf<Long, Pair<Post, List<Comment>>>()
    var user: User
    var measureTimeMillis = measureTimeMillis {
        user = service.getUser(userId = 1)
    }
    // 1000
    println("test: result1 = $measureTimeMillis")

    val posts: List<Post>
    measureTimeMillis = measureTimeMillis {
        posts = service.getPosts(userId = user.id)
    }
    // 1000
    println("test: result2 = $measureTimeMillis")

    measureTimeMillis = measureTimeMillis {
        posts.forEach {
            val comments = service.getComments(postId = it.id)
            postCommentMap.put(it.id, Pair(it, comments))
        }
    }
    // 5000
    println("test: result3 = $measureTimeMillis")
}

/*
[60122]current at: id:   1 name: main
[61321]current at: id:   1 name: main
[62340]current at: id:   1 name: main
[63345]current at: id:   1 name: main
[64350]current at: id:   1 name: main
[65356]current at: id:   1 name: main
[66362]current at: id:   1 name: main
sequential/thread: size = 5 result = 7259
* */
private fun sequentialThread() {
    val service = Service()
    val postCommentMap = mutableMapOf<Long, Pair<Post, List<Comment>>>()
    val measureTimeMillis = measureTimeMillis {
        val user = service.getUser(userId = 1)
        val posts = service.getPosts(userId = user.id)
        posts.forEach {
            val comments = service.getComments(postId = it.id)
            postCommentMap.put(it.id, Pair(it, comments))
        }
    }
    // sequential/thread: size = 5 result = 7265
    // 1000 + 1000 + 1000 * 5
    println("sequential/thread: size = ${postCommentMap.size} result = $measureTimeMillis")
}


/*
[12320]current at: id:   1 name: main
[13584]current at: id:   1 name: main
[14603]current at: id:  16 name: Thread-3
[14603]current at: id:  14 name: Thread-1
[14603]current at: id:  17 name: Thread-4
[14603]current at: id:  15 name: Thread-2
[14608]current at: id:  18 name: Thread-5
* */
private fun concurrentThread() {
    val service = Service()
    val postCommentMap = ConcurrentHashMap<Long, Pair<Post, List<Comment>>>()
    val measureTimeMillis = measureTimeMillis {
        val user = service.getUser(userId = 1)
        val posts = service.getPosts(userId = user.id)
        val threads = mutableListOf<Thread>()
        posts.forEach {
            val t = thread {
                val comments = service.getComments(postId = it.id)
                postCommentMap[it.id] = Pair(it, comments)
            }
            threads.add(t)
        }
        threads.forEach { it.join() }
    }
    // 1000 + 1000 + 1000(concurrent) = 3000
    // concurrent/thread: size = 5 result = 3317
    println("concurrent/thread: size = ${postCommentMap.size} result = $measureTimeMillis")
}


/*
[17646]current at: id:   1 name: main @coroutine#1
[18837]current at: id:   1 name: main @coroutine#1
[19857]current at: id:   1 name: main @coroutine#1
[20862]current at: id:   1 name: main @coroutine#1
[21867]current at: id:   1 name: main @coroutine#1
[22872]current at: id:   1 name: main @coroutine#1
[23878]current at: id:   1 name: main @coroutine#1
sequential/coroutine: size = 5 result = 7250
* */
private fun sequentialCoroutine() {
    runBlocking {
        val service = Service()
        val postCommentMap = ConcurrentHashMap<Long, Pair<Post, List<Comment>>>()
        val measureTimeMillis = measureTimeMillis {
            val user = service.getUserSus(userId = 1)
            val posts = service.getPostsSus(userId = user.id)
            posts.forEach {
                val comments = service.getCommentsSus(postId = it.id)
                postCommentMap[it.id] = Pair(it, comments)
            }
        }
        // 1000 + 1000 + 1000 * 5 = 7000
        // sequential/coroutine: size = 5 result = 7250
        println("sequential/coroutine: size = ${postCommentMap.size} result = $measureTimeMillis")
    }
}

/*
[2179]current at: id:   1 name: main @coroutine#1
[3383]current at: id:   1 name: main @coroutine#1
[4423]current at: id:   1 name: main @coroutine#2
[5431]current at: id:   1 name: main @coroutine#3
[6434]current at: id:   1 name: main @coroutine#4
[7438]current at: id:   1 name: main @coroutine#5
[8441]current at: id:   1 name: main @coroutine#6
sequential/coroutine: size = 5 result = 7278
* */
private fun concurrentCoroutine() {
    runBlocking {
        val service = Service()
        val postCommentMap = ConcurrentHashMap<Long, Pair<Post, List<Comment>>>()
        val measureTimeMillis = measureTimeMillis {
            val user = service.getUserSus(userId = 1)
            val posts = service.getPostsSus(userId = user.id)
            val coroutines = mutableListOf<Job>()
            posts.forEach {
                val job = launch {
                    val comments = service.getCommentsSus(postId = it.id)
                    postCommentMap[it.id] = Pair(it, comments)
                }
                coroutines.add(job)
            }

            coroutines.forEach {it.join()}
        }
        // 1000 + 1000 + 1000 = 7000
        // sequential/coroutine: size = 5 result = 7250
        println("sequential/coroutine: size = ${postCommentMap.size} result = $measureTimeMillis")
    }
}

/*
[18657]current at: id:   1 name: main @coroutine#1
[19827]current at: id:   1 name: main @coroutine#1
[20851]current at: id:   1 name: main @coroutine#2
[21857]current at: id:   1 name: main @coroutine#3
[22857]current at: id:   1 name: main @coroutine#4
[23862]current at: id:   1 name: main @coroutine#5
[24867]current at: id:   1 name: main @coroutine#6
sequential/coroutine: size = 5 result = 7257
* */
private fun concurrentCoroutine2() {
    runBlocking {
        val service = Service()
        val postCommentMap = ConcurrentHashMap<Long, Pair<Post, List<Comment>>>()
        val measureTimeMillis = measureTimeMillis {
            val user = service.getUserSus(userId = 1)
            val posts = service.getPostsSus(userId = user.id)
            val coroutines = mutableListOf<Deferred<Unit>>()
            posts.forEach {
                // 同一thread別coroutineだと早くならない?
                // TODO: why this is slow
                val job = async {
                    val comments = service.getCommentsSus(postId = it.id)
                    postCommentMap[it.id] = Pair(it, comments)
                }
                coroutines.add(job)
            }

            coroutines.forEach { it.await() }
        }
        // 1000 + 1000 + 1000*5 = 7000
        // sequential/coroutine: size = 5 result = 7257
        println("sequential/coroutine: size = ${postCommentMap.size} result = $measureTimeMillis")
    }
}

/*
[4080]current at: id:  13 name: DefaultDispatcher-worker-1 @coroutine#1
[5241]current at: id:  13 name: DefaultDispatcher-worker-1 @coroutine#1
[6261]current at: id:  14 name: DefaultDispatcher-worker-2 @coroutine#3
[6261]current at: id:  17 name: DefaultDispatcher-worker-4 @coroutine#4
[6261]current at: id:  15 name: DefaultDispatcher-worker-3 @coroutine#2
[6261]current at: id:  20 name: DefaultDispatcher-worker-7 @coroutine#6
[6261]current at: id:  19 name: DefaultDispatcher-worker-6 @coroutine#5
sequential/coroutine: size = 5 result = 3216
* */
private fun concurrentCoroutine3() {
    runBlocking(Dispatchers.IO) {
        val service = Service()
        val postCommentMap = ConcurrentHashMap<Long, Pair<Post, List<Comment>>>()
        val measureTimeMillis = measureTimeMillis {
            val user = service.getUserSus(userId = 1)
            val posts = service.getPostsSus(userId = user.id)
            val coroutines = mutableListOf<Deferred<Unit>>()
            posts.forEach {
                // それぞれ別thread
                val job = async {
                    val comments = service.getCommentsSus(postId = it.id)
                    postCommentMap[it.id] = Pair(it, comments)
                }
                coroutines.add(job)
            }
            coroutines.forEach { it.await() }
        }
        // 1000 + 1000 + 1000 = 3000
        // sequential/coroutine: size = 5 result = 3216
        println("sequential/coroutine: size = ${postCommentMap.size} result = $measureTimeMillis")
    }
}

/*
[11587]current at: id:   1 name: main @coroutine#1
[12759]current at: id:   1 name: main @coroutine#1
[13787]current at: id:   1 name: main @coroutine#2
[14794]current at: id:   1 name: main @coroutine#3
[15795]current at: id:   1 name: main @coroutine#4
[16800]current at: id:   1 name: main @coroutine#5
[17806]current at: id:   1 name: main @coroutine#6
[18809]current at: id:   1 name: main @coroutine#7
[19814]current at: id:   1 name: main @coroutine#8
[20816]current at: id:   1 name: main @coroutine#9
[21819]current at: id:   1 name: main @coroutine#10
[22822]current at: id:   1 name: main @coroutine#11
[23827]current at: id:   1 name: main @coroutine#12
[24833]current at: id:   1 name: main @coroutine#13
[25838]current at: id:   1 name: main @coroutine#14
[26842]current at: id:   1 name: main @coroutine#15
[27843]current at: id:   1 name: main @coroutine#16
[28848]current at: id:   1 name: main @coroutine#17
[29852]current at: id:   1 name: main @coroutine#18
[30858]current at: id:   1 name: main @coroutine#19
[31862]current at: id:   1 name: main @coroutine#20
[32867]current at: id:   1 name: main @coroutine#21
sequential/coroutine: size = 19 result = 22318
* */
private fun concurrentCoroutine4() {
    runBlocking {
        val service = Service()
        val postCommentMap = ConcurrentHashMap<Long, Pair<Post, List<Comment>>>()
        val measureTimeMillis = measureTimeMillis {
            val user = service.getUserSus(userId = 1)
            val posts = service.getPostsSus(num = 20, userId = user.id)
            val coroutines = mutableListOf<Deferred<Unit>>()
            posts.forEach {
                // 同一thread別coroutineだと早くならない?
                // async: Coroutine context is inherited from a [CoroutineScope]
                // If the context does not have any dispatcher nor any other [ContinuationInterceptor], then [Dispatchers.Default] is used.
                val job = async {
                    val comments = service.getCommentsSus(postId = it.id)
                    postCommentMap[it.id] = Pair(it, comments)
                }
                coroutines.add(job)
            }

            coroutines.forEach { it.await() }
        }
        // 1000 + 1000 + 1000*20 = 22000
        // sequential/coroutine: size = 19 result = 22318
        println("sequential/coroutine: size = ${postCommentMap.size} result = $measureTimeMillis")
    }
}

/*
[70648]current at: id:  13 name: DefaultDispatcher-worker-1 @coroutine#1
[71856]current at: id:  13 name: DefaultDispatcher-worker-1 @coroutine#1
[72878]current at: id:  15 name: DefaultDispatcher-worker-3 @coroutine#2
[72883]current at: id:  17 name: DefaultDispatcher-worker-4 @coroutine#4
[72883]current at: id:  20 name: DefaultDispatcher-worker-7 @coroutine#6
[72883]current at: id:  14 name: DefaultDispatcher-worker-2 @coroutine#3
[72885]current at: id:  18 name: DefaultDispatcher-worker-5 @coroutine#5
[72885]current at: id:  19 name: DefaultDispatcher-worker-6 @coroutine#7
[72885]current at: id:  22 name: DefaultDispatcher-worker-9 @coroutine#9
[72885]current at: id:  21 name: DefaultDispatcher-worker-8 @coroutine#8
[72887]current at: id:  23 name: DefaultDispatcher-worker-10 @coroutine#10
[72888]current at: id:  25 name: DefaultDispatcher-worker-12 @coroutine#14
[72888]current at: id:  24 name: DefaultDispatcher-worker-11 @coroutine#12
[72888]current at: id:  26 name: DefaultDispatcher-worker-13 @coroutine#11
[72888]current at: id:  27 name: DefaultDispatcher-worker-14 @coroutine#13
[72889]current at: id:  28 name: DefaultDispatcher-worker-15 @coroutine#16
[72889]current at: id:  30 name: DefaultDispatcher-worker-17 @coroutine#17
[72889]current at: id:  29 name: DefaultDispatcher-worker-16 @coroutine#15
[72889]current at: id:  31 name: DefaultDispatcher-worker-18 @coroutine#19
[72889]current at: id:  32 name: DefaultDispatcher-worker-19 @coroutine#18
[72889]current at: id:  36 name: DefaultDispatcher-worker-23 @coroutine#20
[72889]current at: id:  35 name: DefaultDispatcher-worker-22 @coroutine#21
sequential/coroutine: size = 20 result = 3262
* */
private fun concurrentCoroutine5() {
    runBlocking(Dispatchers.IO) {
        val service = Service()
        val postCommentMap = ConcurrentHashMap<Long, Pair<Post, List<Comment>>>()
        val measureTimeMillis = measureTimeMillis {
            val user = service.getUserSus(userId = 1)
            val posts = service.getPostsSus(num = 20, userId = user.id)
            val coroutines = mutableListOf<Deferred<Unit>>()
            posts.forEach {
                // それぞれ別thread
                val job = async {
                    val comments = service.getCommentsSus(postId = it.id)
                    postCommentMap[it.id] = Pair(it, comments)
                }
                coroutines.add(job)
            }
            coroutines.forEach { it.await() }
        }
        // 1000 + 1000 + 1000 = 3000
        // sequential/coroutine: size = 5 result = 3216
        println("sequential/coroutine: size = ${postCommentMap.size} result = $measureTimeMillis")
    }
}

/*
[81481]current at: id:   1 name: main @coroutine#1
[82681]current at: id:   1 name: main @coroutine#1
sequential/coroutine: size = 0 result = 2233  <<<<< 先にでる
[83714]current at: id:  14 name: concurrent async @coroutine#3
[84719]current at: id:  14 name: concurrent async @coroutine#4
[85724]current at: id:  14 name: concurrent async @coroutine#5
[86725]current at: id:  14 name: concurrent async @coroutine#6
[87726]current at: id:  14 name: concurrent async @coroutine#7
[88728]current at: id:  14 name: concurrent async @coroutine#8
[89732]current at: id:  14 name: concurrent async @coroutine#9
[90737]current at: id:  14 name: concurrent async @coroutine#10
[91740]current at: id:  14 name: concurrent async @coroutine#11
[92745]current at: id:  14 name: concurrent async @coroutine#12
[93751]current at: id:  14 name: concurrent async @coroutine#13
[94754]current at: id:  14 name: concurrent async @coroutine#14
[95758]current at: id:  14 name: concurrent async @coroutine#15
[96761]current at: id:  14 name: concurrent async @coroutine#16
[97765]current at: id:  14 name: concurrent async @coroutine#17
[98766]current at: id:  14 name: concurrent async @coroutine#18
[99768]current at: id:  14 name: concurrent async @coroutine#19
[772]current at: id:  14 name: concurrent async @coroutine#20
[1776]current at: id:  14 name: concurrent async @coroutine#21
[2777]current at: id:  14 name: concurrent async @coroutine#22
* */
@OptIn(ObsoleteCoroutinesApi::class)
private fun concurrentCoroutine6() {
    val myContext = newSingleThreadContext("concurrent async")
    // runBlockingはlaunchの終わりを待つが、asyncの終わりは待たない
    runBlocking {
        val service = Service()
        val postCommentMap = ConcurrentHashMap<Long, Pair<Post, List<Comment>>>()
        val measureTimeMillis = measureTimeMillis {
            val user = service.getUserSus(userId = 1)
            val posts = service.getPostsSus(num = 20, userId = user.id)
            val coroutines = mutableListOf<Deferred<Unit>>()
            // 上の処理とは別threadだがgetCommentsSusのcoroutineは全て同一thread内
            launch(context = myContext) {
                posts.forEach {
                    // それぞれ別thread
                    val job = async {
                        val comments = service.getCommentsSus(postId = it.id)
                        postCommentMap[it.id] = Pair(it, comments)
                    }
                    coroutines.add(job)
                }
                coroutines.forEach { it.await() }
            }
        }
        // 1000 + 1000 + 1000 = 3000
        // sequential/coroutine: size = 5 result = 3216
        println("sequential/coroutine: size = ${postCommentMap.size} result = $measureTimeMillis")
    }
}

