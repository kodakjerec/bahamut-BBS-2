package com.kota.telnet.logic

import com.kota.telnet.TelnetClient
import java.util.Vector

class SearchBoardHandler private constructor() {
    private val _boards = Vector<String?>()

    fun read() {
        var i = 3
        while (i < 23) {
            val content = TelnetClient.client!!.model!!.getRowString(i).trim { it <= ' ' }
            if (content.isNotEmpty()) {
                for (board in content.split(" +".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()) {
                    if (board.isNotEmpty()) {
                        this._boards.add(board)
                    }
                }
                i++
            } else {
                return
            }
        }
    }

    fun clear() {
        this._boards.clear()
    }

    val boardsSize: Int
        get() = this._boards.size

    fun getBoard(index: Int): String? {
        return this._boards[index]
    }

    val boards: Array<String?>
        get() = this._boards.toTypedArray<String?>()

    companion object {
        private var _instance: SearchBoardHandler? = null
        @JvmStatic
        val instance: SearchBoardHandler
            get() {
                if (_instance == null) {
                    _instance = SearchBoardHandler()
                }
                return _instance!!
            }
    }
}
