package dev.ishikawa.dd_kotlin._2object

fun main() {

}

/*
typealias: 単に別名をつけるだけ。長い名前を省略するために使う
IntにPriceとつける(ことで方の安全性を増す)ようなものではない
* */

typealias CountryName = String
typealias SSet = Set<String>
typealias MyHandler = (Int, String) -> Unit
typealias Predicate<T> = (T) -> Boolean


class A2 {
    inner class Inner

    class Nested
}
typealias A2Inner = A2.Inner
typealias A2Nested = A2.Nested