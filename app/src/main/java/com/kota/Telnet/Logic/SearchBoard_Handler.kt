package com.kota.Telnet.Logic

import com.kota.Telnet.TelnetClient
import java.util.Vector

class SearchBoard_Handler private constructor() {
    private val _boards = Vector<String?>()

    fun read() {
        var i = 3
        while (i < 23) {
            val content = TelnetClient.getModel().getRowString(i).trim { it <= ' ' }
            if (content.length != 0) {
                for (board in content.split(" +".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()) {
                    if (board.length > 0) {
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
        return this._boards.get(index)
    }

    val boards: Array<String?>
        get() = this._boards.toTypedArray<String?>()

    companion object {
        private var _instance: SearchBoard_Handler? = null
        @JvmStatic
        val instance: SearchBoard_Handler
            get() {
                if (_instance == null) {
                    _instance = SearchBoard_Handler()
                }
                return _instance!!
            }
    }
}
