package com.kota.Telnet.Model

import com.kota.TextEncoder.B2UEncoder
import java.util.*

class TelnetRow {
    private var appendRow: TelnetRow? = null
    private var cachedString: String? = null
    private var emptySpace: Int = 0
    private var quoteLevel: Int = -1
    private var quoteSpace: Int = 0
    
    var backgroundColor: ByteArray = ByteArray(80)
    var bitSpace: ByteArray = ByteArray(80)
    var blink: BooleanArray = BooleanArray(80)
    var data: ByteArray = ByteArray(80)
    var italic: BooleanArray = BooleanArray(80)
    var textColor: ByteArray = ByteArray(80)
    
    constructor() {
        quoteLevel = -1
        quoteSpace = 0
        emptySpace = 0
        cachedString = null
        appendRow = null
        clear()
    }
    
    constructor(row: TelnetRow) : this() {
        set(row)
    }
    
    fun clear() {
        for (i in 0 until 80) {
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
        cachedString = null
        quoteLevel = -1
    }
    
    fun set(row: TelnetRow): TelnetRow {
        for (i in 0 until 80) {
            data[i] = row.data[i]
            textColor[i] = row.textColor[i]
            backgroundColor[i] = row.backgroundColor[i]
            bitSpace[i] = row.bitSpace[i]
            blink[i] = row.blink[i]
            italic[i] = row.italic[i]
        }
        cleanCachedData()
        return this
    }
    
    fun getQuoteLevel(): Int {
        if (quoteLevel == -1) {
            reloadQuoteSpace()
        }
        return quoteLevel
    }
    
    fun getQuoteSpace(): Int {
        if (quoteLevel == -1) {
            reloadQuoteSpace()
        }
        return quoteSpace
    }
    
    fun getEmptySpace(): Int {
        if (quoteLevel == -1) {
            reloadQuoteSpace()
        }
        return emptySpace
    }
    
    fun getDataSpace(): Int {
        return data.size - getEmptySpace()
    }
    
    private fun reloadQuoteSpace() {
        quoteLevel = 0
        quoteSpace = 0
        var spaceCount = 0
        
        while (quoteSpace < data.size) {
            if (data[quoteSpace] == 62.toByte()) {
                quoteLevel++
                spaceCount = 0
            } else if (data[quoteSpace] != 32.toByte() || ++spaceCount > 1) {
                break
            }
            quoteSpace++
        }
        
        emptySpace = 0
        for (i in data.size - 1 downTo 0) {
            if (data[i] == 0.toByte()) {
                emptySpace++
            } else {
                break
            }
        }
    }
    
    override fun toString(): String {
        return getRawString().substring(getQuoteSpace()).trim()
    }
    
    fun toContentString(): String {
        return getRawString().substring(getQuoteSpace())
    }
    
    fun getRawString(): String {
        if (cachedString == null) {
            cachedString = B2UEncoder.getInstance()?.encodeToString(data) ?: ""
            appendRow?.let { cachedString += it.getRawString() }
        }
        return cachedString ?: ""
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
        
        val toPosition = position
        val cachedString = getRawString()
        val actualToPosition = minOf(toPosition, cachedString.length)
        
        return if (actualToPosition < fromPosition) {
            ""
        } else {
            cachedString.substring(fromPosition, actualToPosition)
        }
    }
    
    fun isEmpty(): Boolean {
        return data.all { it == 0.toByte() }
    }
    
    fun append(row: TelnetRow) {
        appendRow = row
        cleanCachedData()
        println("self become: ${getRawString()}")
    }
    
    fun clone(): TelnetRow {
        return TelnetRow(this)
    }
    
    /** 雙字元判斷 */
    fun reloadSpace() {
        var column = 0
        while (column < 80) {
            val data = this.data[column].toInt() and 255
            if (data > 127 && column < 79) {
                val textColorDiff = this.textColor[column] != this.textColor[column + 1]
                val backgroundColorDiff = this.backgroundColor[column] != this.backgroundColor[column + 1]
                
                if (textColorDiff || backgroundColorDiff) {
                    this.bitSpace[column] = BitSpaceType.DOUBLE_BIT_SPACE_2_1
                    this.bitSpace[column + 1] = BitSpaceType.DOUBLE_BIT_SPACE_2_2
                } else {
                    this.bitSpace[column] = BitSpaceType.SINGLE_BIT_SPACE_2_1
                    this.bitSpace[column + 1] = BitSpaceType.SINGLE_BIT_SPACE_2_2
                }
                column++
            } else {
                this.bitSpace[column] = BitSpaceType.BIT_SPACE_1
            }
            column++
        }
    }
    
    /** 回傳對應雙字元的前景色, 單雙字元已經轉換完成 */
    fun getTextColor(): ByteArray {
        val colors = ByteArray(80)
        var nowIndex = -1
        var bitIndex = 0
        
        while (bitIndex < bitSpace.size) {
            nowIndex++
            if (bitSpace[bitIndex] > 0) {
                // 雙字元
                colors[nowIndex] = textColor[bitIndex]
                bitIndex++
            } else {
                // 單字元
                colors[nowIndex] = textColor[bitIndex]
            }
            bitIndex++
        }
        
        nowIndex += 1 // index和copyOfRange定位差異
        return colors.copyOfRange(0, nowIndex)
    }
    
    /** 回傳對應雙字元的背景色, 單雙字元已經轉換完成 */
    fun getBackgroundColor(): ByteArray {
        val colors = ByteArray(80)
        var nowIndex = -1
        var bitIndex = 0
        
        while (bitIndex < bitSpace.size) {
            nowIndex++
            if (bitSpace[bitIndex] > 0) {
                // 雙字元
                colors[nowIndex] = backgroundColor[bitIndex]
                bitIndex++
            } else {
                // 單字元
                colors[nowIndex] = backgroundColor[bitIndex]
            }
            bitIndex++
        }
        
        nowIndex += 1 // index和copyOfRange定位差異
        return colors.copyOfRange(0, nowIndex)
    }
}
