package com.kota.Telnet

class TelnetArticlePush {
    var author: String = ""
    var content: String = ""
    var date: String = ""
    var time: String = ""
    var type: Int = 0

    companion object {
        const val BAD: Int = 1
        const val GOOD: Int = 0
    }
}
