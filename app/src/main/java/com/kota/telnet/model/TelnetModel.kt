package com.kota.telnet.model

import com.kota.telnet.reference.TelnetAnsiCode
import com.kota.telnet.TelnetAnsi
import com.kota.telnet.TelnetCursor
import java.nio.ByteBuffer

class TelnetModel {
    val rowSize: Int
    protected var _ansi: TelnetAnsi = TelnetAnsi()
    private var _frame: TelnetFrame? = null
    var cursor: TelnetCursor = TelnetCursor()
        private set
    private var _saved_cursor = TelnetCursor()
    var pushedDataSize: Int = 0
        private set
    private val _ansi_buffer: ByteBuffer = ByteBuffer.allocate(1024)

    constructor(row: Int) {
        this.rowSize = row
        initialDataModel()
    }

    constructor() {
        this.rowSize = 24
        initialDataModel()
    }

    fun cleanCahcedData() {
        this._frame!!.cleanCachedData()
    }

    fun clear() {
        this._ansi = TelnetAnsi()
        this._frame!!.clear()
        this.cursor = TelnetCursor()
        this._saved_cursor = TelnetCursor()
        this.pushedDataSize = 0
        this._ansi_buffer.clear()
    }

    private fun initialDataModel() {
        this._frame = TelnetFrame(this.rowSize)
    }

    fun cleanFrame() {
        for (row in 0..<this.rowSize) {
            for (column in 0..79) {
                cleanCursor(row, column)
            }
        }
    }

    fun getBlink(row: Int, column: Int): Boolean {
        return this._frame!!.getRow(row).blink[column]
    }

    fun getData(row: Int, column: Int): Int {
        val data = this._frame!!.getRow(row).data[column].toInt()
        return data and 255
    }

    fun getTextColor(row: Int, column: Int): Int {
        val data = this._frame!!.getRow(row).textColor[column]
        return TelnetAnsiCode.getTextColor(data)
    }

    fun getBackgroundColor(row: Int, column: Int): Int {
        val data = this._frame!!.getRow(row).backgroundColor[column]
        return TelnetAnsiCode.getBackgroundColor(data)
    }

    private fun cleanCursor(row: Int, column: Int) {
        if (row >= 0 && row < this.rowSize && column >= 0 && column < 80) {
            this._frame!!.cleanPositionData(row, column)
        }
    }

    fun saveCursor() {
        this._saved_cursor.set(this.cursor)
    }

    fun restoreCursor() {
        this.cursor.set(this._saved_cursor)
    }

    fun setCursor(row: Int, column: Int) {
        setCursorRow(row)
        setCursorColumn(column)
    }

    fun setCursorRow(aRow: Int) {
        if (aRow < 0) {
            this.cursor.row = 0
        } else if (aRow > this.rowSize - 1) {
            this.cursor.row = this.rowSize - 1
        } else {
            this.cursor.row = aRow
        }
    }

    fun setCursorColumn(aColumn: Int) {
        if (aColumn < 0) {
            this.cursor.column = 0
        } else if (aColumn > 79) {
            this.cursor.column = 79
        } else {
            this.cursor.column = aColumn
        }
    }

    /** 給予顏色定義  */
    private fun setCursorData(data: Byte, ansiState: TelnetAnsi?) {
        if (this.cursor.row >= 0 && this.cursor.row < this.rowSize && this.cursor.column >= 0 && this.cursor.column < 80) {
            val row = this._frame!!.getRow(this.cursor.row)
            row.cleanColumn(this.cursor.column)
            row.data[this.cursor.column] = data
            // byte 顏色
            if (ansiState != null) {
                var text_color = ansiState.textColor
                if (ansiState.textBright) {
                    text_color = (text_color + 8).toByte()
                }
                row.textColor[this.cursor.column] = text_color
                row.backgroundColor[this.cursor.column] = ansiState.backgroundColor
                row.blink[this.cursor.column] = ansiState.textBlink
                row.italic[this.cursor.column] = ansiState.textItalic
            }
        }
    }

    fun pushData(data: Byte) {
        if (this.cursor.column < 80) {
            this.pushedDataSize++
            setCursorData(data, this._ansi)
            this.cursor.column++
        }
    }

    fun getRowString(row: Int): String {
        return this._frame!!.getRow(row).toString()
    }

    fun getRow(row: Int): TelnetRow? {
        if (row < 0 || row >= this.rowSize) {
            return null
        }
        return this._frame!!.getRow(row)
    }

    val rows: Vector<TelnetRow?>?
        get() = this._frame!!.getRows()

    val lastRow: TelnetRow?
        get() = this._frame!!.getLatestRow()

    val firstRow: TelnetRow?
        get() = this._frame!!.getFirstRow()

