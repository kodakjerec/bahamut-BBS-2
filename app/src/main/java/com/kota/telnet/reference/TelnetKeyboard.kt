package com.kota.telnet.reference

object TelnetKeyboard {

    // --- ASCII 控制字元 ---
    const val NUL: Int = 0
    const val CTRL_A: Int = 1
    const val CTRL_B: Int = 2
    const val CTRL_C: Int = 3
    const val CTRL_D: Int = 4
    const val CTRL_E: Int = 5
    const val CTRL_F: Int = 6
    const val CTRL_G: Int = 7
    const val CTRL_H: Int = 8
    const val CTRL_I: Int = 9
    const val CTRL_J: Int = 10
    const val CTRL_K: Int = 11
    const val CTRL_L: Int = 12
    const val CTRL_M: Int = 13
    const val CTRL_N: Int = 14
    const val CTRL_O: Int = 15
    const val CTRL_P: Int = 16
    const val CTRL_Q: Int = 17
    const val CTRL_R: Int = 18
    const val CTRL_S: Int = 19
    const val CTRL_T: Int = 20
    const val CTRL_U: Int = 21
    const val CTRL_V: Int = 22
    const val CTRL_W: Int = 23
    const val CTRL_X: Int = 24
    const val CTRL_Y: Int = 25
    const val CTRL_Z: Int = 26
    const val BEL: Int = 7   // Bell (同 CTRL_G)
    const val BS: Int = 8    // Backspace (同 CTRL_H)
    const val TAB: Int = 9   // Horizontal Tab (同 CTRL_I)
    const val LF: Int = 10   // Line Feed (同 CTRL_J)
    const val CR: Int = 13   // Carriage Return (同 CTRL_M)
    const val ESC: Int = 27  // Escape
    const val DEL: Int = 127 // Delete
    const val CAN: Int = 24  // Cancel (同 CTRL_X)
    const val UNSET: Int = -1

    // --- 可列印字元 / 符號 ---
    const val SPACE: Int = 32
    const val LEFT_BRACKET: Int = 91  // [
    const val RIGHT_BRACKET: Int = 93 // ]
    const val EQUAL: Int = 61         // =
    const val KEY_S: Int = 115
    const val SMALL_A: Int = 97       // 加入我的最愛
    const val SMALL_C: Int = 99       // 切換編號/總數
    const val SMALL_D: Int = 100      // 移出我的最愛 / 刪除
    const val SMALL_T: Int = 116      // 串接模式切換編號
    const val SHIFT_M: Int = 77
    const val BACK_ONE_CHAR: Int = 83

    // --- 方向鍵 / 功能鍵 (自訂 keyCode) ---
    const val LEFT_ARROW: Int = 256
    const val RIGHT_ARROW: Int = 257
    const val UP_ARROW: Int = 258
    const val DOWN_ARROW: Int = 259
    const val PAGE_UP: Int = 260
    const val PAGE_DOWN: Int = 261
    const val HOME: Int = 262
    const val END: Int = 263
    const val INSERT: Int = 264
    const val DELETE: Int = 265


    fun getKeyDataWithTimes(keyCode: Int, times: Int): ByteArray {
        val keyData = getKeyData(keyCode)
        val data = ByteArray((keyData.size * times))
        for (index in data.indices) {
            data[index] = keyData[index % keyData.size]
        }
        return data
    }

    /**
     * 輸入鍵盤指令的代號，回傳 telnet command
     * @param keyCode 鍵盤指令
     * @return byte[] telnet command
     */
    @JvmStatic
    fun getKeyData(keyCode: Int): ByteArray {
        return when (keyCode) {
            TAB -> byteArrayOf(9)
            SPACE -> byteArrayOf(32)
            LEFT_ARROW -> byteArrayOf(27, 91, 68)
            RIGHT_ARROW -> byteArrayOf(27, 91, 67)
            UP_ARROW -> byteArrayOf(27, 91, 65)
            DOWN_ARROW -> byteArrayOf(27, 91, 66)
            PAGE_UP -> byteArrayOf(27, 91, 53, 126)
            PAGE_DOWN -> byteArrayOf(27, 91, 54, 126)
            HOME -> byteArrayOf(27, 91, 49, 126)
            END -> byteArrayOf(27, 91, 52, 126)
            INSERT -> byteArrayOf(27, 91, 50, 126)
            DELETE -> byteArrayOf(27, 91, 51, 126)
            else -> byteArrayOf(keyCode.toByte())
        }
    }
}
