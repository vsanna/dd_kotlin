package dev.ishikawa.dd_coroutine.usecases

import kotlinx.coroutines.*
import java.lang.RuntimeException
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

/*
* # goal
* - cancelを理解する
* - exceptionを理解する
*
*
* */
fun main() {
    /*
    * test cases
    * - coroutineScope, supervisorScope, CoroutineScope, launch, async
    * - 親scopeのcancelは自身に伝播するか(親coroutineのテストはしない。なぜならここでの本質は親jobのcancelなので実質同じことを指すから)
    * - 自身の子coroutineによるfailureは親{coroutine, scope}をcancelするか
    *
    要約
    - 上流のcancellation: Jobの論理
        - launch, asyncは当然伝播対象
        - ! coroutineScope, supervisorScopeもcancel伝播対象
        - CoroutineScopeはjob hierarchyに属さないので伝播しない
    - 下流のfailure: JobではなくScopeとtry-catchとresumeWithExceptionの論理
        - 子のlaunch, async内におけるfailureはそれらがcompletion.resumeWithExceptionを呼び出す形で伝播してくる
            - よって try { launch {} } は無意味
        - coroutineScopeはcoroutineScope{}自体がrethrowする形で伝播.
            - このscopeまでたどってきたエラーをhandle. 自身もcancelするしsiblingsにも伝播する. 外界に伝播する
                - よって try { coroutineScope{ launch {...} } } は有効
            - try { coroutineScope { launch {error} }} はどうなる? -> 有効.
        - supervisorScopeも基本的にはcoroutineScopeと同じような動きをするが、scopeまでたどってきたエラーの取り扱いを無視する事によりその後の振る舞いが違って見える
            - このscopeまでたどってきたエラーをignore. 自身もcancelしないしsiblingsにも伝播しない. 外界に伝播しない
                - よって try { supervisorScope{ launch {...} } } はなにもcatchしない
            - supervisorScope 直下のthrowは supervisorScope{}自体がrethrowする形で伝播
            - try { supervisorScope { launch {error} }} はsupervisorScope自身が子コルーチンのfailureを検知すれど何もしないので supervisorScope {} はre-throwしない
                - エラーを子coroutineから受け取っても何もしないので外界に投げようがない
        - CoroutineScope
            - CoroutineExceptionHandlerがなければthreadのUncaughtExceptionHandlerが起動して終わり。
            - そのscopeを起動したcaller coroutineを含むscopeには影響なし
    - 備忘録
        - jobはcompleteするとchildrenから除外される
        - scopeにresumeWithはない! また、全てのcoroutineはscopeに属する。
            - よって、scopeにまで登ってきたexceptionはそこでscopeの外の外界にエラーを伝達する必要がある
                1. ThreadのUncaughtExceptionHandlerを呼び出す
                2. CoroutineExceptionHandlerを持っていればそれで処理する(外界には出さない)
                    - ここでre-throwするとThreadのUncaughtExceptionHandler(つまり外界)に伝播できる
                3. coroutineScope/supervisorScopeのように、そのメソッドのcallerにre-throwする
    *
    * BaseContinuationImpl#resumeWith
      val outcome: Result<Any?> =
          try {
              val outcome = invokeSuspend(param)
              if (outcome === COROUTINE_SUSPENDED) return
              Result.success(outcome)
          } catch (exception: Throwable) {
              Result.failure(exception)
          }
    * */

//    parent_scope__cancel__coroutineScope()
//    parent_scope__cancel__supervisorScope()
//    parent_scope__cancel__CCoroutineScope()
//    parent_scope__cancel__launch()
//    parent_scope__cancel__async()

//    coroutineScope__failure__parent_scope()
//    coroutineScope__direct_failure__parent_scope()
//    coroutineScope__nested_failure__parent_scope()
//    supervisorScope__failure__parent_scope()
//    supervisorScope__direct_failure__parent_scope()
//    supervisorScope__nested_failure__parent_scope()
//    CCoroutineScope__failure__parent_scope()
//    CCoroutineScope__no_failure__parent_scope() // 期待されるjobの親子関係がないことがthrowによるものかどうかの検証
//    launch__failure__parent_scope()
//    async__failure__parent_scope()
    rethrow_in_coroutine_exception_handler()

    Thread.sleep(100)
    println("\nends!")
}

fun parent_scope__cancel__coroutineScope() {
    runBlocking {
        val outerScope = CoroutineScope(Job())
        val coroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            println("$throwable is caught in CoroutineExceptionHandler")
        }

        /*
        * 親coroutineのcancelはcoroutineScopeに伝播するか
        *
# Entities
outerScope = CoroutineScope(coroutineContext=JobImpl{Active}@7113b13f)
innerScope = ScopeCoroutine{Active}@45820e51
innerJob = StandaloneCoroutine{Active}@42d8062c
outerJob = StandaloneCoroutine{Active}@6043cd28

# Relationships
outerScope.coroutineContext[Job]!!.children.contains(outerJob) = true
outerJob.children.contains(innerScope!!.coroutineContext[Job]) = true
innerScope!!.coroutineContext[Job]!!.children.contains(innerJob) = true

# Cancelling Result
[innerJob] kotlinx.coroutines.JobCancellationException: Job was cancelled; job=JobImpl{Cancelling}@7113b13f is thrown
[innerScope] kotlinx.coroutines.JobCancellationException: Job was cancelled; job=JobImpl{Cancelling}@7113b13f is thrown
[outerJob] kotlinx.coroutines.JobCancellationException: Job was cancelled; job=JobImpl{Cancelling}@7113b13f is thrown
[outerScope] kotlinx.coroutines.JobCancellationException: Job was cancelled; job=JobImpl{Cancelled}@7113b13f is thrown

ends!
        *
        * coroutineScopeはcallerのscopeにガッツリ組み込まれている。よってcancel伝播する
        * */

        var innerScope: CoroutineScope? = null
        var innerJob: Job? = null
        val outerJob = outerScope.launch {
            coroutineScope {
                innerScope = this
                innerScope!!.coroutineContext[Job]!!.invokeOnCompletion(printIfCancelled("innerScope"))
                innerJob = launch {
                    delay(200)
                    println("passing here")
                }
                innerJob!!.invokeOnCompletion(printIfCancelled("innerJob"))
                delay(200)
            }
        }
        outerScope.coroutineContext[Job]!!.invokeOnCompletion(printIfCancelled("outerScope"))
        outerJob.invokeOnCompletion(printIfCancelled("outerJob"))

        println("# Entities")
        println("outerScope = $outerScope")
        println("innerScope = $innerScope")
        println("innerJob = $innerJob")
        println("outerJob = $outerJob")

        println("\n# Relationships")
        println("outerScope.coroutineContext[Job]!!.children.contains(outerJob) = ${printRelation(outerScope, outerJob)}")
        println("outerJob.children.contains(innerScope!!.coroutineContext[Job]) = ${printRelation(outerJob, innerScope!!)}")
        println("innerScope!!.coroutineContext[Job]!!.children.contains(innerJob) = ${printRelation(innerScope!!, innerJob!!)}")

        println("\n# Cancelling Result")
        delay(100)
        outerScope.cancel()
    }
}

