package dev.ishikawa.dd_kotlin._3function

fun main() {

    val items = listOf(1,2,3,4,54,5,6,6)
    println(items.fold(0, { acc, next -> acc + next }))


    val mf:MyFunc = {a,b -> a + b}
    println(mf(1, 2))

    val mf2: MyFunc = fun(num1: Int, num2: Int): Int { return num1 - num2 }
    mf2(3, 4)

    println(String::hello1)

    println(String::hello1.invoke("hello", "world"))


    val ints = listOf(1,2,3,4,45,5,)
    println(ints.filter { it > 0 })
    // 同値
    println(ints.filter { return@filter it > 0 })

    // main関数自体を終わらせる
//    println(ints.filter { return@main })


    val closure = createClosure()
    println(closure(0))
    println(closure(10))
    println(closure(21))
    println(closure(-120))

    val sum: Int.(Int) -> Int = {other -> this + other }
    println(sum.invoke(10, 20))
}


/*
# Higher-Order Functions: 高階関数 & Lambda

Kotlinのfunctionは "first-class" = 変数に格納できる / 関数の引数戻り地になれる

1. Higher-Order Functions: 高階関数
    - 関数を引数として受け取る、または関数を戻り地として返す関数
2. functionの型表現
    - typealias MyFunc = (a: Int, b: Int) -> Int
    - typealias MyFuncSuspend = suspend () -> Unit
3. functionの型のインスタンス取得
    - lambdaで定義
    - annonymous function
4. callableへのreference
    - String::hello1などなど
5. そのinvoke
    - String::hello1.invoke("hoge", "geho")

kotlin convention: 最後の引数がlambdaの場合、
hoge(var1, var2) {
    // do something
}
という記法を使える
これはメソッドチェーンもできる(確かにおかしな記法だ)

it: 暗黙的な単一引数への参照! 決してthisではない
apply/letでitがでてくるのはなぜ？

qualified returnを使うとlambdaの中でもreturn使える
    // 同値
    println(ints.filter { it > 0 })
    println(ints.filter { return@filter it > 0 })
    // main関数自体を終わらせる
    println(ints.filter { return@main })

使わない引数には_を使う

lambda 引数のdestruction
val somefunc = {(a, b), c -> }
=> somefunc(obj, anotherparam)と呼べる(objはdestructされる)


closureを作れる


Function literal with receiver
A.(B) -> C で表現可能

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

fun String.hello1(other: String): String {
    return "$this $other"
}

fun createClosure(): (a: Int) -> Int {
    var total = 0;
    return {num ->
        total += num
        total
    }
}