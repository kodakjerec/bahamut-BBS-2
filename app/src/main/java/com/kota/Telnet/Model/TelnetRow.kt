package com.kota.Telnet.Model

import com.kota.TextEncoder.B2UEncoder
import java.util.Arrays

class TelnetRow {
    private var _append_row: TelnetRow?
    private var _cached_string: String?
    private var _empty_space: Int
    private var _quote_level: Int
    private var _quote_space: Int
    @JvmField
    var backgroundColor: ByteArray
    var bitSpace: ByteArray
    var blink: BooleanArray
    @JvmField
    var data: ByteArray
    var italic: BooleanArray
    var textColor: ByteArray

    constructor() {
        data = ByteArray(80)
        textColor = ByteArray(80)
        backgroundColor = ByteArray(80)
        blink = BooleanArray(80)
        italic = BooleanArray(80)
        bitSpace = ByteArray(80)
        _quote_level = -1
        _quote_space = 0
        _empty_space = 0
        _cached_string = null
        _append_row = null
        clear()
    }

    constructor(aRow: TelnetRow) {
        data = ByteArray(80)
        textColor = ByteArray(80)
        backgroundColor = ByteArray(80)
        blink = BooleanArray(80)
        italic = BooleanArray(80)
        bitSpace = ByteArray(80)
        _quote_level = -1
        _quote_space = 0
        _empty_space = 0
        _cached_string = null
        _append_row = null
        clear()
        set(aRow)
    }

    fun clear() {
        for (i in 0..79) {
            cleanColumn(i)
        }
        cleanCachedData()
    }

    fun cleanColumn(column: Int) {
        data[column] = 0
        textColor[column] = 0
        backgroundColor[column] = 0
        bitSpace[column] = 0
        blink[column] = false
        italic[column] = false
    }

    fun cleanCachedData() {
        _cached_string = null
        _quote_level = -1
    }

    fun set(aRow: TelnetRow): TelnetRow {
        for (i in 0..79) {
            data[i] = aRow.data[i]
            textColor[i] = aRow.textColor[i]
            backgroundColor[i] = aRow.backgroundColor[i]
            bitSpace[i] = aRow.bitSpace[i]
            blink[i] = aRow.blink[i]
            italic[i] = aRow.italic[i]
        }
        cleanCachedData()
        return this
    }

    val quoteLevel: Int
        get() {
            if (_quote_level == -1) {
                reloadQuoteSpace()
            }
            return _quote_level
        }

    val quoteSpace: Int
        get() {
            if (_quote_level == -1) {
                reloadQuoteSpace()
            }
            return _quote_space
        }

    val emptySpace: Int
        get() {
            if (_quote_level == -1) {
                reloadQuoteSpace()
            }
            return _empty_space
        }

    val dataSpace: Int
        get() = data.size - this.emptySpace

    private fun reloadQuoteSpace() {
        _quote_level = 0
        _quote_space = 0
        var space_count = 0
        while (_quote_space < data.size) {
            if (data[_quote_space].toInt() == 62) {
                _quote_level++
                space_count = 0
            } else if (data[_quote_space].toInt() != 32 || ((space_count + 1).also {
                    space_count = it
                }) > 1) {
                break
            }
            _quote_space++
        }
        _empty_space = 0
        var i = data.size - 1
        while (i >= 0 && data[i].toInt() == 0) {
            _empty_space++
            i--
        }
    }

    override fun toString(): String {
        return this.rawString.substring(this.quoteSpace).trim { it <= ' ' }
    }

    fun toContentString(): String {
        return this.rawString.substring(this.quoteSpace)
    }

    val rawString: String
        get() {
            if (_cached_string == null) {
                _cached_string = B2UEncoder.getInstance().encodeToString(data)
                if (_append_row != null) {
                    _cached_string += _append_row.getRawString()
                }
            }
            return _cached_string!!
        }

    fun getSpaceString(from: Int, to: Int): String {
        var from_position = 0
        var position = 0
        var i = 0
        while (i <= to) {
            if (i == from) {
                from_position = position
            }
            position++
            val d = data[i].toInt() and 255
            if (d > 127 && i < to) {
                i++
            }
            i++
        }
        var to_position = position
        val cached_string = this.rawString
        if (to_position > cached_string.length) {
            to_position = cached_string.length
        }
        return if (to_position < from_position) "" else cached_string.substring(
            from_position,
            to_position
        )
    }

    val isEmpty: Boolean
        get() {
            for (i in 0..79) {
                if (data[i].toInt() != 0) {
                    return false
                }
            }
            return true
        }

    fun append(row: TelnetRow?) {
        _append_row = row
        cleanCachedData()
        println("self become:" + this.rawString)
    }

    public override fun clone(): TelnetRow {
        return TelnetRow(this)
    }

    /** 雙字元判斷  */
    fun reloadSpace() {
        var column = 0
        while (column < 80) {
            val data = this.data[column].toInt() and 255
            if (data > 127 && column < 79) {
                val text_color_diff = this.textColor[column] != this.textColor[column + 1]
                val background_color_diff =
                    this.backgroundColor[column] != this.backgroundColor[column + 1]
                if (text_color_diff || background_color_diff) {
                    this.bitSpace[column] = BitSpaceType.Companion.DOUBLE_BIT_SPACE_2_1
                    this.bitSpace[column + 1] = BitSpaceType.Companion.DOUBLE_BIT_SPACE_2_2
                } else {
                    this.bitSpace[column] = BitSpaceType.Companion.SINGLE_BIT_SPACE_2_1
                    this.bitSpace[column + 1] = BitSpaceType.Companion.SINGLE_BIT_SPACE_2_2
                }
                column++
            } else {
                this.bitSpace[column] = BitSpaceType.Companion.BIT_SPACE_1
            }
            column++
        }
    }

    /** 回傳對應雙字元的前景色, 單雙字元已經轉換完成  */
    fun getTextColor(): ByteArray? {
        val colors = ByteArray(80)
        var nowIndex = -1
        var bitIndex: Byte = 0
        while (bitIndex < bitSpace.size) {
            nowIndex++
            if (bitSpace[bitIndex.toInt()] > 0) {
                // 雙字元
                colors[nowIndex] = textColor[bitIndex.toInt()]
                bitIndex++
            } else {
                // 單字元
                colors[nowIndex] = textColor[bitIndex.toInt()]
            }
            bitIndex++
        }
        nowIndex += 1 // index和copyOfRange定位差異
        return Arrays.copyOfRange(colors, 0, nowIndex)
    }

    /** 回傳對應雙字元的背景色, 單雙字元已經轉換完成  */
    fun getBackgroundColor(): ByteArray? {
        val colors = ByteArray(80)
        var nowIndex = -1
        var bitIndex: Byte = 0
        while (bitIndex < bitSpace.size) {
            nowIndex++
            if (bitSpace[bitIndex.toInt()] > 0) {
                // 雙字元
                colors[nowIndex] = backgroundColor[bitIndex.toInt()]
                bitIndex++
            } else {
                // 單字元
                colors[nowIndex] = backgroundColor[bitIndex.toInt()]
            }
            bitIndex++
        }
        nowIndex += 1 // index和copyOfRange定位差異
        return Arrays.copyOfRange(colors, 0, nowIndex)
    }
}
