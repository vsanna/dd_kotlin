package dev.ishikawa.dd_kotlin._2object

fun main() {
    val user = User2("hoge", 12)
    println(user.component1())
    println(user.component2())

    val user2 = User2("hoge", 12)
    println(user == user2)  // true!!!
    println(user === user2) // false!

    val user3 = user.copy(name = "bob")

    var (name, age) = user3
    println("name = $name, age = $age")


    println(user == user3) // false

    // component3がないのでこれはできない!!
//    var (name2, gender2, age2) = user3
//    println("name = $name2, age = $age2, gender = $gender2")


}

/*
 * データ保持目的のクラス
 * equals / hashCode / toString / copy / componentN を提供
 * componentN: propertiesの定義順で作られる.
 *
 * Data classes cannot be abstract, open, sealed or inner;
 *
 * primary constructorに入れたくないpropertyはbodyに入れる
 *
 * copy: toBuilderもう不要
 *
 * equals: hashcodeが同一ならtrueを返す(これってアンチパターンじゃないっけ?衝突可能性あるから。)
 * と思ったらstructural equalityは == / equals / hashCodeで表現する
 * referential equalityは === で表現
 * 	val user1 = User("poko")
    val user2 = User("poko")

    // structural equality
    println(user1.hashCode())    // 3446691
    println(user2.hashCode())    // 3446691
    println(user1 == user2)      // true
    println(user1.equals(user2)) // true

    // referential equality
    println(user1 === user2)     // false
 *
 * data class destruction
 * var (name, age) = user3
 * println("name = $name, age = $age")
 * note!: bodyで定義したgenderにはcomponent3が生えない。destructにはそれが必要なのでgenderはとれない
 * */

data class User2(val name: String, val age: Int) {
    val gender: Int = 0
}