package dev.ishikawa.dd_kotlin._5coroutine

import dev.ishikawa.dd_coroutine.util.showDebug
import kotlinx.coroutines.*

fun main () {
/*
# Exception Handling
## Basic
- coroutineがキャンセルされるとその場所でCancellarationExceptionが走る

## Exception Propagation
coroutine builderには2つのパターンが有る
1. 自動でexceptionをcaller functionに伝播するパターン
    - root coroutineの場合、uncaught exceptionとして処理する
2. ユーザーに「その場で」exposeする
    - 特にcatchしないと結果1になる

## CoroutineExceptionHandler
uncaught exceptionは基本consoleに表示されるだけ。そりゃそうか。もうtop leveにまで来ているわけだし
Threadのuncaught exceptionもそう。thread内部のtop level exceptionはmain threadを阻害しない

このprintの挙動をCoroutineExceptionHandler(contextの1要素)でコントロールできる

## Cancellation and Exceptions
- coroutineではcancelのためにCancellationExceptionという例外を使う
    - CancellationExceptionは全てのhandlerで無視される
    - あるcoroutine = job がcancelされたとき、親はcancelされない
- coroutineがCancellationException以外の例外に遭遇したとき
    - 親もキャンセルする
- 親coroutineが例外を処理するのは全てのchildren coroutineがterminateされてから。
    - terminateされるときの処理としてはfinally使える


## Exception aggregation

## Supervision
子がcancelした場合、いつも親もcancelしたいとは限らない
ex. UI thread
ex. サーバーのリクエストを受け付けてはcoroutineを生んでそれに以上するやつ

SupervisorJob, supervisorScopeははエラーの伝達を下方向(親から子)にのみに限定する

* */


//    cancelException()
//    coroutineException()
//    coroutineException2()
    coroutineException3()
}

/*
Throwing exception from launch [67241]current at: id:  13 name: DefaultDispatcher-worker-1 @coroutine#2
Joined failed job [67258]current at: id:   1 name: main @coroutine#1
Throwing exception from async [67260]current at: id:  13 name: DefaultDispatcher-worker-1 @coroutine#3
Caught ArithmeticException [67308]current at: id:   1 name: main @coroutine#1
Exception in thread "DefaultDispatcher-worker-1 @coroutine#2" java.lang.IndexOutOfBoundsException
つまり、 IndexOutOfBoundsExceptionは
* */
fun cancelException() = runBlocking {
    val job = GlobalScope.launch { // root coroutine with launch
        showDebug("Throwing exception from launch")
        throw IndexOutOfBoundsException() // Will be printed to the console by Thread.defaultUncaughtExceptionHandler
    }
    // launch内のIndexOutOfBoundsExceptionは表示だけされる。cancelExceptionを止めはしない
    job.join()
    showDebug("Joined failed job")
    val deferred = GlobalScope.async { // root coroutine with async
        showDebug("Throwing exception from async")
        throw ArithmeticException() // Nothing is printed, relying on user to call await
    }
    try {
        // async内のArithmeticExceptionはcancelExceptionに影響を与えうる
        deferred.await()
        showDebug("Unreached")
    } catch (e: ArithmeticException) {
        showDebug("Caught ArithmeticException")
    }
}


/*
> Task :_7ExceptionHandlingKt.main() FAILED
       1 [55435]current at: id:   1 name: main @coroutine#2
       6 [55437]current at: id:   1 name: main @coroutine#2
       2 [55438]current at: id:   1 name: main @coroutine#3
       5 [55438]current at: id:   1 name: main @coroutine#3
       3 [55438]current at: id:   1 name: main @coroutine#4
2 actionable tasks: 2 executed
Exception in thread "main" java.lang.Exception: dummy

- launchで別coroutineを立ち上げるので先にその下の処理が走る
- 4は表示されない
* */
fun coroutineException() = runBlocking {
    launch {
        showDebug("1")
        launch {
            showDebug("2")
            launch {
                showDebug("3")
                var k = true
                if(k) throw Exception("dummy")
                showDebug("4")
            }
            showDebug("5")
        }
        showDebug("6")
    }
}



/*
       1 [11551]current at: id:   1 name: main @coroutine#2
       8 [11554]current at: id:   1 name: main @coroutine#2
       2 [11554]current at: id:   1 name: main @coroutine#3
       7 [11555]current at: id:   1 name: main @coroutine#3
       3 [11556]current at: id:   1 name: main @coroutine#4
2 actionable tasks: 2 executed
Exception in thread "main" java.lang.Exception: dummy1

5には行かない。dummy1が出た時点でその下のlaunchにはしょりが行かない
* */
fun coroutineException2() = runBlocking {
    launch {
        showDebug("1")
        launch {
            showDebug("2")
            launch {
                showDebug("3")
                var k = true
                if(k) throw Exception("dummy1")
                showDebug("4")
            }
            launch {
                delay(100)
                showDebug("5")
                var k = true
                if(k) throw Exception("dummy2")
                showDebug("6")
            }
            showDebug("7")
        }
        showDebug("8")
    }
}


/*
       1 [1790]current at: id:   1 name: main @coroutine#2
       8 [1793]current at: id:   1 name: main @coroutine#2
       2 [1793]current at: id:   1 name: main @coroutine#3
       7 [1795]current at: id:   1 name: main @coroutine#3
       5 [1802]current at: id:   1 name: main @coroutine#5
2 actionable tasks: 2 executed
Exception in thread "main" java.lang.Exception: dummy2

dummy2が出た時点その親のlaunchにエラーが伝播
? その時兄弟coroutineも死ぬのか？
* */
fun coroutineException3() = runBlocking {
    launch {
        showDebug("1")
        launch {
            showDebug("2")
            launch {
                delay(100)
                showDebug("3")
                var k = true
                if(k) throw Exception("dummy1")
                showDebug("4")
            }
            launch {
                showDebug("5")
                var k = true
                if(k) throw Exception("dummy2")
                showDebug("6")
            }
            showDebug("7")
        }
        showDebug("8")
    }
}
