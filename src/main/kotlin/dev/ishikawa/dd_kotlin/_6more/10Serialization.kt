package dev.ishikawa.dd_kotlin._6more

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/*
Serialization

- kotlinx.serializationはjvm/js/native.json.cbor/protocol buffers などなどのシリアライゼーションツール用意
- GsonはDeprecated. Gson v3のMoshiというrepoはある。

ex: JSON serialization
* */



fun main() {
    val user = User6("name")
    val stringUser = Json.encodeToString(user)
    val userDecoded = Json.decodeFromString<User6>(stringUser)
    println(stringUser)
    println(userDecoded)

    val users = listOf(User6("name1"), User6("name2"), User6("name3"))
    val stringUsers = Json.encodeToString(users)
    val usersDecoded = Json.decodeFromString<List<User6>>(stringUsers)
    println(stringUsers)
    println(usersDecoded)
}

@Serializable
data class User6(var name: String)