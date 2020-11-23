package dev.ishikawa.dd_kotlin._1basic

fun main() {
    boxingEquality()
    primitiveNumConversion()
    division()
    bitwise()
    charcters()

    unsigned()
    strings()
}

fun boxingEquality() {
    val a: Int = 100
    val boxedA: Int? = a
    val anotherBoxedA: Int? = a

    val b: Int = 10000
    val boxedB: Int? = b
    val anotherBoxedB: Int? = b

    println(boxedA === anotherBoxedA) // true
    println(boxedB === anotherBoxedB) // false
}

fun bitwise() {
    // 100 | 001 => 101 == 5
    val x = (1 shl 2) or 0b000001
    println("bitwise:")
    println(x)
}

fun primitiveNumConversion() {
    val a = 10
    println(a.toLong())
    println(a.toByte())
    println(a.toFloat())
}

fun division() {
    println(5/2)
    println(5L/2)
    println(5.toDouble()/2)
}

fun charcters() {
    val c = 'd'
    if (c in 'a'..'z') {
        println("in!")
    } else {
        println("out!")
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
fun unsigned() {
    val u = 100u
    val ul = 100uL
}

fun strings() {
    val str = "hello world"
    str.forEach { println(it.javaClass) }
    println("val = " + 1)
    println("val = " + false)

    // 先頭の行に合わせて3行名はindentされる
    val text = """
        Hey, what is this!
        Are you fine?
            how about this?
    """.trimIndent()
    println(text)

    println("string template: $str")
    println("curly style: ${str.length}")
    println("$str.length is ${str.length}") // hello world.length is 11
}