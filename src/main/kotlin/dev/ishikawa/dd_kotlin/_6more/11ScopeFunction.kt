package dev.ishikawa.dd_kotlin._6more

import dev.ishikawa.dd_kotlin._2object.User

fun main() {
    val user = User6("hoge").apply {
        println("name = ${name}")
    }
    println(user)

    val a = user.run {
        println("name = $name")
        "hello"
    }
    println(a)

    val b = user.let {
        println("name = ${it.name}")
        "world"
    }
    println(b)

    val user2 = User6("geho").also {
        println("name = $it.name")
        "gehogeho"
    }
    println(user2)

    with(user2) {
        println("name = $name")
    }


    val user3 = user2.takeIf { it.name.startsWith("W") }
}

/*
Scope Functions
特定のobjectをcontextとしてもつ中での処理を行う関数。
そのscope内では対象のobjectに名前無し(といいつつit)でアクセスできる
let run with apply also

単にreadabilityのための機能

どれも似ているが、違いが出る軸は次の2つ
1. context objectの参照方法
    - thisでアクセス: run, with, apply
        - より短いコードにするにはこちら。(thisは省略可能)
        - 主にそのobjectのmemberにアクセスする場合はこちら
    - itでアクセス: let, also
        - itなのでreceiverを明示化できる。長くなるが見間違え少ない
2. return value
    - return context object: apply also
    - return lambdaのresult: let, run, with

これは覚えられねー

let: 戻り値がlambdaの値なので、値を変え続けるchain向き
apply: builder向き
run: initしつつcomputationを返すケース。いつ使うか..
also: builderチェーンの途中にdebugいれるのに使えそう

takeIf/takeUnless
条件付き代入に便利
* */