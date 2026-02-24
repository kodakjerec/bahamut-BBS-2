package com.kota.telnet.reference

object TelnetKeyboard {
    const val DELETE: Int = 265
    const val DOWN_ARROW: Int = 259
    const val END: Int = 263
    const val HOME: Int = 262
    const val INSERT: Int = 264
    const val LEFT_ARROW: Int = 256
    const val PAGE_DOWN: Int = 261
    const val PAGE_UP: Int = 260
    const val RIGHT_ARROW: Int = 257
    const val SPACE: Int = 32
    const val TAB: Int = 9
    const val UP_ARROW: Int = 258
    const val CTRL_G: Int = 7
    const val CTRL_P: Int = 16
    const val CTRL_Q: Int = 17
    const val CTRL_R: Int = 18
    const val CTRL_S: Int = 19
    const val CTRL_U: Int = 21
    const val CTRL_X: Int = 24
    const val CTRL_Y: Int = 25
    const val BACK_ONE_CHAR: Int = 83
    const val KEY_S: Int = 115
    const val SHIFT_M: Int = 77
    const val small_C: Int = 99

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
