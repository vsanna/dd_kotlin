package dev.ishikawa.dd_kotlin._6more

/*
Annotation
@Target
@Retention: default = RUNTIME: Annotation is stored in binary output and visible for reflection
@Repeatable
@MustBeDocumented


When you're annotating a property or a primary constructor parameter,
there are multiple Java elements which are generated from the corresponding Kotlin element
よってそこでは相当するjavaコードのどこにannotationを渡すのかを指定することができる

* */


@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY_GETTER,
)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Repeatable
annotation class Special(
    val why: String,
    val reason: Reason = Reason.DEFAULT
) {
    enum class Reason {
        DEFAULT,
        HOGE,
        GEHO,
    }
}

@Special("special is special", reason = Special.Reason.HOGE)
data class User5(val name: String) {
    @Special("special is special 2")
    fun say(): String {
        return name
    }
}


fun main() {
    val annotation = User5::class.java.getAnnotation(Special::class.java)
    println(annotation?.why)
    println(annotation?.reason)

    val methodAnnotation = User5::class.java.getDeclaredMethod("say")?.getAnnotation(Special::class.java)
    println(methodAnnotation?.why)


    val annotation1 = Example2::class.java.getDeclaredMethod("getName")?.getAnnotation(Special::class.java)
    println(annotation1?.why)
}

class Example2(
    // getterにこのannotationを渡す
    @get:Special(why = "special~") var name: String
)
