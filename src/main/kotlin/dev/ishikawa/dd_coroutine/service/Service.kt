package dev.ishikawa.dd_coroutine.service

import dev.ishikawa.dd_coroutine.mockapiio.*

class Service(
    val apiClient: MockApi = MockApi()
) {
    fun getUser(userId: Long? = null): User = apiClient.getUser(userId)
    fun getUsers(num: Int = 5): List<User> = apiClient.getUsers(num)
    fun getPosts(num: Int = 5, userId: Long): List<Post> = apiClient.getPosts(num, userId)
    fun getComments(num: Int = 2, postId: Long): List<Comment> = apiClient.getComments(num, postId)

    suspend fun getUserSus(userId: Long? = null): User = apiClient.getUser(userId)
    suspend fun getUsersSus(num: Int = 5): List<User> = apiClient.getUsers(num)
    suspend fun getPostsSus(num: Int = 5, userId: Long): List<Post> = apiClient.getPosts(num, userId)
    suspend fun getCommentsSus(num: Int = 2, postId: Long): List<Comment> = apiClient.getComments(num, postId)
}