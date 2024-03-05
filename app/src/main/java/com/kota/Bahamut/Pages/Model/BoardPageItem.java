package com.kota.Bahamut.Pages.Model;

import android.text.SpannableString;

import com.kota.Bahamut.ListPage.TelnetListPageItem;
import com.kota.Telnet.Model.TelnetRow;

import java.util.Stack;

public class BoardPageItem extends TelnetListPageItem {
    private static final Stack<BoardPageItem> _pool = new Stack<>();
    public String Author = null;
    public String Date = null;
    public int GY = 0;
    public int Size = 0;
    public String Title = null;
    public boolean isMarked = false;
    public boolean isRead = false;
    public boolean isReply = false;

    private BoardPageItem() {
    }

    public static void release() {
        synchronized (_pool) {
            _pool.clear();
        }
    }

    public static void recycle(BoardPageItem item) {
        synchronized (_pool) {
            _pool.push(item);
        }
    }

    public static BoardPageItem create() {
        BoardPageItem item = null;
        synchronized (_pool) {
            if (_pool.size() > 0) {
                item = _pool.pop();
            }
        }
        if (item == null) {
            return new BoardPageItem();
        }
        return item;
    }

    public void clear() {
        super.clear();
        this.Size = 0;
        this.isRead = false;
        this.isMarked = false;
        this.isReply = false;
        this.GY = 0;
        this.Date = null;
        this.Author = null;
        this.Title = null;
    }

    protected void finalize() throws Throwable {
        super.finalize();
    }

    public void set(TelnetListPageItem item) {
        super.set(item);
        if (item != null) {
            BoardPageItem article_item = (BoardPageItem) item;
            this.Size = article_item.Size;
            this.Date = article_item.Date;
            this.Author = article_item.Author;
            this.isRead = article_item.isRead;
            this.isMarked = article_item.isMarked;
            this.isReply = article_item.isReply;
            this.GY = article_item.GY;
            this.Title = article_item.Title;
        }
    }
}
