package com.kota.telnet

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
        this.textColor = DEFAULT_TEXT_COLOR
        this.textBlink = DEFAULT_TEXT_BLINK
        this.textBright = DEFAULT_TEXT_BRIGHT
        this.textItalic = DEFAULT_TEXT_ITALIC
        this.backgroundColor = DEFAULT_BACKGROUND_COLOR
    }

    companion object {
        const val DEFAULT_BACKGROUND_COLOR: Byte = 0
        const val DEFAULT_TEXT_BLINK: Boolean = false
        const val DEFAULT_TEXT_BRIGHT = false
        const val DEFAULT_TEXT_COLOR: Byte = 7
        const val DEFAULT_TEXT_ITALIC: Boolean = false
    }
}
