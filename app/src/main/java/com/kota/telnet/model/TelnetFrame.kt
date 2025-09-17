package com.kota.telnet.model

import com.kota.telnet.reference.TelnetAnsiCode
import java.util.Vector

class TelnetFrame {
    @JvmField
    var rows: Vector<TelnetRow> = Vector<TelnetRow>()

    constructor() {
        initialData(DEFAULT_ROW)
        clear()
    }

    constructor(row: Int) {
        initialData(row)
        clear()
    }

    constructor(aFrame: TelnetFrame) {
        set(aFrame)
    }

    fun set(aFrame: TelnetFrame?) {
        if (aFrame != null && this.rows.size != aFrame.rowSize) {
            initialData(aFrame.rowSize)
        }
        if (aFrame != null) {
            for (i in this.rows.indices) {
                this.rows[i].set(aFrame.getRow(i))
            }
        }
    }

    fun initialData(row: Int) {
        this.rows.clear()
        for (i in 0..<row) {
            this.rows.add(TelnetRow())
        }
        clear()
    }

    fun clear() {
        for (row in this.rows.indices) {
            getRow(row)!!.clear()
        }
    }

    fun setPositionBitSpace(row: Int, column: Int, aBitSpace: Byte) {
        getRow(row)!!.bitSpace[column] = aBitSpace
    }

    fun getPositionBitSpace(row: Int, column: Int): Byte {
        return getRow(row)!!.bitSpace[column]
    }

    fun getPositionBlink(row: Int, column: Int): Boolean {
        return getRow(row)!!.blink[column]
    }

    fun getPositionData(row: Int, column: Int): Int {
        val data = getRow(row)!!.data[column].toInt()
        return data and 255
    }

    fun getPositionTextColor(row: Int, column: Int): Int {
        val colorIndex = getRow(row)!!.textColor[column]
        return TelnetAnsiCode.getTextColor(colorIndex)
    }

    fun getPositionBackgroundColor(row: Int, column: Int): Int {
        val colorIndex = getRow(row)!!.backgroundColor[column]
        return TelnetAnsiCode.getBackgroundColor(colorIndex)
    }

    fun cleanPositionData(row: Int, column: Int) {
        getRow(row)!!.cleanColumn(column)
    }

    fun setRow(index: Int, aRow: TelnetRow) {
        if (index >= 0 && index < this.rows.size) {
            this.rows[index] = aRow
        }
    }

    fun getRow(index: Int): TelnetRow {
        return this.rows[index]
    }

    val firstRow: TelnetRow
        get() = this.rows.firstElement()

    val latestRow: TelnetRow
        get() = this.rows.lastElement()

    fun switchRow(index: Int, andIndex: Int) {
        val row = this.rows[index]
        this.rows[index] = this.rows[andIndex]
        this.rows[andIndex] = row
    }

    val rowSize: Int
        get() = this.rows.size

    fun clone(): TelnetFrame {
        return TelnetFrame(this)
    }

    val isEmpty: Boolean
        get() = rows.all { it.isEmpty }

    fun removeRow(index: Int) {
        this.rows.removeAt(index)
    }

    /** 雙字元判斷  */
    fun reloadSpace() {
        for (row in 0..<this.rowSize) {
            var column = 0
            while (column < DEFAULT_COLUMN) {
                val data = getPositionData(row, column)
                if (data > 127 && column < 79) {
                    val textColorDiff =
                        getPositionTextColor(row, column) != getPositionTextColor(row, column + 1)
                    val backgroundColorDiff =
                        getPositionBackgroundColor(row, column) != getPositionBackgroundColor(
                            row,
                            column + 1
                        )
                    if (textColorDiff || backgroundColorDiff) {
                        setPositionBitSpace(row, column, 3.toByte())
                        setPositionBitSpace(row, column + 1, 4.toByte())
                    } else {
                        setPositionBitSpace(row, column, 1.toByte())
                        setPositionBitSpace(row, column + 1, 2.toByte())
                    }
                    column++
                } else {
                    setPositionBitSpace(row, column, 0.toByte())
                }
                column++
            }
        }
    }

    fun cleanCachedData() {
        for (row in this.rows) {
            row.cleanCachedData()
        }
    }

    companion object {
        const val DEFAULT_COLUMN: Int = 80
        const val DEFAULT_ROW: Int = 24
    }
}
