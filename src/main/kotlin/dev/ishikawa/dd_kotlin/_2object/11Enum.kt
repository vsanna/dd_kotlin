package dev.ishikawa.dd_kotlin._2object


fun main() {
    println(Color2.RED.signal())
    println(Color2.GREEN.signal())
    println(Color2.BLUE.signal())

    println(Color2.valueOf("RED"))
    println(Color2.values())

    Hoge.VAL1(123)
    Hoge.VAL2("hello")

}

/*


* */

enum class Direction {UP, DOWN, RIGHT, LEFT}

enum class Color2(val rgb: Int) {
    RED(0xfff0000) {
        // 個別には定義できない。abstructのoverrideのみ
//        fun onlyredhas(): Boolean = true
        override fun signal(): Int = 0
    },
    GREEN(0x00ff00) {
        override fun signal(): Int = 1
    },
    BLUE(0x0000ff) {
        override fun signal(): Int = 2
    };

    abstract fun signal(): Int
}

// inline??
// reified??
inline fun <reified T: Enum<T>> pringAllValues() {
    println(enumValues<T>().joinToString { it.name })
}

enum class Hoge {
    VAL1,VAL2;

    // Hoge.VAL1(123) というシンタックス
    operator fun invoke(n: Int) {
        println(this.name + n.toString())
    }

    operator fun invoke(msg: String) {
        println(this.ordinal.toString() + msg)
    }
}
