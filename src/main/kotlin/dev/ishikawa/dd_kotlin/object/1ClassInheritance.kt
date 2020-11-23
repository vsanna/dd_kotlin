package dev.ishikawa.dd_kotlin.`object`

/*
classと名のつくもの
class
abstruct class
data class
nested class
inner class
annoymous class
enum
object class
object companion class
inline class
**/

fun main() {
    println(Invoice())
    println("====")

    println(ClassDoesntNeedToHaveBody())
    println("====")

    println(InitOrderDemo("tom", "21"))
    println("=====")

    println(Person("name"))
    println("=====")

    // no public constructor
//    println(NoPublicConstructor())

    println(NoArgsClass())
    println("=====")

    val b = B()
    println("name = ${b.name}, age = ${b.age}")
    println("=====")

    println(Square().draw())
    println("=====")

    println(WithCompanion.create())
    println(WithCompanion)
    println("=====")
}

class Invoice {}

class ClassDoesntNeedToHaveBody

class Person(
    // primary constructor
    name: String
)

/*
* calling order:
* 0. superclass initialization
* 1. primary constructor
* 2. props & init: in appearing order
* 3. secondary constructor
*
* if constructor needs to have any annotation(not for props, but for constructor itself),
* constructor keyword is needed
* ex: InitOrderDemo @Inject constructor(name: String) {}
* */
class InitOrderDemo(name: String = "default name".apply(::println)) {
    val firstProperty = "first prop: $name".also(::println)

    init {
        println("init")
    }

    val secondProperty = "second prop: ${name.length}".also(::println)

    init {
        println("init2")
    }

    // secondary constructor
    // if the class has primary constructor, secondary constructors must delegate to the primary one.
    constructor(name: String, age: String): this() {
        println("age is $age")
    }
}

class NoPublicConstructor private constructor() {

}

// for jpa/jackson which needs noargs constructor
class NoArgsClass(name: String = "default name", age: Int = 100)


/*
# Classes can contain:

1. Constructors and initializer blocks
2. Functions
3. Properties
4. Nested and Inner Classes
5. Object Declarations
* */

/*
* # Inheritance
* - すべてのクラスはAnyを継承. Anyはequals, hashCode, toStringを持つ。Object的なもの。
* - デフォルトでkotlinのクラスはfinal = つまり継承不可。
* - openをつけると継承可能
* - 小クラスがprimary constructorを持つなら -> そのとなりでparentをinit
* - 小クラスがprimary constructorを持たないなら -> constructorの隣でinit
*
* - methodも基本はfinal. overrideを許容するものはopenをつける
* - overrideしたメソッドは基本的にopen. もし更にその下でのoverrideを防ぐ場合はfinalをつけて明示する
*
* - propertyのoverrideも同様。
* */

class ParentCannotBeInherited {}
//class Child: ParentCannotBeInherited{}

open class Parent(name: String) {
    fun cannotBeOverriden() {}

    open fun canBeOverriden() {}
}
//class Child: Parent {
//    constructor(myname: String, parentName: String): super(parentName) {
//    }
//}
class Child(myname: String, parentName: String): Parent(parentName) {
    override fun canBeOverriden() {
        super.canBeOverriden()
        println("override here")
    }

//    override fun cannotBeOverriden() {}
}

open class A {
    val name: String = "name"
}
class B: A() {
    val age: Int = 10
}

open class Rectangle {
    open fun draw() {
        println("rectangle")
    }
}

interface Polygon {
    fun draw() {
        println("polygon")
    }
}

class Square: Rectangle(), Polygon {
    override fun draw() {
        super<Rectangle>.draw()
        super<Polygon>.draw()
        println("square")
    }
}

/*
* Companion Object
* - instanceはいらないがclassない情報にアクセスしたいときに使う
*
* Object:
* あるクラスに対し、微妙に変更を加えたobjectを持ちたいことがある
* そういったときに利用できる
*
* Companion Object:
* あるクラスの内部に持つobject.
* 実質static相当のことをできるが、実態はあくまでそれを包むclassを微妙に変更した
* 特殊クラス的なものの"singleton"オブジェクト
* このケースだと dev.ishikawa.dd_kotlin.objects.WithCompanion.Companionというクラスが自動生成される
*
* staticに見えるとしてもruntimeにおいてはinstance membersである ?
* ただし、JVMにおいてはstaticメソッド/フィールドとして生成されたobjectに見える?
* */

class WithCompanion {
    companion object {
        fun create(): WithCompanion {
            return WithCompanion()
        }
    }
}