fun parent_scope__cancel__supervisorScope() {
    runBlocking {
        val outerScope = CoroutineScope(Job())
        val coroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            println("$throwable is caught in CoroutineExceptionHandler")
        }

        /*
     * 親coroutineのcancelはsupervisorScopeに伝播するか
     *
# Entities
outerScope = CoroutineScope(coroutineContext=JobImpl{Active}@7113b13f)
innerScope = SupervisorCoroutine{Active}@45820e51
innerJob = StandaloneCoroutine{Active}@42d8062c
outerJob = StandaloneCoroutine{Active}@6043cd28

# Relationships
outerScope.coroutineContext[Job]!!.children.contains(outerJob) = true
outerJob.children.contains(innerScope!!.coroutineContext[Job]) = true
innerScope!!.coroutineContext[Job]!!.children.contains(innerJob) = true

# Cancelling Result
[innerJob] kotlinx.coroutines.JobCancellationException: Job was cancelled; job=JobImpl{Cancelling}@7113b13f is thrown
[innerScope] kotlinx.coroutines.JobCancellationException: Job was cancelled; job=JobImpl{Cancelling}@7113b13f is thrown
[outerJob] kotlinx.coroutines.JobCancellationException: Job was cancelled; job=JobImpl{Cancelling}@7113b13f is thrown
[outerScope] kotlinx.coroutines.JobCancellationException: Job was cancelled; job=JobImpl{Cancelled}@7113b13f is thrown

ends!
     *
     * supervisorScopeもcallerのscopeにガッツリ組み込まれている。よってcancel伝播する
     * */

        var innerScope: CoroutineScope? = null
        var innerJob: Job? = null
        val outerJob = outerScope.launch {
            supervisorScope {
                innerScope = this
                innerScope!!.coroutineContext[Job]!!.invokeOnCompletion(printIfCancelled("innerScope"))
                innerJob = launch {
                    delay(200)
                    println("passing here")
                }
                innerJob!!.invokeOnCompletion(printIfCancelled("innerJob"))
                delay(200)
            }
        }
        outerScope.coroutineContext[Job]!!.invokeOnCompletion(printIfCancelled("outerScope"))
        outerJob.invokeOnCompletion(printIfCancelled("outerJob"))

        println("# Entities")
        println("outerScope = $outerScope")
        println("innerScope = $innerScope")
        println("innerJob = $innerJob")
        println("outerJob = $outerJob")

        println("\n# Relationships")
        println("outerScope.coroutineContext[Job]!!.children.contains(outerJob) = ${printRelation(outerScope, outerJob)}")
        println("outerJob.children.contains(innerScope!!.coroutineContext[Job]) = ${printRelation(outerJob, innerScope!!)}")
        println("innerScope!!.coroutineContext[Job]!!.children.contains(innerJob) = ${printRelation(innerScope!!, innerJob!!)}")

        println("\n# Cancelling Result")
        delay(100)
        outerScope.cancel()
    }
}

// 関数名がかぶっているといわれるのでC2つ
fun parent_scope__cancel__CCoroutineScope() {
    runBlocking {
        val outerScope = CoroutineScope(Job())
        val coroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            println("$throwable is caught in CoroutineExceptionHandler")
        }

        /*
     * 親coroutineのcancelはCoroutineScopeに伝播するか
     *
# Entities
outerScope = CoroutineScope(coroutineContext=JobImpl{Active}@4923ab24)
innerScope = StandaloneCoroutine{Active}@6c841372
innerJob = StandaloneCoroutine{Active}@44c8afef
outerJob = StandaloneCoroutine{Completed}@7b69c6ba

# Relationships
outerScope.coroutineContext[Job]!!.children.contains(outerJob) = false  // why? -> 解決. completeしたchild jobは削除される
outerJob.children.contains(innerScope!!.coroutineContext[Job]) = false  // expected
outerJob.children.contains(newScope!!.coroutineContext[Job]) = false    // expected
newScope!!.coroutineContext[Job]!!.children.contains(innerScope!!.coroutineContext[Job]) = true // expected
innerScope!!.coroutineContext[Job]!!.children.contains(innerJob) = true // expected

# Cancelling Result
[outerScope] kotlinx.coroutines.JobCancellationException: Job was cancelled; job=JobImpl{Cancelled}@4923ab24 is thrown

ends!
     * outerScopeのcancelはnewScope以下には伝播しない(expected)
     * coroutineScopeのときは、block引数のcoroutineがouterJobの直下に属したが、(当たり前だが)こちらはnewScopeにinnerScopeが属する。注意。
     * TODO: outerScopeのcancelがouterJobに伝播しないのは?
     * */

        var innerScope: CoroutineScope? = null
        var newScope: CoroutineScope? = CoroutineScope(Job())
        var innerJob: Job? = null
        val outerJob = outerScope.launch {
            newScope!!.launch {
                innerScope = this
                println("this == innerScope : ${this == innerScope}")
                println("this = $this")
                println("innerScope = $innerScope")
                innerJob = launch {
                    delay(400)
                    println("passing here")
                }
                innerJob!!.invokeOnCompletion(printIfCancelled("innerJob"))
                delay(1000)
            }
            delay(1000)
        }
        delay(50)
        innerScope!!.coroutineContext[Job]!!.invokeOnCompletion(printIfCancelled("innerScope"))
        outerScope.coroutineContext[Job]!!.invokeOnCompletion(printIfCancelled("outerScope"))
        outerJob.invokeOnCompletion(printIfCancelled("outerJob"))

        println("# Entities")
        println("outerScope = $outerScope")
        println("innerScope = $innerScope")
        println("innerJob = $innerJob")
        println("outerJob = $outerJob")

        println("\n# Relationships")
        println("outerScope.coroutineContext[Job]!!.children.contains(outerJob) = ${printRelation(outerScope, outerJob)}")
        println("outerJob.children.contains(innerScope!!.coroutineContext[Job]) = ${printRelation(outerJob, innerScope!!)}")
        println("outerJob.children.contains(newScope!!.coroutineContext[Job]) = ${printRelation(outerJob, newScope!!)}")
        println("newScope!!.coroutineContext[Job]!!.children.contains(innerScope!!.coroutineContext[Job]) = ${printRelation(newScope!!, innerScope!!)}")
        println("innerScope!!.coroutineContext[Job]!!.children.contains(innerJob) = ${printRelation(innerScope!!, innerJob!!)}")

        println("\n# Cancelling Result")
        delay(200)
        outerScope.cancel()

        printJobHierarchy(outerScope.coroutineContext[Job]!!, outerJob)
        printJobHierarchy(newScope.coroutineContext[Job]!!, innerJob!!)
    }
}

fun parent_scope__cancel__launch() {
    runBlocking {
        val outerScope = CoroutineScope(Job())
        val coroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            println("$throwable is caught in CoroutineExceptionHandler")
        }

        /*
     * 親coroutineのcancelはCoroutineScopeに伝播するか
     *
# Entities
outerScope = CoroutineScope(coroutineContext=JobImpl{Active}@670b40af)
innerScope = StandaloneCoroutine{Active}@4923ab24
innerJob = StandaloneCoroutine{Active}@44c8afef
outerJob = StandaloneCoroutine{Active}@7b69c6ba

# Relationships
outerScope.coroutineContext[Job]!!.children.contains(outerJob) = true
outerJob.children.contains(innerScope!!.coroutineContext[Job]) = true

# Cancelling Result
innerScope.coroutineContext[Job]!! == job = true
[innerJob] kotlinx.coroutines.JobCancellationException: Job was cancelled; job=JobImpl{Cancelling}@670b40af is thrown
[innerScope] kotlinx.coroutines.JobCancellationException: Job was cancelled; job=JobImpl{Cancelling}@670b40af is thrown
[outerJob] kotlinx.coroutines.JobCancellationException: Job was cancelled; job=JobImpl{Cancelling}@670b40af is thrown
[outerScope] kotlinx.coroutines.JobCancellationException: Job was cancelled; job=JobImpl{Cancelled}@670b40af is thrown

     * そもそもscope挟まないのでcancel伝播は当たり前
     * */

        var innerScope: CoroutineScope? = null
        var innerJob: Job? = null
        val outerJob = outerScope.launch {
            val job = launch {
                innerScope = this
                innerJob = launch {
                    delay(400)
                    println("passing here")
                }
                innerJob!!.invokeOnCompletion(printIfCancelled("innerJob"))
                delay(60)
            }
            delay(80)
            println("innerScope.coroutineContext[Job]!! == job = ${innerScope!!.coroutineContext[Job]!! == job}")
        }
        delay(50)
        innerScope!!.coroutineContext[Job]!!.invokeOnCompletion(printIfCancelled("innerScope"))
        outerScope.coroutineContext[Job]!!.invokeOnCompletion(printIfCancelled("outerScope"))
        outerJob.invokeOnCompletion(printIfCancelled("outerJob"))

        println("# Entities")
        println("outerScope = $outerScope")
        println("innerScope = $innerScope")
        println("innerJob = $innerJob")
        println("outerJob = $outerJob")

        println("\n# Relationships")
        println("outerScope.coroutineContext[Job]!!.children.contains(outerJob) = ${printRelation(outerScope, outerJob)}")
        println("outerJob.children.contains(innerScope!!.coroutineContext[Job]) = ${printRelation(outerJob, innerScope!!)}")

        println("\n# Cancelling Result")
        delay(200)
        outerScope.cancel()
    }
}

