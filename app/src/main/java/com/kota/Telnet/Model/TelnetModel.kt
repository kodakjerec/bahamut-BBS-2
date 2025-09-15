package com.kota.Telnet.Model

import com.kota.Telnet.TelnetAnsi
import com.kota.Telnet.TelnetCursor
import java.nio.ByteBuffer
import java.util.*

class TelnetModel {
    
    companion object {
        private const val COUNT = 0
    }
    
    private var row: Int = 24
    protected var ansi: TelnetAnsi = TelnetAnsi()
    private var frame: TelnetFrame? = null
    private var cursor: TelnetCursor = TelnetCursor()
    private var savedCursor: TelnetCursor = TelnetCursor()
    private var pushedDataSize: Int = 0
    private val ansiBuffer: ByteBuffer = ByteBuffer.allocate(1024)
    
    constructor(row: Int) {
        this.row = row
        initialDataModel()
    }
    
    constructor() {
        this.row = 24
        initialDataModel()
    }
    
    fun cleanCachedData() {
        frame?.cleanCachedData()
    }
    
    fun clear() {
        ansi = TelnetAnsi()
        frame?.clear()
        cursor = TelnetCursor()
        savedCursor = TelnetCursor()
        pushedDataSize = 0
        ansiBuffer.clear()
    }
    
    private fun initialDataModel() {
        frame = TelnetFrame(row)
    }
    
    fun getRowSize(): Int = row
    
    fun cleanFrame() {
        frame?.clear()
    }
    
    fun setFrame(frame: TelnetFrame) {
        this.frame = frame
    }
    
    fun getBlink(row: Int, column: Int): Boolean {
        return frame?.getPositionBlink(row, column) ?: false
    }
    
    fun getData(row: Int, column: Int): Int {
        return frame?.getPositionData(row, column) ?: 0
    }
    
    fun getTextColor(row: Int, column: Int): Int {
        return frame?.getPositionTextColor(row, column) ?: 0
    }
    
    fun getBackgroundColor(row: Int, column: Int): Int {
        return frame?.getPositionBackgroundColor(row, column) ?: 0
    }
    
    fun getCursor(): TelnetCursor = cursor
    
    private fun cleanCursor(row: Int, column: Int) {
        frame?.cleanPositionData(row, column)
    }
    
    fun saveCursor() {
        savedCursor.set(cursor)
    }
    
    fun restoreCursor() {
        cursor.set(savedCursor)
    }
    
    fun setCursor(row: Int, column: Int) {
        cursor.set(row, column)
    }
    
    fun setCursorRow(row: Int) {
        var newRow = row
        if (newRow < 0) {
            newRow = 0
        } else if (newRow >= this.row) {
            newRow = this.row - 1
        }
        cursor.row = newRow
    }
    
    fun setCursorColumn(column: Int) {
        var newColumn = column
        if (newColumn < 0) {
            newColumn = 0
        } else if (newColumn >= 80) {
            newColumn = 79
        }
        cursor.column = newColumn
    }
    
    private fun setCursorData(data: Byte, ansiState: TelnetAnsi) {
        val currentFrame = frame ?: return
        val currentRow = cursor.row
        val currentColumn = cursor.column
        
        if (currentRow in 0 until row && currentColumn in 0 until 80) {
            val telnetRow = currentFrame.getRow(currentRow)
            telnetRow.data[currentColumn] = data
            telnetRow.textColor[currentColumn] = ansiState.textColor
            telnetRow.backgroundColor[currentColumn] = ansiState.backgroundColor
            telnetRow.blink[currentColumn] = ansiState.textBlink
            telnetRow.italic[currentColumn] = ansiState.textItalic
        }
    }
    
    fun pushData(data: Byte) {
        // 處理 ANSI 序列
        when (ansi.processAnsiData(data)) {
            TelnetAnsi.ANSI_NORMAL -> {
                setCursorData(data, ansi)
                moveCursorRight()
            }
            TelnetAnsi.ANSI_PROCESSING -> {
                // 繼續處理 ANSI 序列
            }
            TelnetAnsi.ANSI_COMMAND -> {
                executeAnsiCommand()
            }
        }
        pushedDataSize++
    }
    
    private fun moveCursorRight() {
        cursor.column++
        if (cursor.column >= 80) {
            cursor.column = 0
            cursor.row++
            if (cursor.row >= row) {
                cursor.row = row - 1
                scrollUp()
            }
        }
    }
    
    private fun scrollUp() {
        val currentFrame = frame ?: return
        // 向上滾動一行
        for (i in 1 until row) {
            currentFrame.getRow(i - 1).set(currentFrame.getRow(i))
        }
        currentFrame.getRow(row - 1).clear()
    }
    
