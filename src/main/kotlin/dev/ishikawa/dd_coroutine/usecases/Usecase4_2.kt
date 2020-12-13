package dev.ishikawa.dd_coroutine.usecases

import kotlinx.coroutines.*

/*
# OLD NOTE
* Usecase4_2Kt$main$1@1230(main)がdispatch(enqueu)される
* Usecase4_2Kt$main$1@(main)がとりだされてresumeWith
*   asyncブロックでcoroutineを同期的に作りstart
*     Usecase4_2Kt$main$1$1$!@2209(outer-async) をenqueue
*     そのなかでinitParentJobInternalにてparent.start ... これなんだ?
*       parent.attachChild(this = )
*         invokeOnCompletion
*           jobNodeなるものを作って返す
*     start.invoke
*       block.startCoroutineCancellable -> そのcoroutine早速resumeWithCancellableWith
*     resumeWithCancellableWithでasyncブロックをdispatch
*   続いてawait.
*     ここでsuspend関数を使う以上、これをresumeさせる処理をdispatchしているはずである...
*       -> わかった。invokeOnCompletionにてouter-asyncのcompletionをrunBlockingがlistenする設定をしている
*     awaitSuspend
*       suspendCoroutineUninterceptedOrReturn
*         invokeOnCompletion
*     dispatch見当たらないがsuspendマークかえして、ここでおわり
* outer-asyncが取り出されてresumeWith
*   inner-async-topをenqueue
*   await
*     **inner-async-topのcompletionをinvokeOnCompletionでlistenする**
*       DeferredCoroutine$await@2475というなぞのcoroutineがuContとしてawaitSuspendのなかでAwaitContinuation
*       としてwrapされjob(DeferredCoroutine@65a1)になる。
*     suspendマーク帰ってきて終了
* inner-async-topが取り出されてresumeWith
*   delayで[1] 自身のcontをschedulerに渡しつつ、suspendマーク帰ってきて終了
* inner-async-topがとりだされてresumeWith
*   outcome = unit
*   自分自身はtop contなのでここでおわり
*     ここの最後のcontinuation.resumeWith 最後のから JobSupport#notifyCompletionにとび、listにもつ各nodeに対しnode.invoke(cause)する
*     このlistにnodeを入れるのがinvokeOnCompletionか...
*   ここでenqueueされるのがouter-asyncではなく!DeferredCoroutine@65a1 = inner-async-topが返すjob.
*     ! 一回jobレイヤーを挟む..
* DeferredCoroutine$await(in outer-async)が取り出される
*   output = unit
*     !! このときのouter-asyncのcompletionはまたもやouter-async with main.(runBlockingじゃないのか)
*     TODO: なぜ?
*       勘違いしているかも。suspendが終わり値なりunitなりが帰ってきた直後の挙動は自分心をcallでよい?
*       仮説: suspend関数が返り値を持つと自分自身をcompletionとして呼び出し、そうでない場合は次のsuspendまで進む
*         val job = あり: outer-async(ただし別coroutineっぽい: DefferedCoroutineからsuspend lambda)
*         val job = なし: outer-async(ただし別coroutineっぽい: DefferedCoroutineからsuspend lambda)
*         よって返り値の有無ではない
*       仮説: asyncは中身のsuspend lambdaとそれを包むDeferredCoroutineがもしかして別物?
*         suspend lambdaがonComplete時に再開させるのはDeferredCoroutine
*         & DeferredCoroutineのonCompleteで再開させるのが新のouter-launch
*         -> これが正解に近い。asyncの場合、jobを一回挟むっぽい。いずれにせよstepを挟んでcaller continuationに戻る。
* */

