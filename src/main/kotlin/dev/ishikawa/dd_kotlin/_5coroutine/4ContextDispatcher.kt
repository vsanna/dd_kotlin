package dev.ishikawa.dd_kotlin._5coroutine

import kotlinx.coroutines.*


fun main() {
    // 今これがroot context. 引数ないので
    runBlocking {

        /*
        * coroutineはcontextに属する
        * contextはjob(そとからcancelを扱うentrypoint)とdispatherを持つ
        * dispatcherはどのthreadを割り当てるかを責務に持つ
        *     - 特定のthread / thread pool / 無制限 に割り当てる
        * */

        launch { // context of the parent, main runBlocking coroutine
            println("main runBlocking      : I'm working in thread ${Thread.currentThread().id}:${Thread.currentThread().name}")
        }
        // callerのthreadを使う
        launch(Dispatchers.Unconfined) { // not confined -- will work with main thread
            println("Unconfined            : I'm working in thread ${Thread.currentThread().id}:${Thread.currentThread().name}")
        }
        launch(Dispatchers.Default) { // will get dispatched to DefaultDispatcher
            println("Default               : I'm working in thread ${Thread.currentThread().id}:${Thread.currentThread().name}")
        }
        launch(newSingleThreadContext("MyOwnThread")) { // will get its own new thread
            println("newSingleThreadContext: I'm working in thread ${Thread.currentThread().id}:${Thread.currentThread().name}")
        }
    }
}

