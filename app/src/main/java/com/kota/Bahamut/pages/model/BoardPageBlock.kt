package com.kota.Bahamut.pages.model

import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.Bahamut.pages.boardPage.BoardPageAction
import java.util.Stack

class BoardPageBlock private constructor() : TelnetListPageBlock() {
    var boardManager: String = ""
    var boardName: String = ""
    var boardTitle: String = ""
    var boardType: Int = BoardPageAction.LIST
    var boardMode: Int = 0

    companion object {
        private val _pool = Stack<BoardPageBlock?>()
        @JvmStatic
        fun release() {
            synchronized(_pool) {
                _pool.clear()
            }
        }

        fun recycle(block: BoardPageBlock?) {
            synchronized(_pool) {
                _pool.push(block)
            }
        }

        fun create(): BoardPageBlock {
            var block: BoardPageBlock? = null
            synchronized(_pool) {
                if (_pool.isNotEmpty()) {
                    block = _pool.pop()
                }
            }
            if (block == null) {
                return BoardPageBlock()
            }
            return block
        }
    }
}
