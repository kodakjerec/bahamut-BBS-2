package com.kota.Telnet.Model

import com.kota.Telnet.Reference.TelnetAnsiCode
import java.util.*

class TelnetFrame {
    
    companion object {
        const val DEFAULT_COLUMN = 80
        const val DEFAULT_ROW = 24
    }
    
    var rows: Vector<TelnetRow> = Vector()
    
    constructor() {
        initialData(DEFAULT_ROW)
        clear()
    }
    
    constructor(row: Int) {
        initialData(row)
        clear()
    }
    
    constructor(frame: TelnetFrame) {
        set(frame)
    }
    
    fun set(frame: TelnetFrame) {
        if (rows.size != frame.getRowSize()) {
            initialData(frame.getRowSize())
        }
        for (i in 0 until rows.size) {
            rows[i].set(frame.getRow(i))
        }
    }
    
    fun initialData(row: Int) {
        rows.clear()
        repeat(row) {
            rows.add(TelnetRow())
        }
        clear()
    }
    
    fun clear() {
        for (row in 0 until rows.size) {
            getRow(row).clear()
        }
    }
    
    fun setPositionBitSpace(row: Int, column: Int, bitSpace: Byte) {
        getRow(row).bitSpace[column] = bitSpace
    }
    
    fun getPositionBitSpace(row: Int, column: Int): Byte {
        return getRow(row).bitSpace[column]
    }
    
    fun getPositionBlink(row: Int, column: Int): Boolean {
        return getRow(row).blink[column]
    }
    
    fun getPositionData(row: Int, column: Int): Int {
        val data = getRow(row).data[column]
        return data.toInt() and 255
    }
    
    fun getPositionTextColor(row: Int, column: Int): Int {
        val colorIndex = getRow(row).textColor[column]
        return TelnetAnsiCode.getTextColor(colorIndex)
    }
    
    fun getPositionBackgroundColor(row: Int, column: Int): Int {
        val colorIndex = getRow(row).backgroundColor[column]
        return TelnetAnsiCode.getBackgroundColor(colorIndex)
    }
    
    fun cleanPositionData(row: Int, column: Int) {
        getRow(row).cleanColumn(column)
    }
    
    fun setRow(index: Int, row: TelnetRow) {
        if (index in 0 until rows.size) {
            rows[index] = row
        }
    }
    
    fun getRow(index: Int): TelnetRow {
        return rows[index]
    }
    
    fun getRows(): Vector<TelnetRow> {
        return rows
    }
    
    fun getFirstRow(): TelnetRow {
        return rows.firstElement()
    }
    
    fun getLatestRow(): TelnetRow {
        return rows.lastElement()
    }
    
    fun switchRow(index: Int, andIndex: Int) {
        val row = rows[index]
        rows[index] = rows[andIndex]
        rows[andIndex] = row
    }
    
    fun getRowSize(): Int {
        return rows.size
    }
    
    // 新增方便的方法
    fun getRowCount(): Int = rows.size
    fun getColumnCount(): Int = DEFAULT_COLUMN
    
    fun getChar(row: Int, column: Int): Char {
        return getPositionData(row, column).toChar()
    }
    
    fun getAttribute(row: Int, column: Int): Int {
        // 組合屬性：文字顏色、背景顏色、閃爍等
        val textColor = getRow(row).textColor[column].toInt()
        val backgroundColor = getRow(row).backgroundColor[column].toInt()
        val blink = if (getRow(row).blink[column]) 0x80 else 0
        val italic = if (getRow(row).italic[column]) 0x40 else 0
        
        return (backgroundColor shl 16) or (textColor shl 8) or blink or italic
    }
    
    public override fun clone(): TelnetFrame {
        return TelnetFrame(this)
    }
    
    fun isEmpty(): Boolean {
        return rows.all { it.isEmpty() }
    }
    
    fun removeRow(index: Int) {
        rows.removeAt(index)
    }
    
    /** 雙字元判斷 */
    fun reloadSpace() {
        for (row in 0 until getRowSize()) {
            var column = 0
            while (column < DEFAULT_COLUMN) {
                val data = getPositionData(row, column)
                if (data > 127 && column < 79) {
                    val textColorDiff = getPositionTextColor(row, column) != getPositionTextColor(row, column + 1)
                    val backgroundColorDiff = getPositionBackgroundColor(row, column) != getPositionBackgroundColor(row, column + 1)
                    
                    if (textColorDiff || backgroundColorDiff) {
                        setPositionBitSpace(row, column, 3)
                        setPositionBitSpace(row, column + 1, 4)
                    } else {
                        setPositionBitSpace(row, column, 1)
                        setPositionBitSpace(row, column + 1, 2)
                    }
                    column++
                } else {
                    setPositionBitSpace(row, column, 0)
                }
                column++
            }
        }
    }
    
    fun cleanCachedData() {
        rows.forEach { it.cleanCachedData() }
    }
}
