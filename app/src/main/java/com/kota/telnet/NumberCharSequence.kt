package com.kota.telnet

class NumberCharSequence : CharSequence {
    private val myData = CharArray(5)
    private var myString: String? = null

    override val length: Int
        get() = myData.size

    override fun get(index: Int): Char {
        return this.myData[index]
    }

    init {
        clear()
        this.myString = String(this.myData)
    }

    fun clear() {
        for (i in 0..4) {
            this.myData[i] = '0'
        }
    }

    fun setInt(i: Int) {
        val number = i
        this.myData[4] = ((number % 10) + 48).toChar()
        val number2 = number / 10
        this.myData[3] = ((number2 % 10) + 48).toChar()
        val number3 = number2 / 10
        this.myData[2] = ((number3 % 10) + 48).toChar()
        val number4 = number3 / 10
        this.myData[1] = ((number4 % 10) + 48).toChar()
        val number5 = number4 / 10
        this.myData[0] = ((number5 % 10) + 48).toChar()
        val number6 = number5 / 10
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        // 参数校验，确保 startIndex 和 endIndex 是有效的
        if (startIndex < 0 || endIndex > length || startIndex > endIndex) {
            throw IndexOutOfBoundsException("startIndex: $startIndex, endIndex: $endIndex, length: $length")
        }
        // 从 myData 数组中提取子序列并创建一个新的 String
        return String(myData, startIndex, endIndex - startIndex)
    }

    override fun toString(): String {
        return this.myString!!
    }
}
