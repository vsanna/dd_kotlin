package dev.ishikawa.dd_coroutine.usecases

import io.ktor.util.Identity.decode
import kotlinx.coroutines.*
import kotlin.coroutines.EmptyCoroutineContext

/*
* scope/contextの理解
* - context
*     - ただの箱で、簡単に合成できる
*     - contextの作られるタイミング
*     - DispatcherとJobが大事
* - scopeの意義
*   - scopeが作られるタイミング
*   - scopeのnest
*   - Structured Concurrencyって?
*
*

*  */
fun main() {
    runBlocking {
        // test1
        /*
        *
1 BlockingCoroutine{Active}@5a8806ef
4 ScopeCoroutine{Active}@704a52ec
2 StandaloneCoroutine{Active}@34cd072c
5 StandaloneCoroutine{Active}@3043fe0e
3 DeferredCoroutine{Active}@bd8db5a
        * */
//        println("1 ${this as CoroutineScope}")
//        launch {
//            println("2 $this")
//            async {
//                println("3 $this")
//            }
//        }
//
//        coroutineScope {
//            println("4 $this")
//            launch {
//                println("5 $this")
//            }
//        }



        /*
        * test2
        * 表示されない
        * */
//        launch {
//            launch {
//                throw Exception("dummy")
//            }
//            delay(100)
//            println("not passing here")
//        }

        /*
        * test3
        * あれー、表示されない。ということはcoroutineScopeで区切れていない?
        * */
//        launch {
//            coroutineScope {
//                launch {
//                    throw Exception("dummy")
//                }
//            }
//            delay(100)
//            println("not passing here")
//        }

        /*
        * test4
        * あれー、だめ。表示されない。だめじゃん
        * つまり伝播を防ぐ目的では使えないのかー。
        * */

//        launch {
//            CoroutineScope(currentCoroutineContext()).launch {
//                throw Exception("dummy")
//            }
//            delay(100)
//            println("not passing here")
//        }

        /*
        * test5
        * これも当然だめ
        * */
//        launch {
//            CoroutineScope(currentCoroutineContext()).launch {
//                throw Exception("dummy")
//            }
//            CoroutineScope(currentCoroutineContext()).launch {
//                delay(100)
//                println("not passing here")
//            }
//        }

        /*
        * test6
        * ダメー. Dispatchers.Defaultをcontextにセットしてもだめだった。
        * */
//        CoroutineScope(currentCoroutineContext()).launch {
//            throw Exception("dummy")
//        }
//        CoroutineScope(currentCoroutineContext()).launch {
//            delay(100)
//            println("not passing here")
//        }

        /*
        * test7
        * これは表示される. やはりscopeを変えても伝播するんだな。
        * */
//        GlobalScope.launch {
//            throw Exception("dummy")
//        }
//        GlobalScope.launch {
//            delay(100)
//            println("passing here")
//        }
//        delay(150)


        /*
        * test8
        * scope.cancel内でcancelを試みるjobはやはりいちばん外側のこのjobっぽい
        * */
        // StandaloneCoroutine@1321
//        val job = launch {
//            val scope = CoroutineScope(currentCoroutineContext())
//            scope.launch {
//                println("passing here1")
//                launch {
//                    delay(10000)
//                }
//            }
//            // scopeと同じcontextを持つ...からcancel?
//            val launch = CoroutineScope(currentCoroutineContext()).launch {
//                delay(100)
//                println("passing here2") // ここも通らない
//            }
//            delay(50)
//            scope.cancel("force cancel")
//        }
//        val a = 1
//
        /*
        * test9
        * やはりlaunchもcancelされてしまう。 別Contextでもjobが同じだから?
        * */
//        val job = launch {
//            // this:  StandaloneCoroutine@1284 / context: CombinedContext@1283
//            // scope: ContextScope@1364        / context: CombinedContext@1377
//            val scope = CoroutineScope(currentCoroutineContext() + CoroutineName("another scope"))
//            scope.launch {
//                println("passing here1")
//                launch {
//                    delay(10000)
//                }
//            }
//            // scopeと同じcontextを持つ...からcancel?
//            val launch = CoroutineScope(currentCoroutineContext()).launch {
//                delay(100)
//                println("passing here2") // ここも通らない
//            }
//            delay(50)
//            scope.cancel("force cancel")
//        }
//        val a = 1

        /*
        * test10
        * passing here
        * cancelled 1
        * cancelled 2
        * cancelled 3
        *
        * scope1のcancelはjob1, その中のlaunchに伝播。なおcancelは下から行われる
        * */
//        val scope1 = CoroutineScope(CoroutineName("another scope"))
//        val job1 = scope1.launch {
//            launch {
//                delay(10000)
//            }.invokeOnCompletion { cause: Throwable? ->  if(cause is CancellationException) println("cancelled 1") }
//        }.invokeOnCompletion { cause: Throwable? ->  if(cause is CancellationException) println("cancelled 2") }
//        scope1.coroutineContext[Job]!!.invokeOnCompletion { cause: Throwable? ->  if(cause is CancellationException) println("cancelled 3") }
//
//        val scope2 = CoroutineScope(CoroutineName("another scope2"))
//        scope2.launch {
//            delay(100)
//            println("passing here")
//        }.invokeOnCompletion { cause: Throwable? ->  if(cause is CancellationException) println("cancelled 4") }
//        scope2.coroutineContext[Job]!!.invokeOnCompletion { cause: Throwable? ->  if(cause is CancellationException) println("cancelled 5") }
//
//        delay(1000)
//        scope1.cancel("force cancel")


        /*
        * test11
        * cancelled 1
        * cancelled 2
        * passing here
        *
        * scope1はcancelされていない
        * */

//        val scope1 = CoroutineScope(CoroutineName("another scope"))
//        val job1 = scope1.launch {
//            launch {
//                delay(10000)
//            }.invokeOnCompletion { cause: Throwable? ->  if(cause is CancellationException) println("cancelled 1") }
//        }
//        job1.invokeOnCompletion { cause: Throwable? ->  if(cause is CancellationException) println("cancelled 2") }
//        scope1.coroutineContext[Job]!!.invokeOnCompletion { cause: Throwable? ->  if(cause is CancellationException) println("cancelled 3") }
//
//        val scope2 = CoroutineScope(CoroutineName("another scope2"))
//        scope2.launch {
//            delay(100)
//            println("passing here")
//        }.invokeOnCompletion { cause: Throwable? ->  if(cause is CancellationException) println("cancelled 4") }
//        scope2.coroutineContext[Job]!!.invokeOnCompletion { cause: Throwable? ->  if(cause is CancellationException) println("cancelled 5") }
//
//        job1.cancel("force cancel")
//        delay(100)


        /*
        * test12
        * scope1.coroutineContext[Job]!!.children.contains(job1) = true
        * job1.children.contains(childJob1) = true
        * scope2.coroutineContext[Job]!!.children.contains(job2) = true
        * cancelled 1
        * cancelled 2
        * cancelled 3
        * */

//        val scope1 = CoroutineScope(CoroutineName("another scope"))
//        var childJob1: Job? = null
//        val job1 = scope1.launch {
//            childJob1 = launch {
//                delay(10000)
//            }
//            childJob1!!.invokeOnCompletion { cause: Throwable? ->  if(cause is CancellationException) println("cancelled 1") }
//        }
//        job1.invokeOnCompletion { cause: Throwable? ->  if(cause is CancellationException) println("cancelled 2") }
//        scope1.coroutineContext[Job]!!.invokeOnCompletion { cause: Throwable? ->  if(cause is CancellationException) println("cancelled 3") }
//
//        println(
//            "scope1.coroutineContext[Job]!!.children.contains(job1) = ${
//                scope1.coroutineContext[Job]!!.children.contains(job1)
//            }"
//        )
//
//        println("job1.children.contains(childJob1) = ${job1.children.contains(childJob1)}")
//
//        val scope2 = CoroutineScope(CoroutineName("another scope2"))
//        val job2 = scope2.launch {
//            delay(100)
//            println("passing here")
//        }
//        job2.invokeOnCompletion { cause: Throwable? ->  if(cause is CancellationException) println("cancelled 4") }
//        scope2.coroutineContext[Job]!!.invokeOnCompletion { cause: Throwable? ->  if(cause is CancellationException) println("cancelled 5") }
//
//        println(
//            "scope2.coroutineContext[Job]!!.children.contains(job2) = ${
//                scope2.coroutineContext[Job]!!.children.contains(job2)
//            }"
//        )
//
//        scope1.cancel("force cancel")
////        job1.cancel("force cancel")
//        delay(50)

        /*
        * test13
        * 仮に手作りのjobを渡すとそこで伝播が切れる
        *
        * scope1.coroutineContext[Job]!!.children.contains(job1) = false
        * job1.children.contains(childJob1) = false
        * manualJob.children.contains(childJob1) = true
        * scope2.coroutineContext[Job]!!.children.contains(job2) = true
        * cancelled 3
        * */

//        val scope1 = CoroutineScope(CoroutineName("another scope"))
//        var childJob1: Job? = null
//        val manualJob = Job() // specify parent job. since this job is not a child of scope1, cancelling of scope1 won't propagate to here
//        val job1 = scope1.launch {
//            childJob1 = launch(manualJob) {
//                delay(2000)
//                "here should not be passed indeed, but will be passed since this job won't get cancellation propagation"
//            }
//            childJob1!!.invokeOnCompletion { cause: Throwable? ->  if(cause is CancellationException) println("cancelled 1") }
//        }
//        job1.invokeOnCompletion { cause: Throwable? ->  if(cause is CancellationException) println("cancelled 2") }
//        scope1.coroutineContext[Job]!!.invokeOnCompletion { cause: Throwable? ->  if(cause is CancellationException) println("cancelled 3") }
//
//        println(
//            "scope1.coroutineContext[Job]!!.children.contains(job1) = ${
//                scope1.coroutineContext[Job]!!.children.contains(job1)
//            }"
//        )
//
//        println("job1.children.contains(childJob1) = ${job1.children.contains(childJob1)}")
//        println("manualJob.children.contains(childJob1) = ${manualJob.children.contains(childJob1)}")
//
//
//        val scope2 = CoroutineScope(CoroutineName("another scope2"))
//        val job2 = scope2.launch {
//            delay(100)
//            println("passing here")
//        }
//        job2.invokeOnCompletion { cause: Throwable? ->  if(cause is CancellationException) println("cancelled 4") }
//        scope2.coroutineContext[Job]!!.invokeOnCompletion { cause: Throwable? ->  if(cause is CancellationException) println("cancelled 5") }
//
//        println(
//            "scope2.coroutineContext[Job]!!.children.contains(job2) = ${
//                scope2.coroutineContext[Job]!!.children.contains(job2)
//            }"
//        )
//
//        scope1.cancel("force cancel")
////        job1.cancel("force cancel")
//        delay(50)


        /*
        * test14
        * SupervisorJob
        * 実験省略!
        * */


        /*
        * test15
        * nest したscopeはjobの階層にふくまれない!!!
        *
1
3
scope.coroutineContext[Job]!!.children.contains(innerScope!!.coroutineContext[Job]!!) = false
1 StandaloneCoroutine{Cancelling}@3278da7a is cancelled
3 BlockingCoroutine{Active}@15b5e176 is cancelled
4 BlockingCoroutine{Active}@15b5e176 is cancelled
4
        * */

//        val scope = CoroutineScope(Dispatchers.Default)
//        var innerScope: CoroutineScope? = null
//        scope.launch {
//            launch {
//                println(1)
//                delay(100)
//                println(2)
//            }.invokeOnCompletion { t -> if (t is CancellationException) println("1 $this is cancelled") }
//
//            innerScope = CoroutineScope(Dispatchers.Default)
//            innerScope!!.launch {
//                println(3)
//                delay(100)
//                println(4)
//            }.invokeOnCompletion { t -> if (t is CancellationException) println("2 $this is cancelled") }
//        }.invokeOnCompletion { t -> if (t is CancellationException) println("3 $this is cancelled") }
//        scope.coroutineContext[Job]!!.invokeOnCompletion { t -> if (t is CancellationException) println("4 $this is cancelled") }
//        println(
//            "scope.coroutineContext[Job]!!.children.contains(innerScope!!.coroutineContext[Job]!!) = ${
//                scope.coroutineContext[Job]!!.children.contains(
//                    innerScope!!.coroutineContext[Job]!!
//                )
//            }"
//        )
//        scope.cancel("force cancel")
//        delay(1000)


        /*
        * test16
1
3
scope.coroutineContext[Job]!!.children.contains(innerScope!!.coroutineContext[Job]!!) = false
outerJob.children.contains(scopeCreatedByCoroutineScopeFunction!!.coroutineContext[Job]!!) = true
scopeCreatedByCoroutineScopeFunction = ScopeCoroutine{Active}@43bd930a
1 StandaloneCoroutine{Cancelling}@12812fed is cancelled
5 BlockingCoroutine{Active}@65142518 is cancelled
3 BlockingCoroutine{Active}@65142518 is cancelled
4 BlockingCoroutine{Active}@65142518 is cancelled
4

        * */

        val scope = CoroutineScope(Dispatchers.Default)
        var innerScope: CoroutineScope? = null
        var scopeCreatedByCoroutineScopeFunction: CoroutineScope? = null
        val outerJob = scope.launch {
            launch {
                println(1)
                delay(2000)
                println(2)
            }.invokeOnCompletion { t -> if (t is CancellationException) println("1 $this is cancelled") }

            innerScope = CoroutineScope(Dispatchers.Default)
            innerScope!!.launch {
                println(3)
                delay(2000)
                println(4)
            }.invokeOnCompletion { t -> if (t is CancellationException) println("2 $this is cancelled") }

            // 現在のcoroutineを使う?
//            * Creates a [CoroutineScope] and calls the specified suspend block with this scope.
//            * The provided scope inherits its [coroutineContext][CoroutineScope.coroutineContext] from the outer scope, but overrides
//            * the context's [Job].
            // ということはやっぱりjobは新規で特にparent持たないroot jobになる?
            coroutineScope {
                scopeCreatedByCoroutineScopeFunction = this

//                delay(900) // 900だと真下のdelay(1000)で待つ間に処理が終わってcancelを試す前にsuccess状態でcompleteしてしまう
                delay(2000)
                println(5)
            }
        }
        delay(1000)

        outerJob.invokeOnCompletion { t -> if (t is CancellationException) println("3 $this is cancelled") }
        scope.coroutineContext[Job]!!.invokeOnCompletion { t -> if (t is CancellationException) println("4 $this is cancelled") }
        scopeCreatedByCoroutineScopeFunction!!.coroutineContext[Job]!!.invokeOnCompletion { t -> if (t is CancellationException) println("5 $this is cancelled") }

        println(
            "scope.coroutineContext[Job]!!.children.contains(innerScope!!.coroutineContext[Job]!!) = ${
                scope.coroutineContext[Job]!!.children.contains(
                    innerScope!!.coroutineContext[Job]!!
                )
            }"
        )
        // coroutineScope's job is added in the parent context' job. it's not a root job.
        println(
            "outerJob.children.contains(scopeCreatedByCoroutineScopeFunction!!.coroutineContext[Job]!!) = ${
                outerJob.children.contains(
                    scopeCreatedByCoroutineScopeFunction!!.coroutineContext[Job]!!
                )
            }"
        )
        println("scopeCreatedByCoroutineScopeFunction = $scopeCreatedByCoroutineScopeFunction")

        scope.cancel("force cancel")
        delay(5000)

    }
}

