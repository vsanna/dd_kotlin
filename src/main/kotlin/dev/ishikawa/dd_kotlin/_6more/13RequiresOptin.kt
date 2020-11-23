@file:OptIn(MyDateTime::class)

package dev.ishikawa.dd_kotlin._6more

import java.time.LocalDateTime

//@OptIn(MyDateTime::class)
fun main() {
    val myDate = MyDate(LocalDateTime.now())
    val myDataClient = MyDataClient(md = myDate)
    println(myDataClient)
}

/*
特定のpublicAPIに対し、その利用には注意が必要であることを明示的に示せる
明示的にOpt-Inしない限り利用できない

Opt-Inの仕方
1. 利用する箇所でそのアノテーションを付ける
2. @OptIn(MyDateTime::class) で宣言
3. @file:OptIn(MyDateTime::class) をpackage前宣言でそのファイル全体で使える
4. module-wideでの許可もかぬ
* */

@RequiresOptIn(message = "this is experimental")
@Target(AnnotationTarget.CLASS)
annotation class MyDateTime


@MyDateTime
data class MyDate(val datetime: LocalDateTime){

}

//@OptIn(MyDateTime::class)
data class MyDataClient(
    val md: MyDate
)