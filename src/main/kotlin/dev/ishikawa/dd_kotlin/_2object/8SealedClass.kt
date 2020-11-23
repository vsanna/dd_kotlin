package dev.ishikawa.dd_kotlin._2object

fun main() {
    val r = when(trySomething()) {
        is Result.Success -> "success!"
        is Result.Failure -> "failure.."
        // elseいらない!

        // かけているbranchがあるとerror
        is Result.Unknown -> TODO()
    }
    println(r)
}

/*
SealedClass
クラス階層を制限することができる。
- sealed classの小クラスは同一ファイルにいる必要がある

usecases: https://qiita.com/kikuchy/items/ad89a12029082be8d218
1. カスタム値も表現できるenum
    - よくわからなかった
2. 値の重ね合わせ
    - Resultをsealedクラスとし、それを継承したSuccess/Failureを定義する
    - clientはResultで受け取ったものをSuccess/Failureかのみ型チェックすれば良い. whenと相性が良い
* */

open class Expr
data class Const(val number: Double): Expr()
data class Sum(val e1: Expr, val e2: Expr): Expr()
object NotANumber: Expr()

sealed class Color {
    object Red: Color()
    object Blue: Color()

    data class Rdb(val red: Int, val green: Int, val blue: Int): Color()
}

sealed class Result {
    data class Success(val code: Int): Result()
    data class Failure(val code: Int): Result()
    data class Unknown(val code: Int) : Result()
}

fun trySomething(): Result {
    return Result.Success(123)
}