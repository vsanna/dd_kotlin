package dev.ishikawa.dd_kotlin._6more

fun main() {
  val obj: Any = "hogehoge"
  if (obj is String) {
    println(obj.length)
  }

  if (obj is String && obj[0] == 'h') {
    println("he-")
  }

  when(obj) {
    is String -> println(obj.length)
    else -> println("hello")
  }


  val y: Any = "hoge"
//  val y: Any = 10 // throw ClassCastException
//  val x: String = y as String
  val x: String? = y as String?
  println(x)

  val z: Any = 10
  val x2: String? = z as? String
  println(x2)
}

/*
smart cast
if, when, 論理演算などの駆使でいい感じにtypeをnarrowingしてくれる

unsafe cast operator
val x: String = obj as String
通常このasはcastできない場合exceptionを実行時に返す。故にunsafe cast.

safe cast operator
仮にcastできないとき、nullをセットする

type erasure
kotlinはcompile時の型安全は保証する一方、実行時はそうでもない
Generic型のインスタンスは、実際にはどういった型変数をもつのかという情報を持たない
(List<T> は実行時にはすべて List<*> に置き換えられる)
TODO: よくわからん
* */
