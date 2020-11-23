package dev.ishikawa.dd_kotlin._3function

fun main() {
    hoge2("hello", var3 = "hoge")

    "hello! " hello "world"

    varargsfun("hoge", "geho", "gaho")

    geho(100, -1.0f)

    println(tailrecfun("hello", 1000000000))
}

/*
普通のmethod
top-level function
extension
infix
inline
tailrec
lambda
annonymous

- parameter: 複雑過ぎ。IDEに従う
    - default value
    - positional
    - keyword
    - varargs
        - varargs items: T は内部で Array<T>として扱える
    - spread passing: listをばらして渡せる. jsで言うところの ...array
        - func(*list) == func(list[0], list[1], ...)
- Unit: void相当
- infix
    - extentionにつけられる。中間演算子として呼び出せる
    - default value持てない he-
- Generic
    - funの直後に必要な型変数を宣言fun <T>
- inline function
    - function/inlineで記述
- extension functions
    - object/Extensionsで記述
- tailrec: 末尾再帰
    - stack overflowせずに再帰関数をかける
    - 条件: 自身を一番最後に呼び出す。
* */

fun hoge2(var1: String, var2: String = "var2", var3: String, var4: String = "var4") {
    println(listOf(var1, var2, var3))
}

fun varargsfun(vararg messages: String) {
    messages.forEach { println(it) }
}

infix fun String.hello(world: String) {
    println("$this $world")
}

fun <T, S> geho(var1: T, var2: S) {
    println("var1 = $var1, var2 = $var2")
}

// NOTE: this is an incorerct example
tailrec fun tailrecfun(msg: String, n: Int): Int {
    if(n < 1) return 1
    return tailrecfun(msg, n/2)
}