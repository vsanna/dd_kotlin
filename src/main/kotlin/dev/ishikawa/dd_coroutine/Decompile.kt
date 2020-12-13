package dev.ishikawa.dd_coroutine

import kotlinx.coroutines.*
import java.util.*


fun main() {
    GlobalScope.launch {
        launch {
            delay(10)

            createPost2(Item("title2"))
        }
        createPost2(Item("title"))
    }

    Dispatchers.IO
}

data class Item (
    val title: String
)

data class Post2 (
    val subject: String
)

suspend fun createPost2(item: Item): Post2 {
    val token = getToken()
    val post = makePost2(item, token)
    return postProcess(post)
}


suspend fun getToken(): String {
    delayWithExtraSuspend()
    return UUID.randomUUID().toString()
}

suspend fun makePost2(item: Item, token: String): Post2 {
    delay(100)
    println(token)
    return Post2(subject = item.title)
}

fun postProcess(post: Post2): Post2 {
    return post.copy(subject = post.subject + "2")
}

suspend fun delayWithExtraSuspend() {
    println("additional suspend")
    delay(100)
}
