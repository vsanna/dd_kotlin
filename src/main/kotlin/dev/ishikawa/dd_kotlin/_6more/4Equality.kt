package dev.ishikawa.dd_kotlin._6more

import java.lang.Float.min
import kotlin.math.abs


fun main() {
    val a: Float = 0.1f + 0.1f + 0.1f +0.1f +0.1f +0.1f +0.1f +0.1f + 0.1f +0.1f
    val b = 1.0f
    println(a)       // 0.999999999
    println(b)       // 1.0
    println(a === b) // false(無意味)
    println(a == b)  // false
    println(a.compareTo(b)) // 1


    // プログラミングの一般的な問題としてfloat/doubleの比較は極めて小さい誤差(epsilon)の範囲に収まるかを比較するのが常
    // なぜなら浮動小数点は定義からして正確ではないから

    // TODO: false返すぞこれ
//    println(a.nearlyEqual(b))
}
/*
Structural Equality
値としてのひとしさ。==, equalsはこちら
equalsの実装はhashCodeの等しさ

Referential Equality
=== をつかう

Floating point numbers equality
IEEE754の比較に従う = 数値をbit列にして比較
大小も辞書順で比較できる(本当?)
* */

fun Float.nearlyEqual(other: Float, epsilon: Float = Float.MIN_VALUE): Boolean {
    if (this == other) return true

    val absThis = abs(this)
    val absOther = abs(other)
    val diff = Math.abs(absThis - absOther)

    if (this == 0.0f || other == 0.0f || (absThis + absOther < Float.MIN_VALUE)) {
        return diff < (epsilon * Float.MIN_VALUE)
    } else {
        return (diff / min(absThis + absOther, Float.MAX_VALUE)) < epsilon
    }
}