package com.kota.telnet.model

import com.kota.textEncoder.B2UEncoder

class TelnetRow {
    private var appendRow: TelnetRow?
    private var cachedString: String?
    private var myEmptySpace: Int
    private var myQuoteLevel: Int
    private var myQuoteSpace: Int
    var myTextColorArray: ByteArray
    var myBackgroundColor: ByteArray
    var bitSpace: ByteArray
    var blink: BooleanArray
    @JvmField
    var data: ByteArray
    var italic: BooleanArray

    constructor() {
        data = ByteArray(80)
        myTextColorArray = ByteArray(80)
        myBackgroundColor = ByteArray(80)
        blink = BooleanArray(80)
        italic = BooleanArray(80)
        bitSpace = ByteArray(80)
        myQuoteLevel = -1
        myQuoteSpace = 0
        myEmptySpace = 0
        cachedString = null
        appendRow = null
        clear()
    }

    constructor(aRow: TelnetRow) {
        data = ByteArray(80)
        myTextColorArray = ByteArray(80)
        myBackgroundColor = ByteArray(80)
        blink = BooleanArray(80)
        italic = BooleanArray(80)
        bitSpace = ByteArray(80)
        myQuoteLevel = -1
        myQuoteSpace = 0
        myEmptySpace = 0
        cachedString = null
        appendRow = null
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
        myTextColorArray[column] = 0
        myBackgroundColor[column] = 0
        bitSpace[column] = 0
        blink[column] = false
        italic[column] = false
    }

    fun cleanCachedData() {
        cachedString = null
        myQuoteLevel = -1
    }

    fun set(aRow: TelnetRow): TelnetRow {
        for (i in 0..79) {
            data[i] = aRow.data[i]
            myTextColorArray[i] = aRow.myTextColorArray[i]
            myBackgroundColor[i] = aRow.myBackgroundColor[i]
            bitSpace[i] = aRow.bitSpace[i]
            blink[i] = aRow.blink[i]
            italic[i] = aRow.italic[i]
        }
        cleanCachedData()
        return this
    }

    val quoteLevel: Int
        get() {
            if (myQuoteLevel == -1) {
                reloadQuoteSpace()
            }
            return myQuoteLevel
        }

    val quoteSpace: Int
        get() {
            if (myQuoteLevel == -1) {
                reloadQuoteSpace()
            }
            return myQuoteSpace
        }

    val emptySpace: Int
        get() {
            if (myQuoteLevel == -1) {
                reloadQuoteSpace()
            }
            return myEmptySpace
        }

    val dataSpace: Int
        get() = data.size - this.emptySpace

    private fun reloadQuoteSpace() {
        myQuoteLevel = 0
        myQuoteSpace = 0
        var spaceCount = 0
        while (myQuoteSpace < data.size) {
            if (data[myQuoteSpace].toInt() == 62) {
                myQuoteLevel++
                spaceCount = 0
            } else if (data[myQuoteSpace].toInt() != 32 || ((spaceCount + 1).also {
                    spaceCount = it
                }) > 1) {
                break
            }
            myQuoteSpace++
        }
        myEmptySpace = 0
        var i = data.size - 1
        while (i >= 0 && data[i].toInt() == 0) {
            myEmptySpace++
            i--
        }
    }

    override fun toString(): String {
        return this.rawString.substring(this.quoteSpace).trim()
    }

    fun toContentString(): String {
        return this.rawString.substring(this.quoteSpace)
    }

    val rawString: String
        get() {
            if (cachedString == null) {
                cachedString = B2UEncoder.instance!!.encodeToString(data)
                appendRow?.let { cachedString += it.rawString }
            }
            return cachedString!!
        }

    fun getSpaceString(from: Int, to: Int): String {
        var fromPosition = 0
        var position = 0
        var i = 0
        while (i <= to) {
            if (i == from) {
                fromPosition = position
            }
            position++
            val d = data[i].toInt() and 255
            if (d > 127 && i < to) {
                i++
            }
            i++
        }
        var toPosition = position
        val cachedString = this.rawString
        if (toPosition > cachedString.length) {
            toPosition = cachedString.length
        }
        return if (toPosition < fromPosition) "" else cachedString.substring(
            fromPosition,
            toPosition
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
        appendRow = row
        cleanCachedData()
        println("self become:" + this.rawString)
    }

    fun clone(): TelnetRow {
        return TelnetRow(this)
    }

    /** 雙字元判斷  */
    fun reloadSpace() {
        var column = 0
        while (column < 80) {
            val data = this.data[column].toInt() and 255
            if (data > 127 && column < 79) {
                val textColorDiff = this.myTextColorArray[column] != this.myTextColorArray[column + 1]
                val backgroundColorDiff =
                    this.myBackgroundColor[column] != this.myBackgroundColor[column + 1]
                if (textColorDiff || backgroundColorDiff) {
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
    fun getTextColorArray(): ByteArray {
        val colors = ByteArray(80)
        var nowIndex = -1
        var bitIndex: Byte = 0
        while (bitIndex < bitSpace.size) {
            nowIndex++
            if (bitSpace[bitIndex.toInt()] > 0) {
                // 雙字元
                colors[nowIndex] = myTextColorArray[bitIndex.toInt()]
                bitIndex++
            } else {
                // 單字元
                colors[nowIndex] = myTextColorArray[bitIndex.toInt()]
            }
            bitIndex++
        }
        nowIndex += 1 // index和copyOfRange定位差異
        return colors.copyOfRange(0, nowIndex)
    }

    /** 回傳對應雙字元的背景色, 單雙字元已經轉換完成  */
    fun getBackgroundColor(): ByteArray {
        val colors = ByteArray(80)
        var nowIndex = -1
        var bitIndex: Byte = 0
        while (bitIndex < bitSpace.size) {
            nowIndex++
            if (bitSpace[bitIndex.toInt()] > 0) {
                // 雙字元
                colors[nowIndex] = myBackgroundColor[bitIndex.toInt()]
                bitIndex++
            } else {
                // 單字元
                colors[nowIndex] = myBackgroundColor[bitIndex.toInt()]
            }
            bitIndex++
        }
        nowIndex += 1 // index和copyOfRange定位差異
        return colors.copyOfRange(0, nowIndex)
    }
}
