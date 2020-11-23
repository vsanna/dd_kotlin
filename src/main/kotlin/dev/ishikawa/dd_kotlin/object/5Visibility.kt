package dev.ishikawa.dd_kotlin.`object`

fun main() {

}

/*
* clas, object, interface, constructor, functions, properties and setters can have visibilities.
* private
* protected
* internal
* public
*
* toplevelで宣言されたfunction, props, classes objects, interfaces
* - デフォルトでpublic. ほかから呼ばれうる
* - internal: 同一moduleないであれば呼べる。いわゆるpackage private
*
* class/interface内部のmethods
* - private: そのクラス内でのみ見える。子も見えない
* - protected: subclassとそのクラスでのみ見える
* - internal: このmodule内のclientであればみえる
* - piblic: 誰もが見える
*
* modules
* */