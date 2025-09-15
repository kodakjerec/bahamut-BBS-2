package com.kota.Telnet

class NumberCharSequence : CharSequence {
    private val data: CharArray = CharArray(5)
    private var string: String = ""
    
    init {
        clear()
        string = String(data)
    }
    
    fun clear() {
        for (i in 0 until 5) {
            data[i] = '0'
        }
    }
    
    fun setInt(number: Int) {
        var num = number
        data[4] = ((num % 10) + 48).toChar()
        num /= 10
        data[3] = ((num % 10) + 48).toChar()
        num /= 10
        data[2] = ((num % 10) + 48).toChar()
        num /= 10
        data[1] = ((num % 10) + 48).toChar()
        num /= 10
        data[0] = ((num % 10) + 48).toChar()
    }
    
    override fun get(index: Int): Char {
        return data[index]
    }
    
    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return String(data, startIndex, endIndex - startIndex)
    }
    
    override val length: Int
        get() = 5
    
    override fun toString(): String {
        return string
    }
}
