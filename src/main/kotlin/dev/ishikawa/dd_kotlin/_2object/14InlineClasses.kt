package dev.ishikawa.dd_kotlin._2object

fun main() {
    // Intではない
//    val priceInt: Int = getPrice(100)

    val price = getPrice(100)
    val price2 = getPrice(100)
    val price3 = getPrice(200)

    val countryName = CountryName2("Japan")
    countryName.show()

    println(price == price2)
    println(price == price3)
//    println(price === price)
}

/*
現在Alpha機能

ビジネスロジック上、時々ある既存クラスをwrapしたいことがある(ただしパフォーマンスは下がる)
IntにPriceとつける(ことで方の安全性を増す)ようなもの。

そのwrap対象がprimitiveのとき、このパフォーマンス低下は顕著。
それを解決するためのクラス

一つの値だけをもつ。
runtime時には時にはそのprimitiveなpropertyに差し替えられる
通常のクラスの機能を部分的に使える
    - inlineの性質上、こちらもメインのproperty以外にはbacking field持てない
    - interfaceの継承はできるが、classのextendsはできないし、それ自体は絶対にfinal
wrapperとしても中のprimitiveな値としても振る舞うので(box/unboxing)、reference equalityは意味を持たない(できない)

vs typealias
- type aliases are assignment-compatible with their underlying type
    - よって型安全は提供されない
* */

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
inline class Price(val value: Int)

fun getPrice(a: Int): Price {
    if(a > 0) {
        return Price(a)
    } else {
        // 返せない
//        return a
        return Price(-1 * a)
    }
}

inline class CountryName2(val name: String) {
    val nameSize: Int
    get() = name.length

    fun show() {
        println(name)
    }
}