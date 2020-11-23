package dev.ishikawa.dd_kotlin._2object

fun main() {

}

/*
* ifはabstract methodsと実装も持てる
* abstract classとの違いはこれ自体はstateを持てるか否か
* propertyも宣言化
* */

interface MyInterface {
    val name: String

    fun foo()
    fun bar() {
        println("bar")
    }
}

open class Child2: MyInterface {
    override val name: String = "hi"

    override fun foo() {
        TODO("Not yet implemented")
    }

    override fun bar() {
        super.bar()
    }
}

interface MyInterface2: MyInterface {
    fun hoge()
}

class Child3: MyInterface2 {
    override val name: String
        get() = TODO("Not yet implemented")

    override fun foo() {
        TODO("Not yet implemented")
    }

    override fun bar() {
        super.bar()
    }

    override fun hoge() {
        TODO("Not yet implemented")
    }
}

class Child4: Child2(), MyInterface2 {
    override val name: String
        get() = super.name

    override fun foo() {
        super.foo()
    }

    override fun bar() {
        // method conflictの解消
        super<MyInterface2>.bar()
        super<Child2>.bar()
    }

    override fun hoge() {
        TODO("Not yet implemented")
    }
}