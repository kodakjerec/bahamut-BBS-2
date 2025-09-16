package com.kota.Telnet

class TelnetAnsi {
    var backgroundColor: Byte = 0
    var textBlink: Boolean = false
    var textBright: Boolean = false
    var textColor: Byte = 0
    var textItalic: Boolean = false

    init {
        resetToDefaultState()
    }

    fun resetToDefaultState() {
        this.textColor = defaultTextColor
        this.textBlink = defaultTextBlink
        this.textBright = DEFAULT_TEXT_BRIGHT
        this.textItalic = defaultTextItalic
        this.backgroundColor = defaultBackgroundColor
    }

    companion object {
        var defaultBackgroundColor: Byte = 0
        const val defaultTextBlink: Boolean = false
        private const val DEFAULT_TEXT_BRIGHT = false
        var defaultTextColor: Byte = 7
        const val defaultTextItalic: Boolean = false
    }
}