fun parent_scope__cancel__async() {
    runBlocking {
        val outerScope = CoroutineScope(Job())
        val coroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            println("$throwable is caught in CoroutineExceptionHandler")
        }

        /*
     * 親coroutineのcancelはCoroutineScopeに伝播するか
     *
# Entities
outerScope = CoroutineScope(coroutineContext=JobImpl{Active}@670b40af)
innerScope = DeferredCoroutine{Active}@4923ab24
innerJob = StandaloneCoroutine{Active}@44c8afef
outerJob = StandaloneCoroutine{Active}@7b69c6ba

# Relationships
outerScope.coroutineContext[Job]!!.children.contains(outerJob) = true
outerJob.children.contains(innerScope!!.coroutineContext[Job]) = true

# Cancelling Result
innerScope.coroutineContext[Job]!! == job = true
[innerJob] kotlinx.coroutines.JobCancellationException: Job was cancelled; job=JobImpl{Cancelling}@670b40af is thrown
[innerScope] kotlinx.coroutines.JobCancellationException: Job was cancelled; job=JobImpl{Cancelling}@670b40af is thrown
[outerJob] kotlinx.coroutines.JobCancellationException: Job was cancelled; job=JobImpl{Cancelling}@670b40af is thrown
[outerScope] kotlinx.coroutines.JobCancellationException: Job was cancelled; job=JobImpl{Cancelled}@670b40af is thrown

ends!
     * そもそもscope挟まないのでcancel伝播は当たり前
     * */

        var innerScope: CoroutineScope? = null
        var innerJob: Job? = null
        val outerJob = outerScope.launch {
            val job = async {
                innerScope = this
                innerJob = launch {
                    delay(400)
                    println("passing here")
                }
                innerJob!!.invokeOnCompletion(printIfCancelled("innerJob"))
                delay(60)
            }
            delay(80)
            println("innerScope.coroutineContext[Job]!! == job = ${innerScope!!.coroutineContext[Job]!! == job}")
        }
        delay(50)
        innerScope!!.coroutineContext[Job]!!.invokeOnCompletion(printIfCancelled("innerScope"))
        outerScope.coroutineContext[Job]!!.invokeOnCompletion(printIfCancelled("outerScope"))
        outerJob.invokeOnCompletion(printIfCancelled("outerJob"))

        println("# Entities")
        println("outerScope = $outerScope")
        println("innerScope = $innerScope")
        println("innerJob = $innerJob")
        println("outerJob = $outerJob")

        println("\n# Relationships")
        println("outerScope.coroutineContext[Job]!!.children.contains(outerJob) = ${printRelation(outerScope, outerJob)}")
        println("outerJob.children.contains(innerScope!!.coroutineContext[Job]) = ${printRelation(outerJob, innerScope!!)}")

        println("\n# Cancelling Result")
        delay(200)
        outerScope.cancel()
    }
}

fun coroutineScope__failure__parent_scope() {
    runBlocking {
        val outerCoroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            println("$throwable is caught in outer CoroutineExceptionHandler")
        }
        val innerCoroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            println("$throwable is caught in inner CoroutineExceptionHandler")
        }
        val outerScope = CoroutineScope(Job())

        /*
# Entities
outerScope = CoroutineScope(coroutineContext=[JobImpl{Active}@cb51256, dev.ishikawa.dd_coroutine.usecases.Usecase6_2Kt$coroutineScope__failure__parent_scope$1$invokeSuspend$$inlined$CoroutineExceptionHandler$1@59906517])
innerScope = ScopeCoroutine{Active}@5bfbf16f
innerJob = StandaloneCoroutine{Active}@25af5db5
outerJob = StandaloneCoroutine{Active}@12cdcf4

# Relationships
outerScope.coroutineContext[Job]!!.children.contains(outerJob) = true
outerJob.children.contains(innerScope!!.coroutineContext[Job]) = true
innerScope!!.coroutineContext[Job]!!.children.contains(innerJob) = true

# Failure Result
[innerJob] java.lang.RuntimeException: dummy is thrown
[innerScope] java.lang.RuntimeException: dummy is thrown
java.lang.RuntimeException: dummy is caught in outer CoroutineExceptionHandler
[outerJob] java.lang.RuntimeException: dummy is thrown
[outerScope] java.lang.RuntimeException: dummy is thrown

#Status
[outerScope] isActive: false, isCompleted: true, isCancelled: true
[innerScope] isActive: false, isCompleted: true, isCancelled: true
[innerJob] isActive: false, isCompleted: true, isCancelled: true
[outerJob] isActive: false, isCompleted: true, isCancelled: true

ends!

        *
        * outerScopeまで伝播しているしcancelもされている(全てnon activeでcancelled)
        *
        * # coroutineExceptionHandlerない場合
# Entities
outerScope = CoroutineScope(coroutineContext=JobImpl{Active}@45820e51)
innerScope = ScopeCoroutine{Active}@42d8062c
innerJob = StandaloneCoroutine{Active}@6043cd28
outerJob = StandaloneCoroutine{Active}@cb51256

# Relationships
outerScope.coroutineContext[Job]!!.children.contains(outerJob) = true
outerJob.children.contains(innerScope!!.coroutineContext[Job]) = true
innerScope!!.coroutineContext[Job]!!.children.contains(innerJob) = true

# Failure Result
[innerJob] java.lang.RuntimeException: dummy is thrown
[innerScope] java.lang.RuntimeException: dummy is thrown
Exception in thread "DefaultDispatcher-worker-2" java.lang.RuntimeException: dummy
[outerJob] java.lang.RuntimeException: dummy is thrown
[outerScope] java.lang.RuntimeException: dummy is thrown

#Status
[outerScope] isActive: false, isCompleted: true, isCancelled: true
[innerScope] isActive: false, isCompleted: true, isCancelled: true
[innerJob] isActive: false, isCompleted: true, isCancelled: true
[outerJob] isActive: false, isCompleted: true, isCancelled: true

ends!

        * */

        var innerScope: CoroutineScope? = null
        var innerJob: Job? = null
        val outerJob = outerScope.launch {
            coroutineScope {
                innerScope = this
                innerScope!!.coroutineContext[Job]!!.invokeOnCompletion(printWithAnyException("innerScope"))
                innerJob = launch {
                    delay(50)
                    throw RuntimeException("dummy")
                }
                innerJob!!.invokeOnCompletion(printWithAnyException("innerJob"))
                delay(200)
            }
        }
        outerScope.coroutineContext[Job]!!.invokeOnCompletion(printWithAnyException("outerScope"))
        outerJob.invokeOnCompletion(printWithAnyException("outerJob"))

        println("# Entities")
        println("outerScope = $outerScope")
        println("innerScope = $innerScope")
        println("innerJob = $innerJob")
        println("outerJob = $outerJob")

        println("\n# Relationships")
        println("outerScope.coroutineContext[Job]!!.children.contains(outerJob) = ${printRelation(outerScope, outerJob)}")
        println("outerJob.children.contains(innerScope!!.coroutineContext[Job]) = ${printRelation(outerJob, innerScope!!)}")
        println("innerScope!!.coroutineContext[Job]!!.children.contains(innerJob) = ${printRelation(innerScope!!, innerJob!!)}")

        println("\n# Failure Result")
        delay(100)

        println("\n#Status")
        printStatus("outerScope", outerScope)
        printStatus("innerScope", innerScope!!)
        printStatus("innerJob", innerJob!!)
        printStatus("outerJob", outerJob)

    }
}

