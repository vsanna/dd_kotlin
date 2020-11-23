package dev.ishikawa.dd_kotlin._6more

fun main() {

}

/*
kotlinでNullpoがでうるのは
- javaへのアクセス
- !!の利用

null check: type checkと似たようなnarrowをしてくれる
val b: String? = "kotlin"
b != null && b.length > 0

safe calls: null返す
nullableVal?.callSomething

safe call を引数として渡したいとき: letつかう
nullable?.let { someFunc(it) }

Elvis operator
デフォルト値とかあてたいとき
val l: Int = b?.length ?: -1
val l: Int = b?.length ?: return null
val l: Int = b?.length ?: throw IllegalArgumentException("hgoe")

!! operator
nullableを強制的にunwrapする

safe cast
hoge as? String?

filterNotNullが便利
items.filterNotNull() でnull除外
* */