    var frame: TelnetFrame?
        get() = this._frame
        set(aFrame) {
            this._frame!!.set(aFrame)
        }

    fun cleanFrameAll() {
        this._frame!!.clear()
    }

    fun cleanFrameToEnd() {
        for (row in this.cursor.row + 1..<this.rowSize) {
            for (column in 0..79) {
                cleanCursor(row, column)
            }
        }
        for (column2 in this.cursor.column..79) {
            cleanCursor(this.cursor.row, column2)
        }
    }

    fun cleanFrameToBeginning() {
        for (row in 0..<this.cursor.row) {
            for (column in 0..79) {
                cleanCursor(row, column)
            }
        }
        for (column2 in 0..<this.cursor.column) {
            cleanCursor(this.cursor.row, column2)
        }
    }

    fun cleanRow(row: Int) {
        for (column in 0..79) {
            cleanCursor(row, column)
        }
    }

    fun cleanRowAll() {
        for (column in 0..79) {
            cleanCursor(this.cursor.row, column)
        }
    }

    fun cleanRowToBeginning() {
        for (column in 0..<this.cursor.column) {
            cleanCursor(this.cursor.row, column)
        }
    }

    fun cleanRowToEnd() {
        for (column in this.cursor.column..79) {
            cleanCursor(this.cursor.row, column)
        }
    }

    fun moveCursorColumnToBegin() {
        this.cursor.column = 0
    }

    fun moveCursorColumnToEnd() {
        this.cursor.column = 79
    }

    fun moveCursorRowToBegin() {
        this.cursor.row = 0
    }

    fun moveCursorRowToEnd() {
        this.cursor.row = 79
    }

    fun moveCursorColumnLeft() {
        if (this.cursor.column > 0) {
            val telnetCursor = this.cursor
            telnetCursor.column--
        }
    }

    fun moveCursorColumnLeft(n: Int) {
        var n = n
        if (n < 1) {
            n = 1
        }
        for (i in 0..<n) {
            moveCursorColumnLeft()
        }
    }

    fun moveCursorColumnRight() {
        if (this.cursor.column < 79) {
            this.cursor.column++
        }
    }

    fun moveCursorColumnRight(n: Int) {
        var n = n
        if (n < 1) {
            n = 1
        }
        for (i in 0..<n) {
            moveCursorColumnRight()
        }
    }

    fun moveCursorToNextLine() {
        moveCursorColumnToBegin()
        if (this.cursor.row == this.rowSize - 1) {
            for (i in 0..<this.rowSize - 1) {
                this._frame!!.switchRow(i, i + 1)
            }
            this._frame!!.getLatestRow().clear()
        } else if (this.cursor.row < this.rowSize - 1) {
            this.cursor.row++
        }
    }

    fun moveCursorRowDown() {
        if (this.cursor.row < this.rowSize - 1) {
            this.cursor.row++
        }
    }

    fun moveCursorRowDown(n: Int) {
        var n = n
        if (n < 1) {
            n = 1
        }
        for (i in 0..<n) {
            moveCursorRowDown()
        }
    }

    fun moveCursorRowUp() {
        if (this.cursor.column > 0) {
            val telnetCursor = this.cursor
            telnetCursor.column--
        }
    }

    fun moveCursorRowUp(n: Int) {
        var n = n
        if (n < 1) {
            n = 1
        }
        for (i in 0..<n) {
            moveCursorRowUp()
        }
    }

    fun cleanPushedDataSize() {
        this.pushedDataSize = 0
    }

    fun pushAnsiBuffer(data: Byte) {
        this._ansi_buffer.put(data)
    }

    fun cleanAnsiBuffer() {
        this._ansi_buffer.clear()
    }

    fun parseAnsiBuffer() {
        this._ansi_buffer.flip()
        val cmd = this._ansi_buffer.get(this._ansi_buffer.limit() - 1).toInt() and 255
        when (cmd) {
            65 -> {
                onReceivedAnsiControlCUU()
                return
            }

            66 -> {
                onReceivedAnsiControlCUD()
                return
            }

            67 -> {
                onReceivedAnsiControlCUF()
                return
            }

            68 -> {
                onReceivedAnsiControlCUB()
                return
            }

            69 -> {
                onReceivedAnsiControlCNL()
                return
            }

            70 -> {
                onReceivedAnsiControlCPL()
                return
            }

            71 -> {
                onReceivedAnsiControlCHA()
                return
            }

            72 -> {
                onReceivedAnsiControlCUP()
                return
            }

            74 -> {
                onReceivedAnsiControlED()
                return
            }

            75 -> {
                onReceivedAnsiControlEL()
                return
            }

            83 -> {
                onReceivedAnsiControlSU()
                return
            }

            84 -> {
                onReceivedAnsiControlSD()
                return
            }

            102 -> {
                onReceivedAnsiControlHVP()
                return
            }

            109 -> {
                onReceivedAnsiControlSGR()
                return
            }

            110 -> {
                onReceivedAnsiControlDSR()
                return
            }

            115 -> {
                onReceivedAnsiControlSCP()
                return
            }

            117 -> {
                onReceivedAnsiControlRCP()
                return
            }

            else -> {
                onReceivedUnknownAnsiControl()
                return
            }
        }
    }

