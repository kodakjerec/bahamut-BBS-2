package com.kota.telnet.reference

import android.util.Log

object TelnetAnsiCode {

    // ANSI 標準色 (Dim / Normal)，對應 SGR 30-37 / 40-47
    // 注意：TEXT_COLOR_NORMAL 與 BACKGROUND_COLOR_NORMAL 色表相同，
    //       因為 ANSI 前景/背景用同一組基礎色
    private const val ANSI_BLACK:        Int = 0xFF000000.toInt() // #000000
    private const val ANSI_DARK_RED:     Int = 0xFF800000.toInt() // #800000
    private const val ANSI_DARK_GREEN:   Int = 0xFF008000.toInt() // #008000
    private const val ANSI_DARK_YELLOW:  Int = 0xFF808000.toInt() // #808000
    private const val ANSI_DARK_BLUE:    Int = 0xFF000080.toInt() // #000080
    private const val ANSI_DARK_MAGENTA: Int = 0xFF800080.toInt() // #800080
    private const val ANSI_DARK_CYAN:    Int = 0xFF008080.toInt() // #008080
    private const val ANSI_DARK_WHITE:   Int = 0xFFC0C0C0.toInt() // #C0C0C0

    // ANSI 亮色 (Bright)，對應 SGR 1 + 30-37 / 40-47
    private const val ANSI_DARK_GRAY:      Int = 0xFF808080.toInt() // #808080
    private const val ANSI_BRIGHT_RED:     Int = 0xFFFF0000.toInt() // #FF0000
    private const val ANSI_BRIGHT_GREEN:   Int = 0xFF00FF00.toInt() // #00FF00
    private const val ANSI_BRIGHT_YELLOW:  Int = 0xFFFFFF00.toInt() // #FFFF00
    private const val ANSI_BRIGHT_BLUE:    Int = 0xFF0000FF.toInt() // #0000FF
    private const val ANSI_BRIGHT_MAGENTA: Int = 0xFFFF00FF.toInt() // #FF00FF
    private const val ANSI_BRIGHT_CYAN:    Int = 0xFF00FFFF.toInt() // #00FFFF
    private const val ANSI_BRIGHT_WHITE:   Int = 0xFFFFFFFF.toInt() // #FFFFFF

    val TEXT_COLOR_NORMAL: IntArray = intArrayOf(
        ANSI_BLACK,
        ANSI_DARK_RED,
        ANSI_DARK_GREEN,
        ANSI_DARK_YELLOW,
        ANSI_DARK_BLUE,
        ANSI_DARK_MAGENTA,
        ANSI_DARK_CYAN,
        ANSI_DARK_WHITE,
    )

    val BACKGROUND_COLOR_NORMAL: IntArray = TEXT_COLOR_NORMAL

    val COLOR_BRIGHT: IntArray = intArrayOf(
        ANSI_DARK_GRAY,
        ANSI_BRIGHT_RED,
        ANSI_BRIGHT_GREEN,
        ANSI_BRIGHT_YELLOW,
        ANSI_BRIGHT_BLUE,
        ANSI_BRIGHT_MAGENTA,
        ANSI_BRIGHT_CYAN,
        ANSI_BRIGHT_WHITE,
    )

    /**
     * 瀏覽文章, 返回前景色
     * @param colorIndex index
     * @return int (color code)
     */
    @JvmStatic
    fun getTextColor(colorIndex: Byte): Int {
        val colorIndex1 = colorIndex.toInt() and 255
        if (colorIndex < 8) {
            return TEXT_COLOR_NORMAL[colorIndex1]
        }
        try {
            return COLOR_BRIGHT[colorIndex1 - 8]
        } catch (e: Exception) {
            Log.e(
                TelnetAnsiCode::class.java.simpleName,
                (if (e.message != null) e.message else "")!!
            )
            return -4144960
        }
    }

    /**
     * 瀏覽文章, 返回背景色
     * @param colorIndex index
     * @return int (color code)
     */
    @JvmStatic
    fun getBackgroundColor(colorIndex: Byte): Int {
        val colorIndex1 = colorIndex.toInt() and 255
        if (colorIndex < 8) {
            return BACKGROUND_COLOR_NORMAL[colorIndex1]
        }
        try {
            return COLOR_BRIGHT[colorIndex1 - 8]
        } catch (e: Exception) {
            Log.e(
                TelnetAnsiCode::class.java.simpleName,
                (if (e.message != null) e.message else "")!!
            )
            return -4144960
        }
    }

    /**
     * 修改文章, 返回前景字碼, 亮色已經先處理掉
     * @param colorIndex  index
     * @return string
     */
    @JvmStatic
    fun getTextAsciiCode(colorIndex: Int): String {
        if (colorIndex < 8) {
            return "3$colorIndex"
        }
        return ""
    }

    /**
     * 修改文章, 返回背景字碼
     * @param colorIndex  index
     * @return string
     */
    @JvmStatic
    fun getBackAsciiCode(colorIndex: Byte): String {
        if (colorIndex < 8) {
            return "4$colorIndex"
        }
        return ""
    }

    object Code {
        const val CHA: Int = 6
        const val CNL: Int = 4
        const val CPL: Int = 5
        const val CUB: Int = 3
        const val CUD: Int = 1
        const val CUF: Int = 2
        const val CUP: Int = 7
        const val CUU: Int = 0
        const val DSR: Int = 14
        const val ED: Int = 8
        const val EL: Int = 9
        const val HC: Int = 17
        const val HVP: Int = 12
        const val RCP: Int = 16
        const val SC: Int = 18
        const val SCP: Int = 15
        const val SD: Int = 11
        const val SGR: Int = 13
        const val SU: Int = 10
    }

    object Color {
        const val BLACK: Byte = 0
        const val RED: Byte = 1
        const val GREEN: Byte = 2
        const val YELLOW: Byte = 3
        const val BLUE: Byte = 4
        const val MAGENTA: Byte = 5
        const val CYAN: Byte = 6
        const val WHITE: Byte = 7
    }
}