fun coroutineScope__direct_failure__parent_scope() {
    runBlocking {
        val outerCoroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            println("$throwable is caught in outer CoroutineExceptionHandler")
        }
        val innerCoroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            println("$throwable is caught in inner CoroutineExceptionHandler")
        }
        val outerScope = CoroutineScope(Job())

        /*
e = java.lang.RuntimeException: dummy
# Entities
outerScope = CoroutineScope(coroutineContext=JobImpl{Active}@29b5cd00)
outerJob = StandaloneCoroutine{Completed}@60285225

# Relationships
[outerJob] completed successfully
outerScope.coroutineContext[Job]!!.children.contains(outerJob) = false

# Failure Result

#Status
[outerScope] isActive: true, isCompleted: false, isCancelled: false
[outerJob] isActive: false, isCompleted: true, isCancelled: false

ends!
        *
        *
        * */

        val outerJob = outerScope.launch {
            try {
                coroutineScope {
                    throw RuntimeException("dummy")
                }
            } catch (e: Exception) {
                println("e = $e")
            }
        }
        outerScope.coroutineContext[Job]!!.invokeOnCompletion(printWithAnyException("outerScope"))
        outerJob.invokeOnCompletion(printWithAnyException("outerJob"))

        println("# Entities")
        println("outerScope = $outerScope")
        println("outerJob = $outerJob")

        println("\n# Relationships")
        println("outerScope.coroutineContext[Job]!!.children.contains(outerJob) = ${printRelation(outerScope, outerJob)}")

        println("\n# Failure Result")
        delay(100)

        println("\n#Status")
        printStatus("outerScope", outerScope)
        printStatus("outerJob", outerJob)

    }
}

fun coroutineScope__nested_failure__parent_scope() {
    runBlocking {
        val outerCoroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            println("$throwable is caught in outer CoroutineExceptionHandler")
        }
        val innerCoroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            println("$throwable is caught in inner CoroutineExceptionHandler")
        }
        val outerScope = CoroutineScope(Job())

        /*
# Entities
e = java.lang.RuntimeException: dummy
[outerJob] completed successfully
outerScope = CoroutineScope(coroutineContext=JobImpl{Active}@29b5cd00)
outerJob = StandaloneCoroutine{Completed}@60285225

# Relationships
outerScope.coroutineContext[Job]!!.children.contains(outerJob) = false

# Failure Result

#Status
[outerScope] isActive: true, isCompleted: false, isCancelled: false
[outerJob] isActive: false, isCompleted: true, isCancelled: false

ends!

        *
        *
        * */

        val outerJob = outerScope.launch {
            try {
                coroutineScope {
                    launch {
                        throw RuntimeException("dummy")
                    }
                }
            } catch (e: Exception) {
                println("e = $e")
            }
        }
        outerScope.coroutineContext[Job]!!.invokeOnCompletion(printWithAnyException("outerScope"))
        outerJob.invokeOnCompletion(printWithAnyException("outerJob"))

        println("# Entities")
        println("outerScope = $outerScope")
        println("outerJob = $outerJob")

        println("\n# Relationships")
        println("outerScope.coroutineContext[Job]!!.children.contains(outerJob) = ${printRelation(outerScope, outerJob)}")

        println("\n# Failure Result")
        delay(100)

        println("\n#Status")
        printStatus("outerScope", outerScope)
        printStatus("outerJob", outerJob)

    }
}

fun supervisorScope__failure__parent_scope() {
    runBlocking {
        val outerCoroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            println("$throwable is caught in outer CoroutineExceptionHandler")
        }
        val innerCoroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            println("$throwable is caught in inner CoroutineExceptionHandler")
        }
        val outerScope = CoroutineScope(Job() + outerCoroutineExceptionHandler)

        /*
        * 親coroutineのcancelはcoroutineScopeに伝播するか
        *
java.lang.RuntimeException: dummy is caught in outer CoroutineExceptionHandler
[innerJob] java.lang.RuntimeException: dummy is thrown
# Entities
outerScope = CoroutineScope(coroutineContext=[JobImpl{Active}@6b0c2d26, dev.ishikawa.dd_coroutine.usecases.Usecase6_2Kt$supervisorScope__failure__parent_scope$1$invokeSuspend$$inlined$CoroutineExceptionHandler$1@3d3fcdb0])
outerJob = StandaloneCoroutine{Active}@641147d0
innerScope = SupervisorCoroutine{Active}@6e38921c
innerJob = StandaloneCoroutine{Cancelled}@64d7f7e0
sibilingJob = StandaloneCoroutine{Active}@27c6e487

# Relationships
outerScope.coroutineContext[Job]!!.children.contains(outerJob) = true
outerJob.children.contains(innerScope!!.coroutineContext[Job]) = true
innerScope!!.coroutineContext[Job]!!.children.contains(innerJob) = false   // なんでだ -> completeしたから. delay調整
innerScope!!.coroutineContext[Job]!!.children.contains(sibilingJob) = true // expected

# Failure Result
passing here
[sibilingJob] completed successfully
[innerScope] completed successfully
[outerJob] completed successfully

#Status
[outerScope] isActive: true, isCompleted: false, isCancelled: false
[outerJob] isActive: false, isCompleted: true, isCancelled: false
[innerScope] isActive: false, isCompleted: true, isCancelled: false
[innerJob] isActive: false, isCompleted: true, isCancelled: true
[sibilingJob] isActive: false, isCompleted: true, isCancelled: false

ends!
        *
        * outerJob, outerScopeは無事. innerScopeも無事
        * innerJobのuncaught exceptionはinheritしたcontextにあるouterCoroutineExceptionHandlerでcatchしてる(多分)
        * coroutineScopeだろうがsupervisorScopeだろうが、CEHがないとthreadを殺してしまうので注意
        * TODO: udemyのあの例は何だったんだ?
        *
        * # exceptionHandlerない場合
Exception in thread "DefaultDispatcher-worker-1" java.lang.RuntimeException: dummy
[innerJob] java.lang.RuntimeException: dummy is thrown
# Entities
outerScope = CoroutineScope(coroutineContext=JobImpl{Active}@1e7c7811)
outerJob = StandaloneCoroutine{Active}@77ec78b9
innerScope = SupervisorCoroutine{Active}@1a3869f4
innerJob = StandaloneCoroutine{Cancelled}@a38d7a3
sibilingJob = StandaloneCoroutine{Active}@77f99a05

# Relationships
outerScope.coroutineContext[Job]!!.children.contains(outerJob) = true
outerJob.children.contains(innerScope!!.coroutineContext[Job]) = true
innerScope!!.coroutineContext[Job]!!.children.contains(innerJob) = false
innerScope!!.coroutineContext[Job]!!.children.contains(sibilingJob) = true

# Failure Result
passing here
[sibilingJob] completed successfully
[innerScope] completed successfully
[outerJob] completed successfully

#Status
[outerScope] isActive: true, isCompleted: false, isCancelled: false
[outerJob] isActive: false, isCompleted: true, isCancelled: false
[innerScope] isActive: false, isCompleted: true, isCancelled: false
[innerJob] isActive: false, isCompleted: true, isCancelled: true
[sibilingJob] isActive: false, isCompleted: true, isCancelled: false

ends!
        * */

        var innerScope: CoroutineScope? = null
        var innerJob: Job? = null
        var sibilingJob: Job? = null
        val outerJob = outerScope.launch {
            supervisorScope {
                innerScope = this
                innerScope!!.coroutineContext[Job]!!.invokeOnCompletion(printWithAnyException("innerScope"))
                innerJob = launch {
                    delay(50)
                    throw RuntimeException("dummy")
                }
                innerJob!!.invokeOnCompletion(printWithAnyException("innerJob"))
                sibilingJob = launch {
                    delay(300)
                    println("passing here")
                }
                sibilingJob!!.invokeOnCompletion(printWithAnyException("sibilingJob"))
                delay(100)
            }
        }
        outerScope.coroutineContext[Job]!!.invokeOnCompletion(printWithAnyException("outerScope"))
        outerJob.invokeOnCompletion(printWithAnyException("outerJob"))
        delay(80)

        println("# Entities")
        println("outerScope = $outerScope")
        println("outerJob = $outerJob")
        println("innerScope = $innerScope")
        println("innerJob = $innerJob")
        println("sibilingJob = $sibilingJob")

        println("\n# Relationships")
        println("outerScope.coroutineContext[Job]!!.children.contains(outerJob) = ${printRelation(outerScope, outerJob)}")
        println("outerJob.children.contains(innerScope!!.coroutineContext[Job]) = ${printRelation(outerJob, innerScope!!)}")
        println("innerScope!!.coroutineContext[Job]!!.children.contains(innerJob) = ${printRelation(innerScope!!, innerJob!!)}")
        println("innerScope!!.coroutineContext[Job]!!.children.contains(sibilingJob) = ${printRelation(innerScope!!, sibilingJob!!)}")

        println("\n# Failure Result")
        delay(500)

        println("\n#Status")
        printStatus("outerScope", outerScope)
        printStatus("outerJob", outerJob)
        printStatus("innerScope", innerScope!!)
        printStatus("innerJob", innerJob!!)
        printStatus("sibilingJob", sibilingJob!!)

    }
}

