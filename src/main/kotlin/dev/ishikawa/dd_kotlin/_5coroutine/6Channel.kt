package dev.ishikawa.dd_kotlin._5coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import kotlin.system.measureTimeMillis
import dev.ishikawa.dd_coroutine.util.showDebug
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce

fun main() {
    runBlocking {
/*
## channel
- Deferred: 単一の値をcoroutine間でやり取りする方法
- Channel: stream of valuesをcoroutine間でやり取りする方法

概念的にはBlockingQueue、golangのchanに似ている
channelがいっぱいならsender側がsuspend. 空ならreceiver側がsuspend

## closing and iteration over channels
- close = special tokenを送るようなこと。EOF的な。
- receiverはclose tokenがくるまでiteration回すことができる

## pub/sub pattern
producerを関数化したくなる

fun produceMySequence(chan: ReceiveChannel<Int>): ReceiveChannel<Int> {
    for (x in 1..5) chan.send(x * x)
}
val chan = Channel<Int>()
produceMySequence(chan)

ただこのsignatureは直感に反するしpipeline構築できない。
やはりproduceMySequenceがchannelを返してほしい

そこで produce coroutine builder

fun CoroutineScope.produceSquares(): ReceiveChannel<Int> = produce {
    for (x in 1..5) send(x * x)
}

またconsumer sideとしての consumeEach

val squareChannel = produceSeuares()
squares.consumeEach { println(it) }


## pipeline
下記参考

## fan-out
同一channelを複数coroutineでreceiveすることもできる

## fan-in
あるcoroutineがchancelを引き受け、他coroutineがその同一channelに対しsend

## buffered channels
- capacityの指定がないときは、基本からのときのみpush可能


**/

//        basicChannel()
//        closeIteration()
//        produceConsume()
        pipeline()
    }
}


/*
      sending 1 [74910]current at: id:   1 name: main @coroutine#3
    receiving 1 [74983]current at: id:   1 name: main @coroutine#2
      sending 2 [75017]current at: id:   1 name: main @coroutine#3
      sending 3 [75123]current at: id:   1 name: main @coroutine#3
    receiving 2 [75186]current at: id:   1 name: main @coroutine#2
      sending 4 [75230]current at: id:   1 name: main @coroutine#3
      sending 5 [75335]current at: id:   1 name: main @coroutine#3
    receiving 3 [75389]current at: id:   1 name: main @coroutine#2
    receiving 4 [75591]current at: id:   1 name: main @coroutine#2
    receiving 5 [75793]current at: id:   1 name: main @coroutine#2
result = 1023

* */
private fun basicChannel() = runBlocking {
    val channel = Channel<Int>(capacity = 3)

    val result = measureTimeMillis {
        launch {
            for(x in 1..5) {
                delay(50)
                showDebug("sending $x", prefixLen = 15)
                channel.send(x)
            }
        }
        repeat(5) {
            delay(200) // slower than sender
            val n = channel.receive()
            showDebug("receiving $n", prefixLen = 15)
        }
    }
    println("result = $result")
}

/*
       1 [41058]current at: id:   1 name: main @coroutine#2
       4 [41059]current at: id:   1 name: main @coroutine#2
       9 [41059]current at: id:   1 name: main @coroutine#2
      16 [41059]current at: id:   1 name: main @coroutine#2
      25 [41059]current at: id:   1 name: main @coroutine#2
    Done [41061]current at: id:   1 name: main @coroutine#2

* */
private fun closeIteration() = runBlocking {
    val channel = Channel<Int>(capacity = 3)
    launch {
        for (x in 1..5) channel.send(x*x)
        channel.close()
    }

    for(y in channel) showDebug(y.toString())
    showDebug("Done")
}

/*
-99
-96
-91
-84
-75
* */
private fun produceConsume() = runBlocking {
    fun square(): ReceiveChannel<Int> = produce {
        for (x in 1..5) send(x * x)
    }

    val producer = square()
    producer.consumeEach { println((it - 100)) }
}

/*
       number 1 [43392]current at: id:   1 name: main @coroutine#3
       number 2 [43498]current at: id:   1 name: main @coroutine#3
       square 0 [43597]current at: id:   1 name: main @coroutine#4
      consume 0 [43598]current at: id:   1 name: main @coroutine#2
       number 3 [43598]current at: id:   1 name: main @coroutine#3
       number 4 [43702]current at: id:   1 name: main @coroutine#3
       square 1 [43798]current at: id:   1 name: main @coroutine#4
      consume 1 [43798]current at: id:   1 name: main @coroutine#2
       number 5 [43802]current at: id:   1 name: main @coroutine#3
       number 6 [43904]current at: id:   1 name: main @coroutine#3
       square 4 [44003]current at: id:   1 name: main @coroutine#4
      consume 4 [44003]current at: id:   1 name: main @coroutine#2
       number 7 [44005]current at: id:   1 name: main @coroutine#3
       number 8 [44110]current at: id:   1 name: main @coroutine#3
       square 9 [44205]current at: id:   1 name: main @coroutine#4
      consume 9 [44206]current at: id:   1 name: main @coroutine#2
       number 9 [44211]current at: id:   1 name: main @coroutine#3
      number 10 [44316]current at: id:   1 name: main @coroutine#3
      square 16 [44411]current at: id:   1 name: main @coroutine#4
     consume 16 [44411]current at: id:   1 name: main @coroutine#2
      square 25 [44612]current at: id:   1 name: main @coroutine#4
     consume 25 [44613]current at: id:   1 name: main @coroutine#2
      square 36 [44818]current at: id:   1 name: main @coroutine#4
     consume 36 [44819]current at: id:   1 name: main @coroutine#2
      square 49 [45024]current at: id:   1 name: main @coroutine#4
     consume 49 [45024]current at: id:   1 name: main @coroutine#2
      square 64 [45229]current at: id:   1 name: main @coroutine#4
     consume 64 [45230]current at: id:   1 name: main @coroutine#2
      square 81 [45435]current at: id:   1 name: main @coroutine#4
     consume 81 [45436]current at: id:   1 name: main @coroutine#2

* */
private fun pipeline() = runBlocking {
    fun produceNumbers() = produce<Int>(capacity = 5) {
        var x = 0
        while (x < 10) {
            delay(100)
            showDebug("number ${x+1}", prefixLen = 15)
            send(x++)
        } // infinite stream of integers starting from 1
    }

    fun square(numbers: ReceiveChannel<Int>): ReceiveChannel<Int> = produce(capacity = 10) {
        for (x in numbers) {
            delay(200)
            showDebug("square ${x*x}", prefixLen = 15)
            send(x * x)
        }
    }

    val numberProducer = produceNumbers()
    val squareProducer = square(numberProducer)
    squareProducer.consumeEach { n -> showDebug("consume $n", prefixLen = 15) }
}