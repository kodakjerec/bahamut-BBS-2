package com.kota.telnet.reference

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import androidx.core.internal.view.SupportMenu
import androidx.core.view.InputDeviceCompat

object TelnetAnsiCode {
    val BACKGROUND_COLOR_NORMAL: IntArray = intArrayOf(
        View.MEASURED_STATE_MASK,
        -8388608,
        -16744448,
        -8355840,
        -16777088,
        -8388480,
        -16744320,
        -4144960
    )

    @SuppressLint("RestrictedApi")
    val COLOR_BRIGHT: IntArray = intArrayOf(
        -8355712,
        SupportMenu.CATEGORY_MASK,
        -16711936,
        InputDeviceCompat.SOURCE_ANY,
        -16776961,
        -65281,
        -16711681,
        -1
    )
    val TEXT_COLOR_NORMAL: IntArray = intArrayOf(
        View.MEASURED_STATE_MASK,
        -8388608,
        -16744448,
        -8355840,
        -16777088,
        -8388480,
        -16744320,
        -4144960
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
