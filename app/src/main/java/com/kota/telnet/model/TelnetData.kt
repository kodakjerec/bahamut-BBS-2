package com.kota.telnet.model

class TelnetData {
    var data: Byte = 0
    var textColor: Byte = 0
    var backgroundColor: Byte = 0
    var blink: Boolean = false
    var italic: Boolean = false

    constructor()

    constructor(aData: TelnetData) {
        set(aData)
    }

    fun set(aData: TelnetData) {
        this.data = aData.data
        this.textColor = aData.textColor
        this.backgroundColor = aData.backgroundColor
        this.blink = aData.blink
        this.italic = aData.italic
    }

    fun clone(): TelnetData {
        return TelnetData(this)
    }

    fun clear() {
        this.data = 0.toByte()
        this.textColor = 0.toByte()
        this.backgroundColor = 0.toByte()
        this.blink = false
        this.italic = false
    }

    val isEmpty: Boolean
        get() = this.data.toInt() == 0

    companion object {
        private const val myCount = 0
    }
}
