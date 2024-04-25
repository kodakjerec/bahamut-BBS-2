package com.kota.Bahamut.Pages.Model;

import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Bahamut.Pages.BoardPage.BoardPageAction;

import java.util.Stack;

public class BoardPageBlock extends TelnetListPageBlock {
    private static final Stack<BoardPageBlock> _pool = new Stack<>();
    public String BoardManager = null;
    public String BoardName = null;
    public String BoardTitle = null;
    public int Type = BoardPageAction.LIST;
    public int mode = 0;

    private BoardPageBlock() {
    }

    public static void release() {
        synchronized (_pool) {
            _pool.clear();
        }
    }

    public static void recycle(BoardPageBlock block) {
        synchronized (_pool) {
            _pool.push(block);
        }
    }

    public static BoardPageBlock create() {
        BoardPageBlock block = null;
        synchronized (_pool) {
            if (_pool.size() > 0) {
                block = _pool.pop();
            }
        }
        if (block == null) {
            return new BoardPageBlock();
        }
        return block;
    }
}
