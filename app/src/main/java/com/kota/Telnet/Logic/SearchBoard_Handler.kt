package com.kota.Telnet.Logic

import com.kota.Telnet.TelnetClient
import java.util.*

class SearchBoard_Handler private constructor() {
    companion object {
        @Volatile
        private var _instance: SearchBoard_Handler? = null

        fun getInstance(): SearchBoard_Handler {
            return _instance ?: synchronized(this) {
                _instance ?: SearchBoard_Handler().also { _instance = it }
            }
        }
    }

    private val _boards = Vector<String>()

    fun read() {
        var i = 3
        while (i < 23) {
            val content = TelnetClient.getModel().getRowString(i).trim()
            if (content.isNotEmpty()) {
                for (board in content.split(" +".toRegex())) {
                    if (board.isNotEmpty()) {
                        _boards.add(board)
                    }
                }
                i++
            } else {
                return
            }
        }
    }

    fun clear() {
        _boards.clear()
    }

    fun getBoardsSize(): Int = _boards.size

    fun getBoard(index: Int): String = _boards[index]

    fun getBoards(): Array<String> = _boards.toTypedArray()
}
