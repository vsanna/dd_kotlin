package dev.ishikawa.dd_coroutine

import kotlinx.coroutines.runBlocking
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.*
import io.ktor.client.features.*
import io.ktor.client.features.observer.*
import io.ktor.client.request.*
import io.ktor.http.*

fun main() {
    runBlocking {
        val client = HttpClient(Apache)
        val res = client.request<String> {
            url("https://api.mocki.io/v1/a9841c50")
            method = HttpMethod.Get
        }

        println(res)
        client.close()
    }
}