fun supervisorScope__direct_failure__parent_scope() {
    runBlocking {
        val outerCoroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            println("$throwable is caught in outer CoroutineExceptionHandler")
        }
        val innerCoroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            println("$throwable is caught in inner CoroutineExceptionHandler")
        }
        val outerScope = CoroutineScope(Job() + outerCoroutineExceptionHandler)

        /*
e = java.lang.RuntimeException: dummy
[outerJob] completed successfully

#Status
[outerScope] isActive: true, isCompleted: false, isCancelled: false
[outerJob] isActive: false, isCompleted: true, isCancelled: false

ends!
        *
        * supervisorScope直下のfailureはre-throw
        */

        var innerScope: CoroutineScope? = null
        var innerJob: Job? = null
        var sibilingJob: Job? = null
        val outerJob = outerScope.launch {
            try {
                supervisorScope {
                    throw RuntimeException("dummy")
                }
            } catch (e: Exception) {
                println("e = $e")
            }
        }
        outerScope.coroutineContext[Job]!!.invokeOnCompletion(printWithAnyException("outerScope"))
        outerJob.invokeOnCompletion(printWithAnyException("outerJob"))
        delay(80)

        delay(500)

        println("\n#Status")
        printStatus("outerScope", outerScope)
        printStatus("outerJob", outerJob)

    }
}

fun supervisorScope__nested_failure__parent_scope() {
    runBlocking {
        val outerCoroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            println("$throwable is caught in outer CoroutineExceptionHandler")
        }
        val innerCoroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            println("$throwable is caught in inner CoroutineExceptionHandler")
        }
        val outerScope = CoroutineScope(Job() + outerCoroutineExceptionHandler)

        /*
java.lang.RuntimeException: dummy is caught in outer CoroutineExceptionHandler
[outerJob] completed successfully

#Status
[outerScope] isActive: true, isCompleted: false, isCancelled: false
[outerJob] isActive: false, isCompleted: true, isCancelled: false

ends!
        *
        * supervisorScopeの子coroutineのfailureは検知せず
        */

        var innerScope: CoroutineScope? = null
        var innerJob: Job? = null
        var sibilingJob: Job? = null
        val outerJob = outerScope.launch {
            try {
                supervisorScope {
                    launch {
                        throw RuntimeException("dummy")
                    }
                }
            } catch (e: Exception) {
                println("e = $e")
            }
        }
        outerScope.coroutineContext[Job]!!.invokeOnCompletion(printWithAnyException("outerScope"))
        outerJob.invokeOnCompletion(printWithAnyException("outerJob"))
        delay(80)

        delay(500)

        println("\n#Status")
        printStatus("outerScope", outerScope)
        printStatus("outerJob", outerJob)

    }
}


