package com.kota.Bahamut.Pages.Model;

import com.kota.Bahamut.ListPage.TelnetListPageBlock
import com.kota.Bahamut.Pages.BoardPage.BoardPageAction

import java.util.Stack

class BoardPageBlock : TelnetListPageBlock()() {
    companion object { private fun val var Stack<BoardPageBlock>: _pool: = Stack<>();
    var BoardManager: String = null;
    var BoardName: String = null;
    var BoardTitle: String = null;
    var Type: Int = BoardPageAction.LIST;
    var mode: Int = 0;

    private BoardPageBlock() {
    }

    companion object { fun Unit release() {
        synchronized (_pool) {
            _pool.clear();
        }
    }

    companion object { fun Unit recycle(BoardPageBlock block) {
        synchronized (_pool) {
            _pool.push(block);
        }
    }

    companion object { fun BoardPageBlock create() {
        var block: BoardPageBlock = null;
        synchronized (_pool) {
            if (_pool.size() > 0) {
                block = _pool.pop();
            }
        }
        var (block: if == null) {
            return BoardPageBlock();
        }
        var block: return
    }
}


