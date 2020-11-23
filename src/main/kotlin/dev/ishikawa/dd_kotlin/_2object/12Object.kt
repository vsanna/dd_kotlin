package dev.ishikawa.dd_kotlin._2object

fun main() {
    val runnable = Runnable { println("hello") }
    val runnable2 = object : Runnable {
        override fun run() {
            println("world")
        }
    }
    val runnable3 = Runnable { println("!!") }
    runnable.run()
    runnable2.run()
    runnable3.run()

    // 便利だが外には渡せない
    val adhocObj = object {
        val name: String = "hoge"
        val age: Int = 21
        override fun toString(): String {
            return "{name: $name, age: $age}"
        }
    }
    println(adhocObj)

    // MySingletonはsingletonのインスタンス
    MySingleton.hello()
}

/*
既存クラスを少し編集したもののobjectがほしいことがある。ただしそのために新クラスを定義したくはない
そういうときにobjectが使える
つまるところJavaで言うところの無名クラスの即時インスタンス化

用途
1. Javaで言うところの無名クラスの即時インスタンス化. Runnableの実装とか。
2. staticの代わり: companion object
3. なんならmapの代わり(これは便利)
    - ただしlocalでしか使えない。外に出るときには継承しているクラスまたはAnyになる. うーむ
4. singletonの定義
* */

object MySingleton {
    const val name = "singleton"

    fun hello() {
        println(name)
    }
}