fun CCoroutineScope__failure__parent_scope() {
    runBlocking {
        val outerCoroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            println("$throwable is caught in outer CoroutineExceptionHandler")
        }
        val innerCoroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            println("$throwable is caught in inner CoroutineExceptionHandler")
        }
        val outerScope = CoroutineScope(Job() + outerCoroutineExceptionHandler)

        /*
        * 親coroutineのcancelはcoroutineScopeに伝播するか
        *
this == innerScope : true
this = StandaloneCoroutine{Active}@6b36dafc
[outerJob] completed successfully
[innerJob] java.lang.RuntimeException: dummy is thrown
[sibilingJob] kotlinx.coroutines.JobCancellationException: Parent job is Cancelling; job=StandaloneCoroutine{Cancelling}@6b36dafc is thrown(cancelled)
Exception in thread "DefaultDispatcher-worker-2" java.lang.RuntimeException: dummy
[innerScope] java.lang.RuntimeException: dummy is thrown
[newScope] java.lang.RuntimeException: dummy is thrown
*
# Entities
outerScope = CoroutineScope(coroutineContext=[JobImpl{Active}@27f723, dev.ishikawa.dd_coroutine.usecases.Usecase6_2Kt$CCoroutineScope__failure__parent_scope$1$invokeSuspend$$inlined$CoroutineExceptionHandler$1@670b40af])
outerJob = StandaloneCoroutine{Completed}@4923ab24
newScope = CoroutineScope(coroutineContext=JobImpl{Cancelled}@44c8afef)
innerScope = StandaloneCoroutine{Cancelled}@6b36dafc
innerJob = StandaloneCoroutine{Cancelled}@7b69c6ba
sibilingJob = StandaloneCoroutine{Cancelled}@46daef40

# Relationships
outerScope.coroutineContext[Job]!!.children.contains(outerJob) = false
outerJob.children.contains(innerScope!!.coroutineContext[Job]) = false
outerJob.children.contains(newScope!!.coroutineContext[Job]) = false
newScope!!.coroutineContext[Job]!!.children.contains(innerScope!!.coroutineContext[Job]) = false
innerScope!!.coroutineContext[Job]!!.children.contains(innerJob) = false
innerScope!!.coroutineContext[Job]!!.children.contains(sibilingJob) = false

# Failure Result

#Status
[outerScope] isActive: true, isCompleted: false, isCancelled: false
[outerJob] isActive: false, isCompleted: true, isCancelled: false
[newScope] isActive: false, isCompleted: true, isCancelled: true
[innerScope] isActive: false, isCompleted: true, isCancelled: true
[innerJob] isActive: false, isCompleted: true, isCancelled: true
[sibilingJob] isActive: false, isCompleted: true, isCancelled: true

ends!

        *
        * innerJobでexception -> innerScopeがcancel -> newScopeがcancel
        *   -> sibilingがcancel(parent job is cancelling)
        * outerJobが無事終了(CoroutineScopeからの上方向propagationなし. 独立scopeなので)
        * outerScopeはまだactive
        * */

        var innerScope: CoroutineScope? = null
        var newScope: CoroutineScope? = CoroutineScope(Job())
        var innerJob: Job? = null
        var sibilingJob: Job? = null
        val outerJob = outerScope.launch {
            newScope!!.launch {
                innerScope = this
                println("this == innerScope : ${this == innerScope}")
                println("this = $this")

                innerScope!!.coroutineContext[Job]!!.invokeOnCompletion(printWithAnyException("innerScope"))
                innerJob = launch {
                    delay(50)
                    throw RuntimeException("dummy")
                }
                innerJob!!.invokeOnCompletion(printWithAnyException("innerJob"))
                sibilingJob = launch {
                    delay(300)
                    println("passing here")
                }
                sibilingJob!!.invokeOnCompletion(printWithAnyException("sibilingJob"))
                delay(100)
            }
            newScope!!.coroutineContext[Job]!!.invokeOnCompletion(printWithAnyException("newScope"))
        }
        outerScope.coroutineContext[Job]!!.invokeOnCompletion(printWithAnyException("outerScope"))
        outerJob.invokeOnCompletion(printWithAnyException("outerJob"))
        delay(80)

        println("# Entities")
        println("outerScope = $outerScope")
        println("outerJob = $outerJob")
        println("newScope = $newScope")
        println("innerScope = $innerScope")
        println("innerJob = $innerJob")
        println("sibilingJob = $sibilingJob")

        println("\n# Relationships")
        println("outerScope.coroutineContext[Job]!!.children.contains(outerJob) = ${printRelation(outerScope, outerJob)}")
        println("outerJob.children.contains(innerScope!!.coroutineContext[Job]) = ${printRelation(outerJob, innerScope!!)}")
        println("outerJob.children.contains(newScope!!.coroutineContext[Job]) = ${printRelation(outerJob, newScope!!)}")
        println("newScope!!.coroutineContext[Job]!!.children.contains(innerScope!!.coroutineContext[Job]) = ${printRelation(newScope!!, innerScope!!)}")
        println("innerScope!!.coroutineContext[Job]!!.children.contains(innerJob) = ${printRelation(innerScope!!, innerJob!!)}")
        println("innerScope!!.coroutineContext[Job]!!.children.contains(sibilingJob) = ${printRelation(innerScope!!, sibilingJob!!)}")

        println("\n# Failure Result")
        delay(500)

        println("\n#Status")
        printStatus("outerScope", outerScope)
        printStatus("outerJob", outerJob)
        printStatus("newScope", newScope!!)
        printStatus("innerScope", innerScope!!)
        printStatus("innerJob", innerJob!!)
        printStatus("sibilingJob", sibilingJob!!)

    }
}

fun CCoroutineScope__no_failure__parent_scope() {
    runBlocking {
        val outerCoroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            println("$throwable is caught in outer CoroutineExceptionHandler")
        }
        val innerCoroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            println("$throwable is caught in inner CoroutineExceptionHandler")
        }
        val outerScope = CoroutineScope(Job() + outerCoroutineExceptionHandler)

        /*
        * 親coroutineのcancelはcoroutineScopeに伝播するか
        *
this == innerScope : true
this = StandaloneCoroutine{Active}@33ee368a
[outerJob] completed successfully
[innerJob] completed successfully
# Entities
outerScope = CoroutineScope(coroutineContext=[JobImpl{Active}@4923ab24, dev.ishikawa.dd_coroutine.usecases.Usecase6_2Kt$CCoroutineScope__no_failure__parent_scope$1$invokeSuspend$$inlined$CoroutineExceptionHandler$1@44c8afef])
outerJob = StandaloneCoroutine{Completed}@7b69c6ba
newScope = CoroutineScope(coroutineContext=JobImpl{Active}@46daef40)
innerScope = StandaloneCoroutine{Active}@33ee368a
innerJob = StandaloneCoroutine{Completed}@12f41634
sibilingJob = StandaloneCoroutine{Active}@13c27452

# Relationships
outerScope.coroutineContext[Job]!!.children.contains(outerJob) = false // まじで謎。exceptionのせいではないのか.. -> 下記で解決
outerJob.children.contains(innerScope!!.coroutineContext[Job]) = false // expected
outerJob.children.contains(newScope!!.coroutineContext[Job]) = false   // expected
newScope!!.coroutineContext[Job]!!.children.contains(innerScope!!.coroutineContext[Job]) = true // expectedだがfailureある時と挙動違う
innerScope!!.coroutineContext[Job]!!.children.contains(innerJob) = false    // まじで謎 -> 下記で解決
innerScope!!.coroutineContext[Job]!!.children.contains(sibilingJob) = true  // expectedだがfailureある時と挙動違う
↓
↓ delay調整後
↓
# Relationships
outerScope.coroutineContext[Job]!!.children.contains(outerJob) = true   // expected. つまり終わったchildは消える. 当たり前
outerJob.children.contains(innerScope!!.coroutineContext[Job]) = false  // expected
outerJob.children.contains(newScope!!.coroutineContext[Job]) = false    // expected
newScope!!.coroutineContext[Job]!!.children.contains(innerScope!!.coroutineContext[Job]) = true // expected
innerScope!!.coroutineContext[Job]!!.children.contains(innerJob) = true // expected
innerScope!!.coroutineContext[Job]!!.children.contains(sibilingJob) = true // expected


# Failure Result
passing here
[sibilingJob] completed successfully
[innerScope] completed successfully

#Status
[outerScope] isActive: true, isCompleted: false, isCancelled: false
[outerJob] isActive: false, isCompleted: true, isCancelled: false
[newScope] isActive: true, isCompleted: false, isCancelled: false
[innerScope] isActive: false, isCompleted: true, isCancelled: false
[innerJob] isActive: false, isCompleted: true, isCancelled: false
[sibilingJob] isActive: false, isCompleted: true, isCancelled: false

ends!
        *
        * innerJobでexception -> innerScopeがcancel -> newScopeがcancel
        *   -> sibilingがcancel(parent job is cancelling)
        * outerJobが無事終了(CoroutineScopeからの上方向propagationなし. 独立scopeなので)
        * outerScopeはまだactive
        * */

        var innerScope: CoroutineScope? = null
        var newScope: CoroutineScope? = CoroutineScope(Job())
        var innerJob: Job? = null
        var sibilingJob: Job? = null
        val outerJob = outerScope.launch {
            newScope!!.launch {
                innerScope = this
                println("this == innerScope : ${this == innerScope}")
                println("this = $this")

                innerScope!!.coroutineContext[Job]!!.invokeOnCompletion(printWithAnyException("innerScope"))
                innerJob = launch {
                    delay(1000)
                }
                innerJob!!.invokeOnCompletion(printWithAnyException("innerJob"))
                sibilingJob = launch {
                    println("passing here")
                    delay(1000)
                }
                sibilingJob!!.invokeOnCompletion(printWithAnyException("sibilingJob"))
                delay(1000)
            }
            newScope!!.coroutineContext[Job]!!.invokeOnCompletion(printWithAnyException("newScope"))
            delay(1000)
        }
        outerScope.coroutineContext[Job]!!.invokeOnCompletion(printWithAnyException("outerScope"))
        outerJob.invokeOnCompletion(printWithAnyException("outerJob"))
        delay(80)

        println("# Entities")
        println("outerScope = $outerScope")
        println("outerJob = $outerJob")
        println("newScope = $newScope")
        println("innerScope = $innerScope")
        println("innerJob = $innerJob")
        println("sibilingJob = $sibilingJob")

        println("\n# Relationships")
        println("outerScope.coroutineContext[Job]!!.children.contains(outerJob) = ${printRelation(outerScope, outerJob)}")
        println("outerJob.children.contains(innerScope!!.coroutineContext[Job]) = ${printRelation(outerJob, innerScope!!)}")
        println("outerJob.children.contains(newScope!!.coroutineContext[Job]) = ${printRelation(outerJob, newScope!!)}")
        println("newScope!!.coroutineContext[Job]!!.children.contains(innerScope!!.coroutineContext[Job]) = ${printRelation(newScope!!, innerScope!!)}")
        println("innerScope!!.coroutineContext[Job]!!.children.contains(innerJob) = ${printRelation(innerScope!!, innerJob!!)}")
        println("innerScope!!.coroutineContext[Job]!!.children.contains(sibilingJob) = ${printRelation(innerScope!!, sibilingJob!!)}")

        println("\n# Failure Result")
        delay(500)

        println("\n#Status")
        printStatus("outerScope", outerScope)
        printStatus("outerJob", outerJob)
        printStatus("newScope", newScope!!)
        printStatus("innerScope", innerScope!!)
        printStatus("innerJob", innerJob!!)
        printStatus("sibilingJob", sibilingJob!!)

    }
}

