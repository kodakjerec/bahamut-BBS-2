package com.kota.telnet.model

import com.kota.telnet.reference.TelnetAnsiCode
import com.kota.telnet.TelnetAnsi
import com.kota.telnet.TelnetCursor
import java.nio.ByteBuffer
import java.util.Vector

class TelnetModel {
    val rowSize: Int
    var telnetAnsi: TelnetAnsi = TelnetAnsi()
    private var telnetFrame: TelnetFrame? = null
    var cursor: TelnetCursor = TelnetCursor()
        private set
    private var savedCursor = TelnetCursor()
    var pushedDataSize: Int = 0
        private set
    private val byteBuffer: ByteBuffer = ByteBuffer.allocate(1024)

    constructor(row: Int) {
        this.rowSize = row
        initialDataModel()
    }

    constructor() {
        this.rowSize = 24
        initialDataModel()
    }

    fun cleanCachedData() {
        this.telnetFrame!!.cleanCachedData()
    }

    fun clear() {
        this.telnetAnsi = TelnetAnsi()
        this.telnetFrame!!.clear()
        this.cursor = TelnetCursor()
        this.savedCursor = TelnetCursor()
        this.pushedDataSize = 0
        this.byteBuffer.clear()
    }

    private fun initialDataModel() {
        this.telnetFrame = TelnetFrame(this.rowSize)
    }

    fun cleanFrame() {
        for (row in 0..<this.rowSize) {
            for (column in 0..79) {
                cleanCursor(row, column)
            }
        }
    }

    fun getBlink(row: Int, column: Int): Boolean {
        return this.telnetFrame!!.getRow(row)!!.blink[column]
    }

    fun getData(row: Int, column: Int): Int {
        val data = this.telnetFrame!!.getRow(row)!!.data[column].toInt()
        return data and 255
    }

    fun getTextColor(row: Int, column: Int): Int {
        val data = this.telnetFrame!!.getRow(row)!!.textColor[column]
        return TelnetAnsiCode.getTextColor(data)
    }

    fun getBackgroundColor(row: Int, column: Int): Int {
        val data = this.telnetFrame!!.getRow(row)!!.backgroundColor[column]
        return TelnetAnsiCode.getBackgroundColor(data)
    }

    private fun cleanCursor(row: Int, column: Int) {
        if (row >= 0 && row < this.rowSize && column >= 0 && column < 80) {
            this.telnetFrame!!.cleanPositionData(row, column)
        }
    }

    fun saveCursor() {
        this.savedCursor.set(this.cursor)
    }

