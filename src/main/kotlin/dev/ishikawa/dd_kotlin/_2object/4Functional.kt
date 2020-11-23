package dev.ishikawa.dd_kotlin._2object

fun main() {
    val isEven = IntPredicate { it % 2 == 0 }
    println(isEven.accept(100))

    val comp = IntCompare { a, b -> a > b }
    println(comp.compare(100, 200))

}

/*
* 1つだけ関数の定義をしているinterfaceをFunctional interface または SingleAbstractMethod interface と呼ぶ
* いわゆるこれまでのnew Runnable(() -> {})のような@FunctionalInterfaceにはこちらを使うkotlin1.4から
*
* 12Objectで述べている、以下のような記述はもう不要
*
* @FunctionalInterface
* interface Hoge {
*   fun onlyonemethod()
* }
*
* // lambdaが使えなかった。代わりに無名クラスを定義する必要がある
* val hoge = object: Hoge {
*   override fun onlyonemethods() {
*     // do something
*   }
* }
* hoge.onlyonemethods()
* */

fun interface MyRunnable {
    fun invoke()
}

fun interface IntPredicate {
    fun accept(i: Int): Boolean
}

fun interface IntCompare {
    fun compare(a: Int, b: Int): Boolean
}