fun launch__failure__parent_scope() {
    runBlocking {
        val outerCoroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            println("$throwable is caught in outer CoroutineExceptionHandler")
        }
        val innerCoroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            println("$throwable is caught in inner CoroutineExceptionHandler")
        }
        val outerScope = CoroutineScope(Job() + outerCoroutineExceptionHandler)

        /*
        * 親coroutineのcancelはcoroutineScopeに伝播するか
        *
j == innerScope : false
this == innerScope : true
this = StandaloneCoroutine{Active}@7164ca3e

# Entities
outerScope = CoroutineScope(coroutineContext=[JobImpl{Active}@59906517, dev.ishikawa.dd_coroutine.usecases.Usecase6_2Kt$launch__failure__parent_scope$1$invokeSuspend$$inlined$CoroutineExceptionHandler$1@5bfbf16f])
outerJob = StandaloneCoroutine{Active}@25af5db5
innerScope = StandaloneCoroutine{Active}@7164ca3e
innerJob = StandaloneCoroutine{Active}@12cdcf4
sibilingJob = StandaloneCoroutine{Active}@5bcea91b

# Relationships
outerScope.coroutineContext[Job]!!.children.contains(outerJob) = true      // expected
outerJob.children.contains(innerScope!!.coroutineContext[Job]) = true      // expected
innerScope!!.coroutineContext[Job]!!.children.contains(innerJob) = true    // expected
innerScope!!.coroutineContext[Job]!!.children.contains(sibilingJob) = true // expected

# Failure Result
[sibilingJob] kotlinx.coroutines.JobCancellationException: Parent job is Cancelling; job=StandaloneCoroutine{Cancelling}@7164ca3e is thrown(cancelled)
[innerJob] java.lang.RuntimeException: dummy is thrown
[j] java.lang.RuntimeException: dummy is thrown
[innerScope] java.lang.RuntimeException: dummy is thrown
java.lang.RuntimeException: dummy is caught in outer CoroutineExceptionHandler
[outerJob] java.lang.RuntimeException: dummy is thrown
[outerScope] java.lang.RuntimeException: dummy is thrown

#Status
[outerScope] isActive: false, isCompleted: true, isCancelled: true
[outerJob] isActive: false, isCompleted: true, isCancelled: true
[innerScope] isActive: false, isCompleted: true, isCancelled: true
[innerJob] isActive: false, isCompleted: true, isCancelled: true
[sibilingJob] isActive: false, isCompleted: true, isCancelled: true

ends!
        *
        * outerScopeまでちゃんと伝播。cancel扱いなのはsibiling以下
        * */

        var innerScope: CoroutineScope? = null
        var innerJob: Job? = null
        var sibilingJob: Job? = null
        val outerJob = outerScope.launch {
            val j = launch {
                innerScope = this
                println("this == innerScope : ${this == innerScope}")
                println("this = $this")

                innerScope!!.coroutineContext[Job]!!.invokeOnCompletion(printWithAnyException("innerScope"))
                innerJob = launch {
                    delay(100)
                    throw RuntimeException("dummy")
                }
                innerJob!!.invokeOnCompletion(printWithAnyException("innerJob"))
                sibilingJob = launch {
                    delay(1000)
                }
                sibilingJob!!.invokeOnCompletion(printWithAnyException("sibilingJob"))
                delay(1000)
            }
            j.invokeOnCompletion(printWithAnyException("j"))
            println("j == innerScope : ${j == innerScope}")
            delay(1000)
        }
        outerScope.coroutineContext[Job]!!.invokeOnCompletion(printWithAnyException("outerScope"))
        outerJob.invokeOnCompletion(printWithAnyException("outerJob"))
        delay(80)

        println("# Entities")
        println("outerScope = $outerScope")
        println("outerJob = $outerJob")
        println("innerScope = $innerScope")
        println("innerJob = $innerJob")
        println("sibilingJob = $sibilingJob")

        println("\n# Relationships")
        println("outerScope.coroutineContext[Job]!!.children.contains(outerJob) = ${printRelation(outerScope, outerJob)}")
        println("outerJob.children.contains(innerScope!!.coroutineContext[Job]) = ${printRelation(outerJob, innerScope!!)}")
        println("innerScope!!.coroutineContext[Job]!!.children.contains(innerJob) = ${printRelation(innerScope!!, innerJob!!)}")
        println("innerScope!!.coroutineContext[Job]!!.children.contains(sibilingJob) = ${printRelation(innerScope!!, sibilingJob!!)}")

        println("\n# Failure Result")
        delay(500)

        println("\n#Status")
        printStatus("outerScope", outerScope)
        printStatus("outerJob", outerJob)
        printStatus("innerScope", innerScope!!)
        printStatus("innerJob", innerJob!!)
        printStatus("sibilingJob", sibilingJob!!)

    }
}

