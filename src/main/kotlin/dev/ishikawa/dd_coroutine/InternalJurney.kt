package dev.ishikawa.dd_coroutine

import com.github.kittinunf.fuel.httpGet
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import com.github.kittinunf.result.Result;


fun main() {

//    Runnable { println("hello ${Thread.currentThread()}") }.run()

    /*
    * runBlockingはBlockingCoroutineを生成する
    * BlockingCoroutineはその内部でjoinBlockingを呼び出され、その中で
    * while(true) { eventLoop.processNextEvent() } を実行してループする。
    * */
    runBlocking {
        "https://api.mocki.io/v1/a9841c50"
            .httpGet()
            .responseString { request, response, result ->
                when(result) {
                    is Result.Failure -> {
                        val ex = result.getException()
                        println(ex)
                    }
                    is Result.Success -> {
                        val data = result.get()
                        println(data)
                    }
                }
            }
            .join()
    }
}