/*
* - coroutine in coroutine の挙動2(asyncのケース)
* asyncの場合は?
* asyncはほぼlaunchと同じで、launch内の結果をアクセスできるか否かの違いのはず... => そのとおり
* async/launchともに子coroutineが終わるまで親coroutineはおわれない...はず... => そのとおり
*
* awaitはpromiseなどのsuspend化と同じ作り。
* asyncで作ったjobのonComplete的なところでcallerのcoroutineをresumeさせるだけ。
*
* まとめ
* - coroutine builder呼び出しでenqueue
* - await/joinの呼び出しはsuspend関数で、caller coroutineをsuspendする
*   - await/joinのreceiver jobが完了時にDeferredCoroutineがenqueue/resumeされ、値を取り出してcaller coroutineを更にresumeする
*
* file
* - EventLoop.common.kt enqueue
* - ContinuationImpl.kt resumeWith
*
* Usecase4_2Kt$main$1@1230(main)がdispatch(enqueu)される
* Usecase4_2Kt$main$1@1230(main)がとりだされてresumeWith
*   asyncブロックでcoroutineを同期的に作りstart
*     Usecase4_2Kt$main$1$1$!@2260(outer-async) をenqueue
*   続いてawait.
*     await/awaitInternal/awaitSuspend
*       cont.disposeOnCancellation(invokeOnCompletion(ResumeAwaitOnCompletion(this, cont).hasHandler): DisposeHandle)
*         contがcancelされたときには引数のDisposeHandleが発動
*         DisposeHandleの内容は(dispose時に)onCompletionをinvokeして、そのhandlerがResumeAwaitOnCompletion(this, cont)
*           = this job(=asyncが返すjob)のcompletion時にcont(JobSupport$AwaitContinuation@2304)をresumeする登録
*       要約: 微妙なレイヤーを挟んで、 Usecase4_2Kt$main$1$1$!@2260(outer-async) 完了時に Usecase4_2Kt$main$1@1230(main) をresumeするよう登録
*     suspendマーク返して終了
* Usecase4_2Kt$main$1$1$!@2260(outer-async)が取り出されてresumeWith
*   async Usecase4_2Kt$main$1$1$1$job$1@2438(inner-async-top)をenqueue
*   await
*     this job のcompletion時にcont(JobSupport$AwaitContinuation@2476)をresumeする登録
*     suspendマーク帰ってきて終了
* Usecase4_2Kt$main$1$1$1$job$1@2438(inner-async-top)が取り出されてresumeWith
*   delayで[1] 自身のcontをschedulerに渡しつつ、suspendマーク帰ってきて終了
* Usecase4_2Kt$main$1$1$1$job$1@2438(inner-async-top)がとりだされてresumeWith // delay schedulerによる復帰
*   outcome = unit // inner-async-topの返り値はなにもない
*   自分自身はtop contなのでここでおわり
*     ここの最後のcontinuation.resumeWith から JobSupport#notifyCompletionにとび、listにもつ各nodeに対しnode.invoke(cause)する
*     このlistにnodeを入れるのがinvokeOnCompletionか...
*     JobSupport$AwaitContinuation@2476をenqueue
* DeferredCoroutine$await@2475(outer-async)が取り出される
*   TODO DeferredCoroutine$await@2475 == JobSupport$AwaitContinuation@2476がwrapしていたuContのこと。どこからきてどうunwrapしたのか..
*   output = unit
*     asyncブロックの値を以てcallerであるouter-asyncの次の処理へ進む
*   current = completion(Usecase4_2Kt$main$1$1$!@2260(outer-async))
*   output = suspendマーク
*     delayで[1] 自身のcontをschedulerに渡しつつ、suspendマーク帰ってきて終了
* Usecase4_2Kt$longRunningIOOperation$1@3508(outer-async)が取り出される
*   output = string
*   current = completion(Usecase4_2Kt$main$1$1$!@2260(outer-async))
*   output = suspendマーク
*     println string
*     async Usecase4_2Kt$main$1$1$1$1@3964(inner-bottom)をenqueue
*     await
*       uCont = DeferredCoroutine$await$1@4050(outer-async)
*       cont  = JobSupport$AwaitContinuation@4051(outer-async)
*       this job のcompletion時にcont(JobSupport$AwaitContinuation@4051(outer-async))を再開
*     suspendマーク返す
* Usecase4_2Kt$main$1$1$1$1@3964(inner-bottom)が取り出される
*   output = unit // delayもない
*   自分自身はtop contなのでここで終わり
*     notifyCompletionを経由してJobSupport$AwaitContinuation@4051(outer-async)をenqueue
*   TODO: DeferredCoroutine$await@4050 == JobSupport$AwaitContinuation@4051 の検証
*   output = unit
*   current = completion(Usecase4_2Kt$main$1$1$!@2260(outer-async))
*   output = suspendのハズ -> unitだった
*     launch Usecase4_2Kt$main$1$1$1$launchJob$1@4671 をenqueue
*       TODO: joinまでいかない?
* Usecase4_2Kt$main$1$1$1$launchJob$1@4671が取り出される
*   output = suspend マーク
*     delayで自身再開を登録
* Usecase4_2Kt$main$1$1$1$launchJob$1@4671が取り出される(再び)
*   output = unit
*   自分自身はtop contなのでここで終わり
*     notifyCompletionを経由してCancellableContinuationImpl@5089(outer-async)(delegate by Usecase4_2Kt$main$1$1$!@2260(outer-async))をenqueue
* Usecase4_2Kt$main$1$1$!@2260(outer-async) が取り出される
*   output = unit
*   自分自身はtop contなのでここで終わり
*     notifyCompletionを経由してJobSupport$AwaitContinuation@2304をenqueue
* DeferredCoroutine$await@2303が取り出される
*   output = unit
*   current = Usecase4_2Kt$main$1@1230(main)
*   output = unit
*   自分自身はtop contなのでここで終わり -> 全体終了
* */
fun main() {
    runBlocking(CoroutineName("runBlocking")) {
        repeat(1) {
            async(CoroutineName("outer-async-$it")) {
                val job = async(CoroutineName("inner-async-top-$it")) { // 2456
                    delay(30)
                }
                job.await()

                // Dispatchers.Defaultになっているはず
                val result = longRunningIOOperation(it)
                println(result)

                async(CoroutineName("inner-async-bottom-$it")) {
                    println("world")
                }.await()


                val launchJob = launch(CoroutineName("inner-launch-bottom")) {
                    delay(10)
                }
                launchJob.join()
            }.await()
        }
    }
}

private suspend fun longRunningIOOperation(idx: Int): String {
    delay(100)
    return "result:$idx"
}
