package dev.ishikawa.dd_coroutine.usecases

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/*
* # goal
* - coroutine in coroutine の挙動を実際におって理解する. Usecase3と同じことをする
*
* mainがdispatchされて取り出される
*   同期的にouterをlaunchして(=dispatchして),自分自身はcomplete
* outerが取り出される
*   inner-topをdispatch
*   この時点でcompletionがdebugに差し替わっている!! いつ?
*     ちがうちがう、そもそもlaunchはsuspend関数ではないからcallerからstate machine受け取らない。launchがコルーチンの始まり
*   longRunningIOOperationをschedulerにdispatchしつつsuspend返す
* inner-top取り出される
*   同期的にprintlnして終了
* longRunningIOOperationが取り出される
*   値を返す
*   completableであるouterをdispatch
* outerが取り出される
*   bottomをdispatch
*   ? このときcompletableとしてのrunBlocking呼ばないんだっけ?もう終わってるけど
*     上記解説。
* bottomが取り出される
*   同期的にprintlnして終了
*
* launchは同期関数!! そこで新しいcontinuationを生成している
* */
fun main() {
    runBlocking(CoroutineName("runBlocking")) {
        repeat(1) {
            launch(CoroutineName("outer-launch-$it")) {
                launch(CoroutineName("inner-launch-top-$it")) {
                    println("hello")
                }
                // Dispatchers.Defaultになっているはず
                val result = longRunningIOOperation(it)
                println(result)

                launch(CoroutineName("inner-launch-bottom-$it")) {
                    println("world")
                }
            }
        }
    }
}

private suspend fun longRunningIOOperation(idx: Int): String {
    delay(100)
    return "result:$idx"
}
