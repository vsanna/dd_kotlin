package dev.ishikawa.dd_kotlin._5coroutine

import kotlinx.coroutines.*
import kotlin.concurrent.thread


fun main() {
    showDebug()                                 // main
    runBlocking {
        showDebug()
        List(5) {
            launch {
                showDebug()                     // main
                delay(500)
                print(".")
            }
        }
        launch {
            delay(3000)
            println("\n=")
            showDebug()                         // main
        }

        async {
            delay(3000)
            println("==")                       // main: ほぼl23と同タイミング? launch vs async
            showDebug()
        }
        val deferred = async(start = CoroutineStart.LAZY) {
            delay(3000)
            println("===")                      // main:
            showDebug()
        }
        deferred.await()                        // await呼び出さないとmain終わらない。runBlockingは中のcoroutineが終わるまでまつっぽい。
    }

    thread {
        showDebug()                             // Thread-1
        thread {
            showDebug()                         // Thread-2
        }
    }
}

fun showDebug() {
    println("current at: id:${"%4s".format(Thread.currentThread().id.toString())} name: ${Thread.currentThread().name}")
}


