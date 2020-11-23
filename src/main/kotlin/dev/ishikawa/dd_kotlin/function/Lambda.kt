package dev.ishikawa.dd_kotlin.function

fun main() {

    val items = listOf(1,2,3,4,54,5,6,6)
    println(items.fold(0, { acc, next -> acc + next }))


    val mf:MyFunc = {a,b -> a + b}
    println(mf())

    val mf2: MyFunc = fun(num1: Int, num2: Int): Int { return num1 - num2 }
    mf2()
}


/*
# Higher-Order Functions: 高階関数 & Lambda

Kotlinのfunctionは "first-class" = 変数に格納できる / 関数の引数戻り地になれる

1. Higher-Order Functions: 高階関数
    - 関数を引数として受け取る、または関数を戻り地として返す関数
2. functionの型表現
    - typealias MyFunc = (a: Int, b: Int) -> Int
    - typealias MyFuncSuspend = suspend () -> Unit
    - functionの型のインスタンス取得
        - lambdaで定義
        - annonymous function
* */


// 高階関数の例
fun <T, R> Collection<T>.fold(initial: R, combine: (acc: R, nextElement: T) -> R): R {
    var accumulator: R = initial
    for(element: T in this) {
        accumulator = combine(accumulator, element)
    }
    return accumulator
}

typealias MyFunc = (a: Int, b: Int) -> Int
typealias MyFuncSuspend = suspend () -> Unit