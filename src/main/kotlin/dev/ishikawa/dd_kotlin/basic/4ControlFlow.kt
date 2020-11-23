package dev.ishikawa.dd_kotlin.basic

fun main() {
    _if()
    _when()
    _loop()
    _label()
    _label2()
}

fun _if(): Int {
    val a = 1
    var result = if (a > 10) {
        100
    } else {
        -100
    }
    println(result)

    // 三項演算子
    result = if(a < 10) 3 else -3
    println(result)

    return result
}

// when gets return, it returns its then clause
fun _when() {
    var x: Any = 1
    when(x) {
        1 -> println("this is 1")
        3 -> println("this is 3")
        in -1..100 -> println("in some range") // this line doesn't be shown
        !in -1..100 -> println("not in the range")
        is Int -> println("this is Int")
        else -> println("unknown")
    }

    /*
    * web app example
    *
    * Request.getBody() = when(val response = executeSomeRequest()) {
    *   is Success -> response.body
    *   is HttpError -> throw HttpExecution(response.status)
    * }
    *
    * */
}

fun _loop() {
    for (i in 1..15) {
        if(i < 3) continue
        print(i)
        if (i>5) break
    }

    val arr = arrayOf(1,2,3,4,5)
    arr.forEachIndexed { index, i ->  println("idx=$index, val=$i")}
}

fun _label() {
    out@ for(i in 0..100) {
        for(j in 0..100) {
            if(j > 10) break@out
            println("i=$i, j=$j")
        }
    }

    listOf(1,2,3,4,5).forEach lit@{
        if(it == 3) return@lit
        print(it)
    }
    println()
    listOf(1,2,3,4,5).forEach {
        if(it == 3) return@forEach
        print(it)
    }
}

// return-expression in this lambda returns from _label2 function.
fun _label2() {
    println()
    listOf(10,2,3,4,5).forEach{
        if(it == 3) return
        print(it)
    }
}