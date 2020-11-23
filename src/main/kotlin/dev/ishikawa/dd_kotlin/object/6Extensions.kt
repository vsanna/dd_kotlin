package dev.ishikawa.dd_kotlin.`object`

fun main() {
    val list: MutableList<Int> = mutableListOf(1,2,3)
    val mutableList = MutableList<Any>(1) { it }
    mutableList.add(1)
    mutableList.anyExtention()
    val mutableList2 = MutableList<Int>(1) { it }
    mutableList2.add(1)
//    mutableList2.anyExtention()
    mutableList2.intExtention()

    mutableList.swap(0, 1)
    mutableList2.swap(1, 0)

    val user: User = User()
    val user2: UserMeta = User()
    user.say()  // say1
    user.ho()   // ho1
    user2.say() // say3 member function always win
    user2.ho()  // ho2  runtime

    user.overrideme("here")

    val str: String? = null
    str.areyouthere() // no, I'm not

    println(user.fullname)

    println(User.foo())
}

fun MutableList<Any>.anyExtention() {
}

fun MutableList<Int>.intExtention() {
}

fun <T> MutableList<T>.swap(index1: Int, index2: Int) {
    val tmp = this[index1]
    this[index1] = this[index2]
    this[index2] = tmp
}

/*
* extension functions are dispatched statically
* val user: User = User()
* val user2: UserMeta = User()
* user.say()  // say1
* user.ho()   // ho1  runtimeではなくstaticにその式の型で判定
* user2.say() // say3 member function always win
* user2.ho()  // ho2  runtimeではなくstaticにその式の型で判定
*
* その式の評価結果の方ではなく、その式自体の(宣言された)型で決まる
*
* member functionの別signatureによるoverrideは可能
*
* nullableにも定義できる
*
* extensionはpropertiesにも使える
* ただしextensionは文字通りにクラスを拡張するわけではないので、
* extensionで生えたフィールド用には(backing)fieldを持てない
* 故にinitializerも使えない(保存するところがないから). calculated propertyと考えると良さそう
*
* companion objectにもextensionはやせる
*
* scope
* - toplevelに定義したものはimportすれば外からでも使える
* - あるクラス内でほかクラスのextensionを定義可能
*     - そこでのthis -> 拡張対象とそのクラスのインスタンスの2つ存在する. this@User(qualified this)で識別
*         - println(this.javaClass)
*         - println(this@User.javaClass)
*
*
* */
interface UserMeta {}
class User: UserMeta {
    fun say() {
        println("say1")
    }
    open fun overrideme() {
        println("member method ${"hogaa".gaa()}")
    }

    fun String.gaa() {
        println(this.javaClass)
        println(this@User.javaClass)
        println("gaa")
    }

    companion object {}
}
fun User.say() {
    println("say2")
}
fun UserMeta.say() {
    println("say3")
}

fun User.ho() {
    println("ho1")
}
fun UserMeta.ho() {
    println("ho2")
}

fun User.overrideme(msg: String) {
    println("overridden")
}

fun String?.areyouthere() {
    println("no, I'm not")
}

val User.fullname: String
    get() = "fullname"


fun User.Companion.foo() {
    println("fooooo")
}