package com.kota.Telnet.Model

data class TelnetData(
    var data: Byte = 0,
    var textColor: Byte = 0,
    var backgroundColor: Byte = 0,
    var blink: Boolean = false,
    var italic: Boolean = false
) {
    
    companion object {
        private var count = 0
    }
    
    constructor(data: TelnetData) : this() {
        set(data)
    }
    
    fun set(data: TelnetData) {
        this.data = data.data
        this.textColor = data.textColor
        this.backgroundColor = data.backgroundColor
        this.blink = data.blink
        this.italic = data.italic
    }
    
    public override fun clone(): TelnetData {
        return TelnetData(this)
    }
    
    fun clear() {
        data = 0
        textColor = 0
        backgroundColor = 0
        blink = false
        italic = false
    }
    
    fun isEmpty(): Boolean {
        return data == 0.toByte()
    }
}
