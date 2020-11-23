package dev.ishikawa.dd_kotlin._1basic.packagesample

// toplevelのfunは{packagename}.printmsgで外からimportできる
// privateにすることで閉じれる
// packageないとdefault package = global定義になる
fun printmsg(msg: Message) {
    println(msg.msg)
}

data class Message(val msg: String)

