package dev.ishikawa.dd_kotlin.`object`


fun main() {
    val outer = Outer()
    val nested = Outer.Nested()

    println(nested.foo())

    val inner = outer.Inner()
    // outer.new Inner()に相当
    inner.geho()

    val value = object : Outer() {
        override fun hoge() {
            println("annoymous")
        }
    }
    value.hoge()

    val fd = functionalDesu { println("gogogo") }
    fd.gogo()


    val fd2 = FunctionalDemo { println("hoge") }
    fd2.trigger()
}

/*
1. 普通のnested -> static nested 相当
2. inner: いわゆるインスタンスに属するクラス。
3. annoymous class: "object expression"を使って表現可能
    - Javaでは派生元がfunctional interfaceであればlambdaで定義できるが、kotlinではできない
    - helper関数をかませる
* */

open class Outer {
    private val bar: Int = 1
    open fun hoge() {
        println("outer hoge")
    }

    class Nested {
        fun foo() = 2
    }

    inner class Inner {
        fun geho() {
            println(this)
            println(this@Outer)
            // javaのこれに相当
//            System.out.println(this.foo);
//            System.out.println(JavaSandbox.this);
        }
    }
}

interface FunctionalDesu {
    fun gogo() {
        println("default impl")
    }
}

// こういうhelper関数を用意しておく
fun functionalDesu(f: () -> Unit): FunctionalDesu {
    return object : FunctionalDesu {
        override fun gogo() = f()
    }
}

// fun interface??
fun interface FunctionalDemo {
    fun trigger()
}
