package dev.ishikawa.dd_coroutine

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


fun main() {
    // Dispatchers.Defaultが使われる
    println(Dispatchers.Default.toString())
    println(Dispatchers.IO.toString())
    println(Dispatchers.Unconfined.toString())


    GlobalScope.launch {

        // fuelの実装は中で runCatching { future.get() }
        // つまりこれってどこかのスレッドを普通にblockしてない?
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

    // Thread.sleepすればしっかり結果でる...つまりdispatchしたものをどこかでちゃんと消化している
    Thread.sleep(3000)
}

