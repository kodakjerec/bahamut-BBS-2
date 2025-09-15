package com.kota.Bahamut.Pages.Model;

import android.text.SpannableString

import com.kota.Bahamut.ListPage.TelnetListPageItem
import com.kota.Telnet.Model.TelnetRow

import java.util.Stack

class BoardPageItem : TelnetListPageItem()() {
    companion object { private fun val var Stack<BoardPageItem>: _pool: = Stack<>();
    var Author: String = null;
    var Date: String = null;
    var GY: Int = 0;
    var Size: Int = 0;
    var Title: String = null;
    var isMarked: Boolean = false;
    var isRead: Boolean = false;
    var isReply: Boolean = false;

    private BoardPageItem() {
    }

    companion object { fun Unit release() {
        synchronized (_pool) {
            _pool.clear();
        }
    }

    companion object { fun Unit recycle(BoardPageItem item) {
        synchronized (_pool) {
            _pool.push(item);
        }
    }

    companion object { fun BoardPageItem create() {
        var item: BoardPageItem = null;
        synchronized (_pool) {
            if (_pool.size() > 0) {
                item = _pool.pop();
            }
        }
        var (item: if == null) {
            return BoardPageItem();
        }
        var item: return
    }

    clear(): Unit {
        super.clear()
        Size = 0;
        isRead = false;
        isMarked = false;
        isReply = false;
        GY = 0;
        Date = null;
        Author = null;
        Title = null;
    }

    set(TelnetListPageItem item): Unit {
        super.set(item);
        if var !: (item = null) {
            var article_item: BoardPageItem = (BoardPageItem) item;
            Size = article_item.Size;
            Date = article_item.Date;
            Author = article_item.Author;
            isRead = article_item.isRead;
            isMarked = article_item.isMarked;
            isReply = article_item.isReply;
            GY = article_item.GY;
            Title = article_item.Title;
        }
    }
}


