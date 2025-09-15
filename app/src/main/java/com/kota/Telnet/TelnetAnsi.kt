package com.kota.Telnet

class TelnetAnsi {
    companion object {
        const val DEFAULT_BACKGROUND_COLOR: Byte = 0
        private const val DEFAULT_TEXT_BLINK = false
        private const val DEFAULT_TEXT_BRIGHT = false
        const val DEFAULT_TEXT_COLOR: Byte = 7
        private const val DEFAULT_TEXT_ITALIC = false

        fun getDefaultTextColor(): Byte = DEFAULT_TEXT_COLOR
        fun getDefaultBackgroundColor(): Byte = DEFAULT_BACKGROUND_COLOR
        fun getDefaultTextBlink(): Boolean = DEFAULT_TEXT_BLINK
        fun getDefaultTextItalic(): Boolean = DEFAULT_TEXT_ITALIC
    }

    var backgroundColor: Byte = 0
    var textBlink = false
    var textBright = false
    var textColor: Byte = 0
    var textItalic = false

    init {
        resetToDefaultState()
    }

    fun resetToDefaultState() {
        textColor = DEFAULT_TEXT_COLOR
        textBlink = DEFAULT_TEXT_BLINK
        textBright = DEFAULT_TEXT_BRIGHT
        textItalic = DEFAULT_TEXT_ITALIC
        backgroundColor = DEFAULT_BACKGROUND_COLOR
    }
}
