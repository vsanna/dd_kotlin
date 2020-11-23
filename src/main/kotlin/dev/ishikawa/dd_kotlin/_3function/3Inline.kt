package dev.ishikawa.dd_kotlin._3function

import jdk.jfr.Experimental
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantReadWriteLock

fun main() {
    val l: Lock = ReentrantReadWriteLock().readLock()
    lock(l) { "hello" }

    lock2(l, { "hello" }, { "world" })


    printAnnotations<User4>()
}

/*
高階関数の利用は実行時にいくつかの欠点をもたらす: ここがobjectなのでそれぞれメモリを占める
それらを解決するのが関数のinline化

lock(l) { foo() } // 引数としてlambda objを生成している
↓
l.lock()
try {
    foo()
} catch {
    l.unlock()
}
と書けばメモリ利用を回避可能

とはいえ面倒. そこでinline化
注意! inline = 展開して埋め込みなのでcompileあとのコードは大きくなる

渡すlambdaのうち、一部だけをinlineにすることもできる

lambdaの中でreturnはできない
lambdaを脱出するにはlabel/qualifierつきreturnが必須
ただし、その関数に渡されたlambdaがinline化されていればreturnで脱出できる
inline == 展開して埋め込み、と考えると当然


# reified(具象化された) type params
javaでいうClass<T>の受け渡し. 単にちょっと見た目がよくなる
reifiedを使うにはinlineである必要がある


inline propertyというものもある。
* */


inline fun <T> lock(lock: Lock, body: () -> T): T {
    lock.lock()
    try {
        return body()
    } finally {
        lock.unlock()
    }
}

inline fun <T> lock2(lock: Lock, body1: () -> T, noinline body2: () -> T): T {
    lock.lock()
    try {
        body1()
        return body2()
    } finally {
        lock.unlock()
    }
}


inline fun <reified T> printAnnotations() {
    val annotations = T::class.java.annotations
    for (annotatedInterface in annotations) {
        println(annotatedInterface)
    }
}

@FunctionalInterface
@Experimental
class User4