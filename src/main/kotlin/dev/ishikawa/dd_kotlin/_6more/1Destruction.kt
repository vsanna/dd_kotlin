package dev.ishikawa.dd_kotlin._6more


fun main() {
    val bob = Person2("bob", 100)
//    val (age, name) = bob
//    println("age = $age, name = $name") // age = bob, name = 100
    val (name, age) = bob
    println("age = $age, name = $name")   // age = 100, name = bob


    val users = listOf(Person2("1", 1), Person2("2", 2))
    for ((name, age) in users) {
        println("name = $name, age = $age")
    }

    // golangっぽいことをdestructで行う
    val (value, error) = hoge3()
    println("name = $value, age = $error")
}

/*
Destruction
1. object
    - componentNのちから
2. function
    - return several values from function
    - return valueのobjをdestructすることで擬似的に複数値を返す
    - val (_, age) = Person() でcomponent1()はcallされない(ただ変数を捨てるのではなくパフォーマンス向上させる)
3. map
    - for((k, v) in map) {}
4. lambdaの引数
    - {(a, b), c -> }のやつ
* */

data class Person2(val name: String, val age: Int)

fun hoge3(): Person2 {
    return Person2("hoge", 123)
}