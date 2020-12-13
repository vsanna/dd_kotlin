package dev.ishikawa.dd_kotlin._5coroutine

import kotlinx.coroutines.*

fun main() = runBlocking {

    val job = GlobalScope.launch {
        repeat(1000) { i ->
            println("job: I'm sleeping $i")
            delay(500)
        }
    }

    delay(1300)
    println("main: I'm tired of waiting!")
    job.cancel()
    job.join() // wait for job's completion

    println("main: Now I can quit")

//    withContext()
    withTimeout(1300) {
        repeat(1000) { it
            println("i = $it")
            delay(500)
        }
    }

}

