package dev.ishikawa.dd_coroutine.util

import kotlinx.coroutines.runBlocking
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.*
import io.ktor.client.features.*
import io.ktor.client.features.observer.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun main() {
    runBlocking {
        launch {
            suspendCoroutine { cont ->
                Thread.sleep(100)
                cont.resume(Unit)
            }
        }
    }
}

