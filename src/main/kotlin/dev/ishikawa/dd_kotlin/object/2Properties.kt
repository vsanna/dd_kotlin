package dev.ishikawa.dd_kotlin.`object`

fun main() {
    val properties = Properties("name", 100)
    println(properties.name)
    println(properties.age)
//    properties.age = 10
    properties.name = "updated"
    println(properties.name)
    println(CompileTimeConsts.hoge)
}

/*
properties: field + getter/setterをあわせたもの
* varで定義すればsetter/getter
* valで定義すればgetterのみ
* kotlinではproperty accessor(getter/setter)内部でのみ使用可能なfieldを
* backing filedとして呼ぶ.
* "field"としてgetter/setter内で読める
* */

class Properties(
    var name: String,
    val age: Int
)

/*
* getter/setterをカスタムすることができる
* */
class CustomAccessor {
    var name: String = ""
        //
        get() {
            println("hey")
//            return this.name
            return field
        }
        set(value) {
            println("set")
//            this.name = value
            field = value
        }

    // filedは、counter propertyのbacking fieldを指す。
    var counter = 0
        set(value) {
            if (value >= 0) field = value
        }
}

class Sandbox {
    fun hoge() {
//        const val hoge = 1
//        println(hoge)
    }
}


/*
* Compile-Time Constants
* const をつけた値はcompile時に単純に置換される
* const をつけられる条件
* 1. top-level, object declaration, companion objectのどこかで定義されている
* 2. stringまたはprimitive valueで初期化されている
* 3. カスタムゲッターがない
* */

class CompileTimeConsts {
    companion object {
        // varはだめ
//        const var hoge = "VALUE"
        const val hoge = "VALUE"
    }
}


/*
* lateinit
* nonnullの値はconstructor内で初期化される必要があるが、得てしてこれが困難なことがある
* java -> nullcheckを受け入れるしかなかった
* kotlinえはlateinitが使える
* */