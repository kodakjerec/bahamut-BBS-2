package com.kota.Telnet.Reference

object TelnetKeyboard {
    const val DELETE = 265
    const val DOWN_ARROW = 259
    const val END = 263
    const val HOME = 262
    const val INSERT = 264
    const val LEFT_ARROW = 256
    const val PAGE_DOWN = 261
    const val PAGE_UP = 260
    const val RIGHT_ARROW = 257
    const val SPACE = 32
    const val TAB = 9
    const val UP_ARROW = 258
    const val CTRL_G = 7
    const val CTRL_P = 16
    const val CTRL_Q = 17
    const val CTRL_R = 18
    const val CTRL_S = 19
    const val CTRL_U = 21
    const val CTRL_X = 24
    const val CTRL_Y = 25
    const val BACK_ONE_CHAR = 83
    const val SHIFT_M = 115

    fun getKeyDataWithTimes(keyCode: Int, times: Int): ByteArray {
        val keydata = getKeyData(keyCode)
        val data = ByteArray(keydata.size * times)
        for (index in data.indices) {
            data[index] = keydata[index % keydata.size]
        }
        return data
    }

    /**
     * 輸入鍵盤指令的代號，回傳 telnet command
     * @param keyCode 鍵盤指令
     * @return ByteArray telnet command
     */
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