    fun restoreCursor() {
        this.cursor.set(this.savedCursor)
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
            val row = this.telnetFrame!!.getRow(this.cursor.row)
            row?.cleanColumn(this.cursor.column)
            row?.data[this.cursor.column] = data
            // byte 顏色
            if (ansiState != null) {
                var textColor = ansiState.textColor
                if (ansiState.textBright) {
                    textColor = (textColor + 8).toByte()
                }
                row?.textColor[this.cursor.column] = textColor
                row?.backgroundColor[this.cursor.column] = ansiState.backgroundColor
                row?.blink[this.cursor.column] = ansiState.textBlink
                row?.italic[this.cursor.column] = ansiState.textItalic
            }
        }
    }

    fun pushData(data: Byte) {
        if (this.cursor.column < 80) {
            this.pushedDataSize++
            setCursorData(data, this.telnetAnsi)
            this.cursor.column++
        }
    }

    fun getRowString(row: Int): String {
        return this.telnetFrame!!.getRow(row).toString()
    }

    fun getRow(row: Int): TelnetRow? {
        if (row < 0 || row >= this.rowSize) {
            return null
        }
        return this.telnetFrame!!.getRow(row)
    }

    val rows: Vector<TelnetRow>
        get() = this.telnetFrame!!.rows

    val lastRow: TelnetRow
        get() = this.telnetFrame!!.latestRow

    val firstRow: TelnetRow
        get() = this.telnetFrame!!.firstRow

    var frame: TelnetFrame?
        get() = this.telnetFrame
        set(aFrame) {
            this.telnetFrame!!.set(aFrame)
        }

    fun cleanFrameAll() {
        this.telnetFrame!!.clear()
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
                this.telnetFrame!!.switchRow(i, i + 1)
            }
            this.telnetFrame!!.latestRow?.clear()
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
        this.byteBuffer.put(data)
    }

    fun cleanAnsiBuffer() {
        this.byteBuffer.clear()
    }

    fun parseAnsiBuffer() {
        this.byteBuffer.flip()
        val cmd = this.byteBuffer.get(this.byteBuffer.limit() - 1).toInt() and 255
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
        var data = 0
        var state = 0
        while (this.byteBuffer.position() < this.byteBuffer.limit() && ((this.byteBuffer.get()
                .toInt() and 255).also { data = it }) >= 48 && data <= 57
        ) {
            state = (state * 10) + (data - 48)
        }
        return state
    }

    fun onReceivedAnsiControlSGR() {
        if (this.byteBuffer.limit() == 1) {
            parseSGRState(0)
        } else if (this.byteBuffer.limit() > 1) {
            while (this.byteBuffer.position() < this.byteBuffer.limit()) {
                parseSGRState(readIntegerFromAnsiBuffer())
            }
        } else {
            onReceivedUnknownAnsiControl()
        }
    }

    private fun parseSGRState(state: Int) {
        when (state) {
            0 -> {
                this.telnetAnsi.resetToDefaultState()
                return
            }

            1 -> {
                this.telnetAnsi.textBright = true
                return
            }

            2 -> {
                this.telnetAnsi.textBright = false
                return
            }

            3 -> {
                this.telnetAnsi.textItalic = true
                return
            }

            4, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 27, 28, 29, 38, 48, 51, 52, 53, 54, 55, 60, 61, 62, 63, 64 -> return
            5 -> {
                this.telnetAnsi.textBlink = true
                return
            }

            6 -> {
                this.telnetAnsi.textBlink = true
                return
            }

            7 -> {
                val textColor = this.telnetAnsi.textColor
                this.telnetAnsi.textColor = this.telnetAnsi.backgroundColor
                this.telnetAnsi.backgroundColor = textColor
                return
            }

            25 -> {
                this.telnetAnsi.textBlink = false
                return
            }

            26, 50, 56, 57, 58, 59 -> {
                this.telnetAnsi.resetToDefaultState()
                println("Unsupported SGR code : $state")
                return
            }

            30 -> {
                this.telnetAnsi.textColor = 0.toByte()
                return
            }

            31 -> {
                this.telnetAnsi.textColor = 1.toByte()
                return
            }

            32 -> {
                this.telnetAnsi.textColor = 2.toByte()
                return
            }

            33 -> {
                this.telnetAnsi.textColor = 3.toByte()
                return
            }

            34 -> {
                this.telnetAnsi.textColor = 4.toByte()
                return
            }

            35 -> {
                this.telnetAnsi.textColor = 5.toByte()
                return
            }

            36 -> {
                this.telnetAnsi.textColor = 6.toByte()
                return
            }

            37 -> {
                this.telnetAnsi.textColor = 7.toByte()
                return
            }

            39 -> {
                this.telnetAnsi.textColor = TelnetAnsi.DEFAULT_TEXT_COLOR
                return
            }

            40 -> {
                this.telnetAnsi.backgroundColor = 0.toByte()
                return
            }

            41 -> {
                this.telnetAnsi.backgroundColor = 1.toByte()
                return
            }

            42 -> {
                this.telnetAnsi.backgroundColor = 2.toByte()
                return
            }

            43 -> {
                this.telnetAnsi.backgroundColor = 3.toByte()
                return
            }

            44 -> {
                this.telnetAnsi.backgroundColor = 4.toByte()
                return
            }

            45 -> {
                this.telnetAnsi.backgroundColor = 5.toByte()
                return
            }

            46 -> {
                this.telnetAnsi.backgroundColor = 6.toByte()
                return
            }

            47 -> {
                this.telnetAnsi.backgroundColor = 7.toByte()
                return
            }

            49 -> {
                this.telnetAnsi.backgroundColor = TelnetAnsi.DEFAULT_BACKGROUND_COLOR
                return
            }

            else -> {
                this.telnetAnsi.resetToDefaultState()
                println("Unsupported SGR code : $state")
                return
            }
        }
    }

    fun onReceivedAnsiControlSCP() {
        if (this.byteBuffer.limit() == 1) {
            saveCursor()
        } else {
            onReceivedUnknownAnsiControl()
        }
    }

    fun onReceivedAnsiControlRCP() {
        if (this.byteBuffer.limit() == 1) {
            restoreCursor()
        } else {
            onReceivedUnknownAnsiControl()
        }
    }

    fun onReceivedAnsiControlCUU() {
        if (this.byteBuffer.limit() == 1) {
            moveCursorRowUp(1)
        } else if (this.byteBuffer.limit() > 1) {
            moveCursorRowUp(readIntegerFromAnsiBuffer())
        } else {
            onReceivedUnknownAnsiControl()
        }
    }

    fun onReceivedAnsiControlCUD() {
        if (this.byteBuffer.limit() == 1) {
            moveCursorRowDown(1)
        } else if (this.byteBuffer.limit() > 1) {
            moveCursorRowDown(readIntegerFromAnsiBuffer())
        } else {
            onReceivedUnknownAnsiControl()
        }
    }

    fun onReceivedAnsiControlCUF() {
        if (this.byteBuffer.limit() == 1) {
            moveCursorColumnRight(1)
        } else if (this.byteBuffer.limit() > 1) {
            moveCursorColumnRight(readIntegerFromAnsiBuffer())
        } else {
            onReceivedUnknownAnsiControl()
        }
    }

    fun onReceivedAnsiControlCUB() {
        if (this.byteBuffer.limit() == 1) {
            moveCursorColumnLeft(1)
        } else if (this.byteBuffer.limit() > 1) {
            moveCursorColumnLeft(readIntegerFromAnsiBuffer())
        } else {
            onReceivedUnknownAnsiControl()
        }
    }

    fun onReceivedAnsiControlCNL() {
        if (this.byteBuffer.limit() == 1) {
            moveCursorRowDown(1)
        } else if (this.byteBuffer.limit() > 1) {
            moveCursorRowDown(readIntegerFromAnsiBuffer())
        } else {
            onReceivedUnknownAnsiControl()
        }
    }

    fun onReceivedAnsiControlHVP() {
        onReceivedAnsiControlCUP()
    }

    fun onReceivedAnsiControlCPL() {
        if (this.byteBuffer.limit() == 1) {
            moveCursorRowUp(1)
            moveCursorColumnToBegin()
        } else if (this.byteBuffer.limit() > 1) {
            moveCursorRowUp(readIntegerFromAnsiBuffer())
            moveCursorColumnToBegin()
        } else {
            onReceivedUnknownAnsiControl()
        }
    }

    fun onReceivedAnsiControlCHA() {
        if (this.byteBuffer.limit() > 1) {
            setCursorColumn(readIntegerFromAnsiBuffer() - 1)
        } else {
            onReceivedUnknownAnsiControl()
        }
    }

    fun onReceivedAnsiControlCUP() {
        if (this.byteBuffer.limit() > 1) {
            setCursor(readIntegerFromAnsiBuffer() - 1, readIntegerFromAnsiBuffer() - 1)
        } else {
            onReceivedUnknownAnsiControl()
        }
    }

    fun onReceivedAnsiControlED() {
        var state = 0
        if (this.byteBuffer.limit() == 1) {
            state = 0
        } else if (this.byteBuffer.limit() > 1) {
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
        if (this.byteBuffer.limit() == 1) {
            state = 0
        } else if (this.byteBuffer.limit() > 1) {
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
        if (this.byteBuffer.limit() != 1) {
            onReceivedUnknownAnsiControl()
        }
    }

    fun onReceivedAnsiControlSD() {
        if (this.byteBuffer.limit() != 1) {
            onReceivedUnknownAnsiControl()
        }
    }

    fun onReceivedAnsiControlDSR() {
        if (this.byteBuffer.limit() != 1 || this.byteBuffer.get(0).toInt() != 6) {
            onReceivedUnknownAnsiControl()
        }
    }

    fun onReceivedUnknownAnsiControl() {
        println("get unsupported ansi control : " + this.ansiBufferString)
    }

    val ansiBufferString: String
        get() {
            var str = ""
            for (i in 0..<this.byteBuffer.limit()) {
                val c = this.byteBuffer.get(i).toInt() and 255
                str = str + c.toChar().toString()
            }
            return str
        }

    companion object {
        private const val MY_COUNT = 0
    }
}