    private fun readIntegerFromAnsiBuffer(): Int {
        var data: Int
        var state = 0
        while (this._ansi_buffer.position() < this._ansi_buffer.limit() && ((this._ansi_buffer.get()
                .toInt() and 255).also { data = it }) >= 48 && data <= 57
        ) {
            state = (state * 10) + (data - 48)
        }
        return state
    }

    fun onReceivedAnsiControlSGR() {
        if (this._ansi_buffer.limit() == 1) {
            parseSGRState(0)
        } else if (this._ansi_buffer.limit() > 1) {
            while (this._ansi_buffer.position() < this._ansi_buffer.limit()) {
                parseSGRState(readIntegerFromAnsiBuffer())
            }
        } else {
            onReceivedUnknownAnsiControl()
        }
    }

    private fun parseSGRState(state: Int) {
        when (state) {
            0 -> {
                this._ansi.resetToDefaultState()
                return
            }

            1 -> {
                this._ansi.textBright = true
                return
            }

            2 -> {
                this._ansi.textBright = false
                return
            }

            3 -> {
                this._ansi.textItalic = true
                return
            }

            4, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 27, 28, 29, 38, 48, 51, 52, 53, 54, 55, 60, 61, 62, 63, 64 -> return
            5 -> {
                this._ansi.textBlink = true
                return
            }

            6 -> {
                this._ansi.textBlink = true
                return
            }

            7 -> {
                val text_color = this._ansi.textColor
                this._ansi.textColor = this._ansi.backgroundColor
                this._ansi.backgroundColor = text_color
                return
            }

            25 -> {
                this._ansi.textBlink = false
                return
            }

            26, 50, 56, 57, 58, 59 -> {
                this._ansi.resetToDefaultState()
                println("Unsupported SGR code : " + state)
                return
            }

            30 -> {
                this._ansi.textColor = 0.toByte()
                return
            }

            31 -> {
                this._ansi.textColor = 1.toByte()
                return
            }

            32 -> {
                this._ansi.textColor = 2.toByte()
                return
            }

            33 -> {
                this._ansi.textColor = 3.toByte()
                return
            }

            34 -> {
                this._ansi.textColor = 4.toByte()
                return
            }

            35 -> {
                this._ansi.textColor = 5.toByte()
                return
            }

            36 -> {
                this._ansi.textColor = 6.toByte()
                return
            }

            37 -> {
                this._ansi.textColor = 7.toByte()
                return
            }

            39 -> {
                this._ansi.textColor = TelnetAnsi.defaultTextColor
                return
            }

            40 -> {
                this._ansi.backgroundColor = 0.toByte()
                return
            }

            41 -> {
                this._ansi.backgroundColor = 1.toByte()
                return
            }

            42 -> {
                this._ansi.backgroundColor = 2.toByte()
                return
            }

            43 -> {
                this._ansi.backgroundColor = 3.toByte()
                return
            }

            44 -> {
                this._ansi.backgroundColor = 4.toByte()
                return
            }

            45 -> {
                this._ansi.backgroundColor = 5.toByte()
                return
            }

            46 -> {
                this._ansi.backgroundColor = 6.toByte()
                return
            }

            47 -> {
                this._ansi.backgroundColor = 7.toByte()
                return
            }

            49 -> {
                this._ansi.backgroundColor = TelnetAnsi.defaultBackgroundColor
                return
            }

            else -> {
                this._ansi.resetToDefaultState()
                println("Unsupported SGR code : " + state)
                return
            }
        }
    }

    fun onReceivedAnsiControlSCP() {
        if (this._ansi_buffer.limit() == 1) {
            saveCursor()
        } else {
            onReceivedUnknownAnsiControl()
        }
    }

    fun onReceivedAnsiControlRCP() {
        if (this._ansi_buffer.limit() == 1) {
            restoreCursor()
        } else {
            onReceivedUnknownAnsiControl()
        }
    }

    fun onReceivedAnsiControlCUU() {
        if (this._ansi_buffer.limit() == 1) {
            moveCursorRowUp(1)
        } else if (this._ansi_buffer.limit() > 1) {
            moveCursorRowUp(readIntegerFromAnsiBuffer())
        } else {
            onReceivedUnknownAnsiControl()
        }
    }

