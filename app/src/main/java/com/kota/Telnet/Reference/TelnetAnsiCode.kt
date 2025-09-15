package com.kota.Telnet.Reference

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import androidx.core.internal.view.SupportMenu
import androidx.core.view.InputDeviceCompat

object TelnetAnsiCode {
    val BACKGROUND_COLOR_NORMAL = intArrayOf(
        View.MEASURED_STATE_MASK, -8388608, -16744448, -8355840, 
        -16777088, -8388480, -16744320, -4144960
    )
    
    @SuppressLint("RestrictedApi")
    val COLOR_BRIGHT = intArrayOf(
        -8355712, SupportMenu.CATEGORY_MASK, -16711936, InputDeviceCompat.SOURCE_ANY, 
        -16776961, -65281, -16711681, -1
    )
    
    val TEXT_COLOR_NORMAL = intArrayOf(
        View.MEASURED_STATE_MASK, -8388608, -16744448, -8355840, 
        -16777088, -8388480, -16744320, -4144960
    )

    object Code {
        const val CHA = 6
        const val CNL = 4
        const val CPL = 5
        const val CUB = 3
        const val CUD = 1
        const val CUF = 2
        const val CUP = 7
        const val CUU = 0
        const val DSR = 14
        const val ED = 8
        const val EL = 9
        const val HC = 17
        const val HVP = 12
        const val RCP = 16
        const val SC = 18
        const val SCP = 15
        const val SD = 11
        const val SGR = 13
        const val SU = 10
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

    /**
     * 瀏覽文章, 返回前景色
     * @param colorIndex index
     * @return Int (color code)
     */
    fun getTextColor(colorIndex: Byte): Int {
        val colorIndexInt = colorIndex.toInt() and 255
        return if (colorIndex < 8) {
            TEXT_COLOR_NORMAL[colorIndexInt]
        } else {
            try {
                COLOR_BRIGHT[colorIndexInt - 8]
            } catch (e: Exception) {
                Log.e(TelnetAnsiCode::class.java.simpleName, e.message ?: "")
                -4144960
            }
        }
    }

    /**
     * 瀏覽文章, 返回背景色
     * @param colorIndex index
     * @return Int (color code)
     */
    fun getBackgroundColor(colorIndex: Byte): Int {
        val colorIndexInt = colorIndex.toInt() and 255
        return if (colorIndex < 8) {
            BACKGROUND_COLOR_NORMAL[colorIndexInt]
        } else {
            try {
                COLOR_BRIGHT[colorIndexInt - 8]
            } catch (e: Exception) {
                Log.e(TelnetAnsiCode::class.java.simpleName, e.message ?: "")
                -4144960
            }
        }
    }

    /**
     * 修改文章, 返回前景字碼, 亮色已經先處理掉
     * @param colorIndex index
     * @return String
     */
    fun getTextAsciiCode(colorIndex: Byte): String {
        return if (colorIndex < 8) {
            "3$colorIndex"
        } else {
            ""
        }
    }

    /**
     * 修改文章, 返回背景字碼
     * @param colorIndex index
     * @return String
     */
    fun getBackAsciiCode(colorIndex: Byte): String {
        return if (colorIndex < 8) {
            "4$colorIndex"
        } else {
            ""
        }
    }
}
