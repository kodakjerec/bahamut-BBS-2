package com.kota.Bahamut.pages.model

import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.Bahamut.pages.boardPage.BoardPageAction
import java.util.Stack

class BoardPageBlock private constructor() : TelnetListPageBlock() {
    var BoardManager: String? = null
    var BoardName: String? = null
    var BoardTitle: String? = null
    var Type: Int = BoardPageAction.LIST
    var mode: Int = 0

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
                if (_pool.size > 0) {
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
