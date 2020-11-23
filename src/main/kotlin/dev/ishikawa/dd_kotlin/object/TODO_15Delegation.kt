package dev.ishikawa.dd_kotlin.`object`

import kotlin.properties.Delegates
import kotlin.reflect.KProperty

fun main() {
    val b = BaseImpl(100)
    val d = Derived(b)
    d.print()

    d.print2()
    d.b.print2()

    d.print3()
    d.b.print3()

    println("=========")

    val e = ExamplePropertyDelegation()
    println(e.p)

    e.p = "NEW"
    println(e.p)

    println(lazyValue)
    println(lazyValue)
//    lazyValue = "NEWWW"

    obserbableValue = "NEW"
    obserbableValue = "new2"
    obserbableValue = "New3"
    println(obserbableValue)


    val user = User3()
    println(user.oldName)


    val usermap = UserMap(mapOf(
        "name" to "tom",
        "age" to 100,
        "nullable" to 200
    ))
    println(usermap)

    // error! Key age is missingがでる
//    val usermap2 = UserMap(mapOf(
//        "name" to "tom",
//        "age2" to 100,
//        "nullable" to 200,
//    ))
//    println(usermap2)

//    val usermap3 = UserMap(mapOf(
//        "name" to "tom",
//        "age" to 100,
//    ))
//    println(usermap3)
}

/*
# Delegation
継承よりも移譲!

- impl by delegation
    - delegation相手として持つpropertyを経由してそのメソッドを呼べる
    - fun print() { b.print() } とかいちいち書かなくていい
- delegation properties
    - 共通のふるまいとしてlibに入れておきたいpropertiesの挙動があったりする。それを実現
    - そのpropertyに対するgetValue/SetValueを持つクラスを指定する
    - これ用のDelegate相当クラスを作るfactoryメソッドをkotlinはいくつか提供
        - lazy:
            - 初回のget時に一度だけ渡したlambdaを実行。その結果を覚えてその後は利用
            - setterなし。再代入できない
        - Delegates.observable()
            - initial value, modifier
            - modifierはassignmentが終わったあとに呼ばれる
            - 本来はここでlistenerに向けてnotifyする
        - notnull
        - vetoable
            - modifierにあたるlambdaでbooleanを返す。falseならそのsetを受け入れない
- delegation to another property
    - propertyのalias的に振る舞う. backward compatibilityのための機能
- Storing properties in a map
    - mapを受け取ってそれにpropertiesをdelegateできる
    - propertiesがnullableであろうがそのkeyがmapにないとエラー
* */


interface Base {
    val message: String
    fun print()
    fun print2()
    fun print3()
}

class BaseImpl(val x: Int): Base {
    override val message = "baseimpl"

    override fun print() {
        println(x)
    }

    override fun print2() {
        println("in baseimpl")
    }

    override fun print3() {
        println("msg = $message")
    }
}

// delegation相手をpropertyに持ちつつ、Baseをそれ経由で継承
class Derived(val b: Base): Base by b {
    override val message = "derived"
    override fun print2() {
        println("in derived")
    }

    override fun print3() {
        println("msg in derived = $message")
    }
}


class ExamplePropertyDelegation {
    var p: String by Delegate()
}

class Delegate {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return "$thisRef, thank you for delegating '${property.name}' to me!"
    }
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        println("$value has been assigned to '${property.name}' in $thisRef.")
    }
}

val lazyValue: String by lazy {
    println("computed")
    "Hello"
}

var obserbableValue: String by Delegates.observable("initial value") {
    property, oldValue, newValue ->
    println(property.name)
    // listeners.forEach(listener -> listener.notified(newValue))
}

class User3 {
    val newName: Int = 0
    @Deprecated("User 'newName' instead", ReplaceWith("newName"))
    val oldName: Int by this::newName
}

class UserMap(map: Map<String, Any?>) {
    val name: String by map
    val age: Int by map
    val nullable: Int? by map

    override fun toString(): String {
        return "{name=$name, age=$age, nullable=$nullable}"
    }
}