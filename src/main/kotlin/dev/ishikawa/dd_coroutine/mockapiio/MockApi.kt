package dev.ishikawa.dd_coroutine.mockapiio

import com.github.javafaker.Faker
import dev.ishikawa.dd_coroutine.util.showDebug

data class User(
    val id: Long,
    val name: String,
)
private fun randomUser(id: Long? = null, name: String? = null): User {
    return User(id ?: faker.number().randomNumber(), name ?: faker.name().name())
}

data class Post(
    val id: Long,
    val userId: Long,
    val title: String,
)
private fun randomPost(id: Long? = null, title: String? = null, userId: Long? = null): Post {
    return Post(id ?: faker.number().randomNumber(), userId ?: faker.number().randomNumber(),title ?: faker.book().title())
}

data class Comment(
    val id: Long,
    val postId: Long,
    val body: String,
)
private fun randomComment(id: Long? = null, body: String? = null, postId: Long? = null): Comment {
    return Comment(id ?: faker.number().randomNumber(), postId ?: faker.number().randomNumber(), body ?: faker.book().title())
}


private val faker: Faker = Faker()


class MockApi {
    fun getUser(userId: Long? = null): User{
        Thread.sleep(1000)
        showDebug()
        return randomUser(id = userId)
    }

    fun getUsers(num: Int = 3): List<User> {
        Thread.sleep(1000)
        showDebug()
        return List(num) {
            randomUser()
        }
    }

    fun getPosts(num: Int = 3, userId: Long): List<Post> {
        Thread.sleep(1000)
        showDebug()
        return List(num) {
            randomPost(userId = userId)
        }
    }

    fun getComments(num: Int = 3, postId: Long): List<Comment> {
        Thread.sleep(1000)
        showDebug()
        return List(num) {
            randomComment(postId = postId)
        }
    }
}
