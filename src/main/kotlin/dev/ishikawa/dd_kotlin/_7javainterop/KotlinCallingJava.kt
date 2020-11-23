@file:JvmName("KotlinCallingJavaUtil")
@file:JvmMultifileClass

package dev.ishikawa.dd_kotlin._7javainterop


data class KotlinCalledByJava(val name: String)

class KotlinCallingJava {
    fun hello() {
        val javaSandbox = JavaSandbox()
        val inner = javaSandbox.Inner()
        println(inner.foo)
        JavaSandbox.test()
    }
}

fun topLevelFunc(): String {
    return "hogehoge"
}

fun main() {
    KotlinCallingJava().hello()
    val hello = JavaCallingKotlin().hello()
    println(hello)
}