    private fun executeAnsiCommand() {
        // 執行 ANSI 命令，這裡簡化處理
        when (ansi.command) {
            "H", "f" -> {
                // 移動游標
                val params = ansi.parameters
                val newRow = if (params.isNotEmpty()) params[0] - 1 else 0
                val newColumn = if (params.size > 1) params[1] - 1 else 0
                setCursor(newRow, newColumn)
            }
            "A" -> {
                // 游標上移
                val lines = if (ansi.parameters.isNotEmpty()) ansi.parameters[0] else 1
                setCursorRow(cursor.row - lines)
            }
            "B" -> {
                // 游標下移
                val lines = if (ansi.parameters.isNotEmpty()) ansi.parameters[0] else 1
                setCursorRow(cursor.row + lines)
            }
            "C" -> {
                // 游標右移
                val columns = if (ansi.parameters.isNotEmpty()) ansi.parameters[0] else 1
                setCursorColumn(cursor.column + columns)
            }
            "D" -> {
                // 游標左移
                val columns = if (ansi.parameters.isNotEmpty()) ansi.parameters[0] else 1
                setCursorColumn(cursor.column - columns)
            }
            "J" -> {
                // 清除螢幕
                val param = if (ansi.parameters.isNotEmpty()) ansi.parameters[0] else 0
                when (param) {
                    0 -> cleanFrameToEnd()
                    1 -> cleanFrameToBeginning()
                    2 -> cleanFrameAll()
                }
            }
            "K" -> {
                // 清除行
                val param = if (ansi.parameters.isNotEmpty()) ansi.parameters[0] else 0
                when (param) {
                    0 -> cleanLineToEnd()
                    1 -> cleanLineToBeginning()
                    2 -> cleanCurrentLine()
                }
            }
            "m" -> {
                // 設定顏色和樣式
                applyGraphicsMode()
            }
        }
    }
    
    fun getRowString(row: Int): String {
        return frame?.getRow(row)?.toString() ?: ""
    }
    
    fun getRow(row: Int): TelnetRow? {
        return if (row in 0 until this.row) {
            frame?.getRow(row)
        } else {
            null
        }
    }
    
    fun getRows(): Vector<TelnetRow>? {
        return frame?.getRows()
    }
    
    fun getLastRow(): TelnetRow? {
        return frame?.getLatestRow()
    }
    
    fun getFirstRow(): TelnetRow? {
        return frame?.getFirstRow()
    }
    
    fun getFrame(): TelnetFrame? = frame
    
    fun cleanFrameAll() {
        frame?.clear()
    }
    
    fun cleanFrameToEnd() {
        val currentFrame = frame ?: return
        
        for (row in cursor.row + 1 until this.row) {
            for (column in 0 until 80) {
                cleanCursor(row, column)
            }
        }
        
        for (column in cursor.column until 80) {
            cleanCursor(cursor.row, column)
        }
    }
    
    fun cleanFrameToBeginning() {
        val currentFrame = frame ?: return
        
        for (row in 0 until cursor.row) {
            for (column in 0 until 80) {
                cleanCursor(row, column)
            }
        }
        
        for (column in 0 until cursor.column) {
            cleanCursor(cursor.row, column)
        }
    }
    
    private fun cleanLineToEnd() {
        for (column in cursor.column until 80) {
            cleanCursor(cursor.row, column)
        }
    }
    
    private fun cleanLineToBeginning() {
        for (column in 0..cursor.column) {
            cleanCursor(cursor.row, column)
        }
    }
    
    private fun cleanCurrentLine() {
        for (column in 0 until 80) {
            cleanCursor(cursor.row, column)
        }
    }
    
    private fun applyGraphicsMode() {
        // 處理 SGR (Select Graphic Rendition) 參數
        if (ansi.parameters.isEmpty()) {
            // 重置所有屬性
            ansi.textColor = 7 // 白色
            ansi.backgroundColor = 0 // 黑色
            ansi.blink = false
            ansi.italic = false
            return
        }
        
        for (param in ansi.parameters) {
            when (param) {
                0 -> {
                    // 重置
                    ansi.textColor = 7
                    ansi.backgroundColor = 0
                    ansi.blink = false
                    ansi.italic = false
                }
                1 -> ansi.bold = true
                3 -> ansi.italic = true
                5 -> ansi.blink = true
                7 -> {
                    // 反轉顏色
                    val temp = ansi.textColor
                    ansi.textColor = ansi.backgroundColor
                    ansi.backgroundColor = temp
                }
                in 30..37 -> ansi.textColor = (param - 30).toByte()
                in 40..47 -> ansi.backgroundColor = (param - 40).toByte()
                in 90..97 -> ansi.textColor = (param - 90 + 8).toByte()
                in 100..107 -> ansi.backgroundColor = (param - 100 + 8).toByte()
            }
        }
    }
}
