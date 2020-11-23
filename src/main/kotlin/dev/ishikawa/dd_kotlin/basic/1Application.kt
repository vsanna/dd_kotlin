package dev.ishikawa.dd_kotlin.basic

fun main() {
    functions()
    variables()
    nullSafety()
    classes()
}

fun functions(): Unit {
    println("hello world")
    printMsg("hoge")

    println(sum(1, 20))
    println(multiple(10, 0))

    println("hello" onto "world")
    println(5 * "hoge")
}
// Unit == void of Java
fun printMsg(msg: String): Unit {
    println(msg)
}

fun sum(x: Int, y: Int): Int {
    return x + y
}

fun multiple(x: Int, y: Int): Int = x * y

infix fun String.onto(other: String) = Pair(this, other)


// override operator.
operator fun Int.times(msg: String) = msg.repeat(this)

fun variables() {
    // 型推論を積極的に使う
    // varよりvalが好まれる
    // val: 再代入できない. 定数とは別
    var a: String = "initial"
    var b = 123

    val c = 123

    var e: Int
//    println(e) // compile error

    // lateinit: NonNullなプロパティーの初期化をconstructorより後に遅らせられる機能
}

fun nullSafety() {
    var neverNull: String = "this cannot be null"
//    neverNull = null // nullのassignはできない
    // ? GCに明示的に開放対象だとどう伝える?

    var nullableString: String? = "can be null"
//    nullableString = null // ok

    // type inferは原則non-null
    val inferredNonNull = "hogehoge"

    fun strLen(str: String): Int {
        return str.length
    }

    strLen(neverNull)
//    strLen(nullableString) // null代入していなくてもcompile error
    if (nullableString != null) {
        strLen(nullableString) // 呼べる
    }
}

fun classes() {}