    fun onReceivedAnsiControlCUD() {
        if (this._ansi_buffer.limit() == 1) {
            moveCursorRowDown(1)
        } else if (this._ansi_buffer.limit() > 1) {
            moveCursorRowDown(readIntegerFromAnsiBuffer())
        } else {
            onReceivedUnknownAnsiControl()
        }
    }

    fun onReceivedAnsiControlCUF() {
        if (this._ansi_buffer.limit() == 1) {
            moveCursorColumnRight(1)
        } else if (this._ansi_buffer.limit() > 1) {
            moveCursorColumnRight(readIntegerFromAnsiBuffer())
        } else {
            onReceivedUnknownAnsiControl()
        }
    }

    fun onReceivedAnsiControlCUB() {
        if (this._ansi_buffer.limit() == 1) {
            moveCursorColumnLeft(1)
        } else if (this._ansi_buffer.limit() > 1) {
            moveCursorColumnLeft(readIntegerFromAnsiBuffer())
        } else {
            onReceivedUnknownAnsiControl()
        }
    }

    fun onReceivedAnsiControlCNL() {
        if (this._ansi_buffer.limit() == 1) {
            moveCursorRowDown(1)
        } else if (this._ansi_buffer.limit() > 1) {
            moveCursorRowDown(readIntegerFromAnsiBuffer())
        } else {
            onReceivedUnknownAnsiControl()
        }
    }

    fun onReceivedAnsiControlHVP() {
        onReceivedAnsiControlCUP()
    }

    fun onReceivedAnsiControlCPL() {
        if (this._ansi_buffer.limit() == 1) {
            moveCursorRowUp(1)
            moveCursorColumnToBegin()
        } else if (this._ansi_buffer.limit() > 1) {
            moveCursorRowUp(readIntegerFromAnsiBuffer())
            moveCursorColumnToBegin()
        } else {
            onReceivedUnknownAnsiControl()
        }
    }

    fun onReceivedAnsiControlCHA() {
        if (this._ansi_buffer.limit() > 1) {
            setCursorColumn(readIntegerFromAnsiBuffer() - 1)
        } else {
            onReceivedUnknownAnsiControl()
        }
    }

    fun onReceivedAnsiControlCUP() {
        if (this._ansi_buffer.limit() > 1) {
            setCursor(readIntegerFromAnsiBuffer() - 1, readIntegerFromAnsiBuffer() - 1)
        } else {
            onReceivedUnknownAnsiControl()
        }
    }

    fun onReceivedAnsiControlED() {
        var state = 0
        if (this._ansi_buffer.limit() == 1) {
            state = 0
        } else if (this._ansi_buffer.limit() > 1) {
            state = readIntegerFromAnsiBuffer()
        } else {
            onReceivedUnknownAnsiControl()
        }
        when (state) {
            1 -> {
                cleanFrameToBeginning()
                return
            }

            2 -> {
                cleanFrameAll()
                setCursor(0, 0)
                return
            }

            else -> {
                cleanFrameToEnd()
                return
            }
        }
    }

    fun onReceivedAnsiControlEL() {
        var state = 0
        if (this._ansi_buffer.limit() == 1) {
            state = 0
        } else if (this._ansi_buffer.limit() > 1) {
            state = readIntegerFromAnsiBuffer()
        } else {
            onReceivedUnknownAnsiControl()
        }
        when (state) {
            1 -> {
                cleanRowToBeginning()
                return
            }

            2 -> {
                cleanRowAll()
                return
            }

            else -> {
                cleanRowToEnd()
                return
            }
        }
    }

    fun onReceivedAnsiControlSU() {
        if (this._ansi_buffer.limit() != 1) {
            onReceivedUnknownAnsiControl()
        }
    }

    fun onReceivedAnsiControlSD() {
        if (this._ansi_buffer.limit() != 1) {
            onReceivedUnknownAnsiControl()
        }
    }

    fun onReceivedAnsiControlDSR() {
        if (this._ansi_buffer.limit() != 1 || this._ansi_buffer.get(0).toInt() != 6) {
            onReceivedUnknownAnsiControl()
        }
    }

    fun onReceivedUnknownAnsiControl() {
        println("get unsupport ansi control : " + this.ansiBufferString)
    }

    val ansiBufferString: String
        get() {
            var str = ""
            for (i in 0..<this._ansi_buffer.limit()) {
                val c = this._ansi_buffer.get(i).toInt() and 255
                str = str + c.toChar().toString()
            }
            return str
        }

    companion object {
        private const val _count = 0
    }
}