fun async__failure__parent_scope() {
    runBlocking {
        val outerCoroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            println("$throwable is caught in outer CoroutineExceptionHandler")
        }
        val innerCoroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            println("$throwable is caught in inner CoroutineExceptionHandler")
        }
        val outerScope = CoroutineScope(Job() + outerCoroutineExceptionHandler)

        /*
        * 親coroutineのcancelはcoroutineScopeに伝播するか
        *
this == innerScope : true
j == innerScope : true
this = DeferredCoroutine{Active}@4c6bc2b1

# Entities
outerScope = CoroutineScope(coroutineContext=[JobImpl{Active}@670b40af, dev.ishikawa.dd_coroutine.usecases.Usecase6_2Kt$async__failure__parent_scope$1$invokeSuspend$$inlined$CoroutineExceptionHandler$1@4923ab24])
outerJob = StandaloneCoroutine{Active}@44c8afef
innerScope = DeferredCoroutine{Active}@4c6bc2b1
innerJob = StandaloneCoroutine{Active}@7b69c6ba
sibilingJob = StandaloneCoroutine{Active}@46daef40

# Relationships
outerScope.coroutineContext[Job]!!.children.contains(outerJob) = true
outerJob.children.contains(innerScope!!.coroutineContext[Job]) = true
innerScope!!.coroutineContext[Job]!!.children.contains(innerJob) = true
innerScope!!.coroutineContext[Job]!!.children.contains(sibilingJob) = true

# Failure Result
[sibilingJob] kotlinx.coroutines.JobCancellationException: Parent job is Cancelling; job=DeferredCoroutine{Cancelling}@4c6bc2b1 is thrown(cancelled)
[innerJob] java.lang.RuntimeException: dummy is thrown
[j] java.lang.RuntimeException: dummy is thrown
[innerScope] java.lang.RuntimeException: dummy is thrown
java.lang.RuntimeException: dummy is caught in outer CoroutineExceptionHandler
[outerJob] java.lang.RuntimeException: dummy is thrown
[outerScope] java.lang.RuntimeException: dummy is thrown

#Status
[outerScope] isActive: false, isCompleted: true, isCancelled: true
[outerJob] isActive: false, isCompleted: true, isCancelled: true
[innerScope] isActive: false, isCompleted: true, isCancelled: true
[innerJob] isActive: false, isCompleted: true, isCancelled: true
[sibilingJob] isActive: false, isCompleted: true, isCancelled: true

ends!
        *
        * outerScopeまでちゃんと伝播。cancel扱いなのはsibiling以下
        * */

        var innerScope: CoroutineScope? = null
        var innerJob: Job? = null
        var sibilingJob: Job? = null
        val outerJob = outerScope.launch {
            val j = async {
                innerScope = this
                println("this == innerScope : ${this == innerScope}")
                println("this = $this")

                innerScope!!.coroutineContext[Job]!!.invokeOnCompletion(printWithAnyException("innerScope"))
                innerJob = launch {
                    delay(100)
                    throw RuntimeException("dummy")
                }
                innerJob!!.invokeOnCompletion(printWithAnyException("innerJob"))
                sibilingJob = launch {
                    delay(1000)
                }
                sibilingJob!!.invokeOnCompletion(printWithAnyException("sibilingJob"))
                delay(1000)
            }
            j.invokeOnCompletion(printWithAnyException("j"))
            println("j == innerScope : ${j == innerScope}")
            delay(1000)
        }
        outerScope.coroutineContext[Job]!!.invokeOnCompletion(printWithAnyException("outerScope"))
        outerJob.invokeOnCompletion(printWithAnyException("outerJob"))
        delay(80)

        println("# Entities")
        println("outerScope = $outerScope")
        println("outerJob = $outerJob")
        println("innerScope = $innerScope")
        println("innerJob = $innerJob")
        println("sibilingJob = $sibilingJob")

        println("\n# Relationships")
        println("outerScope.coroutineContext[Job]!!.children.contains(outerJob) = ${printRelation(outerScope, outerJob)}")
        println("outerJob.children.contains(innerScope!!.coroutineContext[Job]) = ${printRelation(outerJob, innerScope!!)}")
        println("innerScope!!.coroutineContext[Job]!!.children.contains(innerJob) = ${printRelation(innerScope!!, innerJob!!)}")
        println("innerScope!!.coroutineContext[Job]!!.children.contains(sibilingJob) = ${printRelation(innerScope!!, sibilingJob!!)}")

        println("\n# Failure Result")
        delay(500)

        println("\n#Status")
        printStatus("outerScope", outerScope)
        printStatus("outerJob", outerJob)
        printStatus("innerScope", innerScope!!)
        printStatus("innerJob", innerJob!!)
        printStatus("sibilingJob", sibilingJob!!)

    }
}

fun rethrow_in_coroutine_exception_handler() {
    runBlocking {
        /*
        * # rethrowなしのとき
java.lang.RuntimeException: dummy is caught in outer CoroutineExceptionHandler
[outerJob] java.lang.RuntimeException: dummy is thrown
ends!
        *
        * # rethrowするとき
java.lang.RuntimeException: dummy is caught in outer CoroutineExceptionHandler
thread = Thread[Thread-0,5,main], exception = java.lang.RuntimeException: dummy in UncaughtExceptionHandler
[outerJob] java.lang.RuntimeException: dummy is thrown

ends!
        * threadのUncaughtExceptionHandlerをcallする
        * */
        val coroutineDispatcher = Executors.newSingleThreadExecutor({ r ->
            Thread(r).apply {
                this.setUncaughtExceptionHandler { thread, exception ->
                    println("thread = $thread, exception = $exception in UncaughtExceptionHandler")
                }
            }
        }).asCoroutineDispatcher()

        val coroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            println("$throwable is caught in outer CoroutineExceptionHandler")
//            throw throwable
        }
        val outerScope = CoroutineScope(Job() + coroutineDispatcher + coroutineExceptionHandler)

        val outerJob = outerScope.launch {
            launch {
                launch {
                    throw RuntimeException("dummy")
                }
            }
        }
        outerJob.invokeOnCompletion(printWithAnyException("outerJob"))
        delay(500)
    }
}

fun printIfCancelled(marker: String): (throwable: Throwable?) -> Unit {
    return {throwable ->
        if (throwable is CancellationException) {
            println("[$marker] $throwable is thrown(cancelled)")
        }
    }
}
fun printWithAnyException(marker: String): (throwable: Throwable?) -> Unit {
    return {throwable ->
        throwable?.let {
            if (throwable is CancellationException) {
                println("[$marker] $throwable is thrown(cancelled)")
            } else {
                println("[$marker] $throwable is thrown")
            }
        }
        if (throwable == null) {
            println("[$marker] completed successfully")
        }
    }
}

fun printRelation(scope: CoroutineScope, job: Job): Boolean {
    return printRelation(getJobFromScope(scope), job)
}
fun printRelation(job: Job, scope: CoroutineScope): Boolean {
    return printRelation(job, getJobFromScope(scope))
}
fun printRelation(scope: CoroutineScope, innerScope: CoroutineScope): Boolean {
    return printRelation(getJobFromScope(scope), getJobFromScope(innerScope))
}
fun printRelation(job: Job, target: Job): Boolean {
    return job.children.contains(target)
}
fun getJobFromScope(scope: CoroutineScope): Job {
    return scope.coroutineContext[Job]!!
}


fun printStatus(marker: String, job: Job) {
    println("[$marker] isActive: ${job.isActive}, isCompleted: ${job.isCompleted}, isCancelled: ${job.isCancelled}")
}
fun printStatus(marker: String, scope: CoroutineScope) {
    printStatus(marker, getJobFromScope(scope))
}


fun printJobHierarchy(root: Job, target: Job) {
    val hierarchy = mutableListOf<Job>()
    if(hasAsDescendant(root, target, hierarchy)) {
        hierarchy.forEachIndexed { index, job ->
            if (index == hierarchy.size-1) {
                print(job)
            } else {
                print("$job > ")
            }
        }
    } else {
        println("root($root) not containing target($target)")
    }
}

fun hasAsDescendant(current: Job, target: Job, hierarchy: MutableList<Job>): Boolean {
    hierarchy.add(current)

    if (current.children.contains(target)) {
        hierarchy.add(target)
        return true
    }
    current.children.forEach { c ->
        if(hasAsDescendant(c, target, hierarchy)) {
            return true
        }
    }
    return false
}