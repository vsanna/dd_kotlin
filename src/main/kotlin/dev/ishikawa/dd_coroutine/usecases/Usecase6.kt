package dev.ishikawa.dd_coroutine.usecases

import kotlinx.coroutines.*
import java.lang.Exception
import java.lang.RuntimeException

/*
* # goal
* - exceptionを理解する
*   - coroutine内でcatchされないexceptionはcompletion.resumeWithExceptionで渡される.
*   - 従来のre-throwではない
*
*
* */
fun main() {
    runBlocking {
        val scope = CoroutineScope(Job())
        val coroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            println("$throwable is caught in CoroutineExceptionHandler")
        }

        // test1
//    scope.launch {
//       throwException()
//    }

        // test2
        // 正常にcatchされる
//    scope.launch {
//        try {
//            throwException()
//        } catch (e: Exception) {
//            println("caught $e")
//        }
//    }

        /*
        * test3
        * これはcatchされない!!
        * coroutine内でエラーがcatchされない場合、そのcoroutineはcompletionをresumeWithExceptionする
        * */
//    scope.launch {
//        try {
//            launch {
//                throwException()
//            }
//        } catch (e: Exception) {
//            println("caught $e")
//        }
//    }

        /*test4
        * Caught java.lang.RuntimeException: dummy in CoroutineExceptionHander
        * スレッドもしなない
        * */
//    val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
//        println("Caught $throwable in CoroutineExceptionHander")
//    }
//    val scope2 = CoroutineScope(Job() + exceptionHandler)
//    scope2.launch {
//        throwException()
//    }

        /*
        * test5
        * Caught java.lang.RuntimeException: dummy in CoroutineExceptionHander
        * 同じ挙動
        *
        * ! CoroutineExceptionHandlerはtop-level scopeかSupervisorJob直下でのみinjectする
        * */
//    val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
//        println("Caught $throwable in CoroutineExceptionHander")
//    }
//    val scope2 = CoroutineScope(Job())
//    scope2.launch(exceptionHandler) {
//        throwException()
//    }

        /*
        * test6
        * これはエラーがでる
        * */

//    val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
//        println("Caught $throwable in CoroutineExceptionHander")
//    }
//    val scope2 = CoroutineScope(Job())
//    scope2.launch {
//        launch(exceptionHandler) {
//            throwException()
//        }
//    }


        /*
        * test7
        * */

//    scope.launch {
//        launch {
//            println("Starting coroutine 1")
//            delay(100)
//            throw RuntimeException()
//        }
//
//        launch {
//            println("Starting coroutine2")
//            delay(3)
//        }
//    }



        /*
        * test8
        * java.lang.RuntimeException: dummy is caught
        * launchないのuncaught exceptionは通常通りcoroutineExceptionHandlerに届く
        * */
//    scope.launch(coroutineExceptionHandler) {
//        delay(10)
//        throwException()
//    }

        /*
        * test9
        * exceptionhandlerに到達しない
        * top level coroutineがasyncだとそのDeferredに格納される
        * */
//    scope.async(coroutineExceptionHandler) {
//        delay(10)
//        throwException()
//    }

        /*
        * test10
        * coroutineexceptionhandlerに到達する.
        * job自体は即座に上にpropagateする
        *
        * java.lang.RuntimeException: dummy is caught
        * false
        * true
        * true
        *
        * d.await() は RuntimeException("dummy") をthrowする
        * */

//        var d: Deferred<Unit>? = null
//        scope.launch(coroutineExceptionHandler) {
//            d = async {
//                throwException()
//            }
//        }
//        delay(100)
//        println(d!!.isActive)
//        println(d!!.isCompleted)
//        println(d!!.isCancelled)


        /*
        * test11
        * CoroutineExceptionHandlerでcatchしたら上にはparopagateしなくなる?
        * -> そんなことはないっぽい。上にpropagateする。rootのときだけuncaughtexceptionhandlerに渡さないという性質が発動する。
        * parent jobには普通に渡す
        * そういえばnestedにceh渡しても意味ないと言っていたような。
        *
        * inner launch is cancelled
        * outer launch handler
        * j is cancelled
        * scope is cancelled
        * */

//        val j = scope.launch(CoroutineExceptionHandler { _, _ -> println("outer launch handler") }) {
//            launch(coroutineExceptionHandler) {
//                throwException()
//            }.invokeOnCompletion { println("inner launch is cancelled") }
//        }.invokeOnCompletion { println("j is cancelled") }
//        scope.coroutineContext[Job]!!.invokeOnCompletion { println("scope is cancelled") }
//        delay(100)


        /*
        * test12
        * top levelのcoroutineでエラーをcatchしなかったらThreadのUncaughtExceptionHandler...
        * かりにscopeをtry-catchで囲ってもそうなる?
        * -> そうなった。
        * */

//        try {
//           scope.launch {
//               throwException()
//           }
//        } catch (ex: Exception) {
//            println(ex)
//        }


        /*
        * test13
        * coroutineScope
        * */

        // 復習: これは意味ない
//        try {
//            launch {
//                throwException()
//            }
//        } catch (e: Exception) {
//            println("caught $e")
//        }

        // これはcatchされる
//        caught java.lang.RuntimeException: dummy
//        try {
//            coroutineScope {
//                launch { throwException() }
//                async { throwException() }
//            }
//        } catch (e: Exception) {
//            println("caught $e")
//        }

        /*
        * test14
        * coroutineScopeは上にexceptionを伝播する?
        * -> coroutineScopeは内部のエラーをcoroutineScopeとしてre-throwする性質があるので、それでscope.launchの内部uncaught exceptionになる。
        * すくなくともcoroutineScopeはparent job"から"cancellationはでんぱされる
3
1
java.lang.RuntimeException: dummy
ends!
        * */
//        var innerScope: CoroutineScope? = null
//        scope.launch(CoroutineExceptionHandler { coroutineContext, throwable -> println(throwable) }) {
//            launch { delay(200); println("hello") }.invokeOnCompletion { throwable -> if(throwable is CancellationException) println(1) }
//            coroutineScope {
//                innerScope = this
//                launch { delay(100); throw RuntimeException("dummy") }.invokeOnCompletion { throwable -> if(throwable is CancellationException) println(2) }
//                launch { delay(200); println("this may not be printed") }.invokeOnCompletion { throwable -> if(throwable is CancellationException) println(3) }
//            }
//        }.invokeOnCompletion { throwable -> if(throwable is CancellationException) println(4) }
//        delay(10)
//        innerScope!!.coroutineContext[Job]!!.invokeOnCompletion { throwable -> if(throwable is CancellationException) println(4) }

        /*
        * test15
        * Exception in thread "main" java.lang.RuntimeException: dummy
        * supervisorScopeは独立したsubscopeをin our hierarchyに作る?
        * その独立性により、そのexceptionをhandleするものがいない?
        *
        * Exception in thread "main" java.lang.RuntimeException: dummy
        * this should be printed
        * ends!
        *
        * * A failure of a child does not cause this scope to fail and does not affect its other children,
        * * so a custom policy for handling failures of its children can be implemented. See [SupervisorJob] for details
        * * A failure of the scope itself (exception thrown in the [block] or cancellation) fails the scope with all its children,
        * * but does not cancel parent job.
        * */
//        try {
//            supervisorScope {
//                launch { throw RuntimeException("dummy") }
//                launch { println("this should be printed") }
//            }
//        } catch (e: Exception) {
//            println("caught $e")
//        }


        /*
        * test16
        *
        * これはcatchできる
        * * A failure of the scope itself (exception thrown in the [block] or cancellation) fails the scope with all its children,
        * * but does not cancel parent job.
        * */

//        try {
//            supervisorScope {
//                throwException()
//            }
//        } catch (e: Exception) {
//            println("caught $e")
//        }

        /*
        * test17
        * java.lang.RuntimeException: dummy is caught in CoroutineExceptionHandler
        * ends!
        *
        * supervisorScopeは上へpropagateしないのになぜscope2のCEHがhandleしているのか?
        * -> 単にinner launchがcontextをinheritしているだけ。scope2が処理しているわけではない
        * */

//        val scope2 = CoroutineScope(Job()+ coroutineExceptionHandler)
//        scope2.launch {
//            try {
//                supervisorScope {
//                    try {
//                        launch {
//                            println("CEH: ${coroutineContext[CoroutineExceptionHandler]}")
//                            throw RuntimeException("dummy")
//                        }
//                    } catch (ex: Exception) {
//                        println("not passing here")
//                        throw ex
//                    }
//                }
//            } catch (e: Exception) {
//                println("Caught $e")
//            }
//        }

        /*
        * test18
        * e = kotlinx.coroutines.JobCancellationException: DeferredCoroutine was cancelled; job=DeferredCoroutine{Cancelled}@82b13a4
        * ends!
        * */

//        var deferred: Deferred<Unit>? = null
//        scope.launch {
//            deferred = async{
//                delay(100)
//                println("finished")
//            }
//
//            try {
//                deferred!!.await()
//            } catch (e: Exception) {
//                println("e = $e")
//            }
//        }
//        delay(10)
//        deferred!!.cancel()
//
//        var job: Job? = null
//        scope.launch {
//            job = launch{
//                delay(100)
//                println("finished")
//            }
//
//            try {
//                job!!.join()
//            } catch (e: Exception) {
//                println("e = $e")
//            }
//        }
//        delay(10)
//        job!!.cancel()

        /*
        * test19
        * - coroutineScope, supervisorScope, CoroutineScope, launch, async
        * - 親{coroutine, scope}のcancelは自身に伝播するか
        * - 自身のfailureは親{coroutine, scope}をcancelするか
        * */


    }



    Thread.sleep(100)
    println("ends!")
}

private fun throwException() {
    throw RuntimeException("dummy")
}


private suspend fun doSomethingSuspend() {
    coroutineScope {
        launch {
            throw RuntimeException()
        }
    }
}