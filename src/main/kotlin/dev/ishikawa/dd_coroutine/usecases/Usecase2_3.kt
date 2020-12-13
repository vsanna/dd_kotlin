package dev.ishikawa.dd_coroutine.usecases

import io.ktor.util.Identity.decode
import kotlinx.coroutines.*

/*
* scope/contextの理解
* - cancelの伝播
*
*

*  */
fun main() {
    runBlocking {
        println("1 ${this as CoroutineScope}")
        launch {
            println("2 $this")
            val deferred = async {
                println("3 $this")
            }
        }

        coroutineScope {
            println("4 $this")
            launch {
                println("5 $this")
            }
        }
    }
}

