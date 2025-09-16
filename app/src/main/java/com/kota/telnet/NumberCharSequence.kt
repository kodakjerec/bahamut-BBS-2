package com.kota.telnet

class NumberCharSequence : CharSequence {
    private val _data = CharArray(5)
    private var _string: String? = null

    init {
        clear()
        this._string = String(this._data)
    }

    fun clear() {
        for (i in 0..4) {
            this._data[i] = '0'
        }
    }

    fun setInt(i: Int) {
        val number = i
        this._data[4] = ((number % 10) + 48).toChar()
        val number2 = number / 10
        this._data[3] = ((number2 % 10) + 48).toChar()
        val number3 = number2 / 10
        this._data[2] = ((number3 % 10) + 48).toChar()
        val number4 = number3 / 10
        this._data[1] = ((number4 % 10) + 48).toChar()
        val number5 = number4 / 10
        this._data[0] = ((number5 % 10) + 48).toChar()
        val number6 = number5 / 10
    }

    override fun charAt(index: Int): Char {
        return this._data[index]
    }

    override fun length(): Int {
        return 5
    }

    override fun subSequence(start: Int, end: Int): CharSequence {
        return null
    }

    override fun toString(): String {
        return this._string!!
    }
}
