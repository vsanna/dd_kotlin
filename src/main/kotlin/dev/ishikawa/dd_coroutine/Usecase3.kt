package dev.ishikawa.dd_coroutine

import dev.ishikawa.dd_coroutine.util.showDebug
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

fun main() {
//    flowException()
//    flowException2()
//    flowException3()
//    flowNonlaunchIn()
    flowlaunchIn()
}

/*
ex dummy
のみ。この時点で終わり
* */
private fun flowException() {
    runBlocking {
        (1..5).asFlow()
            .map { it.toString() }
            .onEach { throw Exception("dummy") }
            .catch { ex -> emit("ex ${ex.message}") }
            .collect { println(it) }
    }
}

/*
ex dummy
のみ。この時点で終わり
* */
private fun flowException2() {
    runBlocking {
       try {
           (1..5).asFlow()
               .map { it.toString() }
               .onEach { throw Exception("dummy") }
               .catch { ex -> emit("ex ${ex.message}") }
               .collect { println(it) }
       } catch (ex: Exception) {
           println(ex)
       }
    }
}


/*
> Process 'command '/path/to/java/11.0.9-amzn/bin/java'' finished with non-zero exit value 1
全体が終わって終了
* */
private fun flowException3() {
    runBlocking {
        (1..5).asFlow()
            .map { it.toString() }
            .catch { ex -> emit("ex ${ex.message}") }
            .onEach { throw Exception("dummy") }
            .collect { println(it) }
    }
}

private fun flowExceptionEncapsulation() {
    runBlocking {
        try {
            (1..5).asFlow()
                .map { it.toString() }
                .onEach { throw Exception("This Error is Internal Exception!") }
                .catch { ex -> throw Exception("this Error is Outer Exception $ex") }
                .collect { println(it) }
        } catch (ex: Exception) {
            println(ex)
        }
    }
}

/*
do some notify action: 1 [20618]current at: id:   1 name: main @coroutine#1
do some notify action: 2 [20721]current at: id:   1 name: main @coroutine#1
do some notify action: 3 [20823]current at: id:   1 name: main @coroutine#1
                    Done [20823]current at: id:   1 name: main @coroutine#1

* */
private fun flowNonlaunchIn() {
    runBlocking {
        (1..3).asFlow().onEach { delay(100) }
            .onEach { event -> showDebug("do some notify action: $event") }
            .collect()

        // collectだとこのshowDebugがそのflowの終了を待ってしまう
        // 単にaddEventLister的に使いたいのであれば下のlaunchIn
        showDebug("Done")
    }
}

/*
                    Done [49864]current at: id:   1 name: main @coroutine#1
do some notify action: 1 [50001]current at: id:   1 name: main @coroutine#2
do some notify action: 2 [50105]current at: id:   1 name: main @coroutine#2
do some notify action: 3 [50210]current at: id:   1 name: main @coroutine#2
* */
private fun flowlaunchIn() {
    runBlocking {
        // launchInを使うと別coroutineへ
        (1..3).asFlow().onEach { delay(100) }
            .onEach { event -> showDebug("do some notify action: $event") }
            .launchIn(this)
        showDebug("Done")
    }
}