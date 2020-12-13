package dev.ishikawa.dd_coroutine.usecases

import kotlinx.coroutines.*

/*
* # goal:
* - runBlockingの中でlaunchする
*     - その挙動を実際におって理解する
* - Dispatchers.Defaultの挙動
*     - Dispatchers.IOなどの言及はするが内部まで見るのはDefaultのみ。
*     - CoroutineSchedulerのglobalQueueに入って、workerがそれを貪欲に取り出す
*
* mainをenqueue
* mainをresumeWith(mainスレッド)
*   outer-launchをDefaultにdispatch
*   outcome = unit
*   completion(debug).resumeWith(outcome)で終わり
* outer-launchをresumeWith(DefaultDispatcher-worker)
*   delayで [1] outer-launchをdispatch して [2] suspendマーク返して終了
*   ! delay内部のcont.context.delay.scheduleResumeAfterDelayにおけるdelayがDefaultDelay(DefaultExecutor)になることがある
*     DefaultExecutorが kotlin.coroutines.DefaultExecutor というスレッドを持っている
*       DefaultExecutor.thread内でcreateThreadSync()しており、その中でThread(this = DefaultExecutor).startしている
*         startの中ではtarget.run()
*         DefaultExecutor#runの中で無限ループ
*       schedule/unpark内でthreadにアクセスしてそのタイミングで上記createThreadSync走る
* outer-launchをresumeWith(DefaultDispatcher-worker) <- longRunningIOOperation/delayでsuspendした分
*   outcome = string // longRunningIOOperationのresume結果
*     completionもouter-launch
*   while次loop
*     outcome = unit
*     completion.resumeWith(outcome) で終了
*
* */
fun main() {
    runBlocking(CoroutineName("runBlocking")) {
        repeat(1) {
            launch(CoroutineName("outer-launch") + Dispatchers.Default) {
                // Dispatchers.Defaultになっているはず
                val result = longRunningIOOperation(it)
                println(result)
            }
        }
    }
}

private suspend fun longRunningIOOperation(idx: Int): String {
    delay(100)
    return "result:$idx"
}
