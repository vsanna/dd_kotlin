package dev.ishikawa.dd_coroutine.usecases

import dev.ishikawa.dd_kotlin._6more.user
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.concurrent.thread


/*
* # 最終的な理解のゴール
* - runBlocking(これはjobではない)の中でのlaunch(job)の挙動
* - clientの内部
* - TBD: 色々
* */
fun main() {
    // let's ignore this part now. this is just starting a mock server.
    // this is not related to understanding how coroutine works.
    startMockServer()

    runBlocking {
        val client = HttpClient(Apache)
        val job = launch {
            val response = callExternal(client)
            println(response)
        }
        job.join()
        client.close()
    }
}

suspend fun callExternal(client: HttpClient): String {
    return client.request {
        url("http://localhost:3000/users/me")
        method = HttpMethod.Get
    }
}


@OptIn(ObsoleteCoroutinesApi::class)
fun startMockServer() {
    GlobalScope.launch {
        val serverThread = newSingleThreadContext("server")
        CoroutineScope(serverThread).
        embeddedServer(Netty, port = 3000) {
            routing {
                get("/users/me") {
                    delay(100) // DB I/O...
                    call.respondText("{\"id\": 123, \"name\": \"tom\"}")
                }
                get("/users/{userId}/posts") {
                    delay(100)
                    val userId = call.parameters["userId"]
                    call.respondText { """
                    [
                        { "id": 23, "userId": $userId, "title": "hello" },
                        { "id": 34, "userId": $userId, "title": "world" }
                    ]
                """.trimIndent() }
                }
                get("/comments") {
                    delay(100) // DB I/O...
                    val queryParameters: Parameters = call.request.queryParameters
                    val postIds: List<String> = queryParameters["postIds"]?.split(",")?.map { postId -> postId.trim() } ?: emptyList()
                    val comments = postIds.map { postId ->
                        """
                        { "id": ${UUID.randomUUID()}, "body": "comment-body", "postId": $postId }
                    """.trimIndent()
                    }

                    call.respondText { "[ ${comments.joinToString(",")} ]"}
                }
            }
        }.start(wait = true)
    }
}

