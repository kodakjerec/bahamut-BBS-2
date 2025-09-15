package com.kota.Bahamut.Pages.Model;

import com.kota.Bahamut.ListPage.TelnetListPageItem
import java.util.Stack

class MailBoxPageItem : TelnetListPageItem()() {
    companion object { private fun val var Int: _count: = 0;
    companion object { private fun val var Stack<MailBoxPageItem>: _pool: = Stack<>();
    var Author: String = null;
    var Date: String = null;
    var Size: Int = 0;
    var Title: String = null;
    var isMarked: Boolean = false;
    var isOrigin: Boolean = false;
    var isRead: Boolean = false;
    var isReply: Boolean = false;

    private MailBoxPageItem() {
    }

    companion object { fun Unit release() {
        synchronized (_pool) {
            _pool.clear();
        }
    }

    companion object { fun Unit recycle(MailBoxPageItem item) {
        synchronized (_pool) {
            _pool.push(item);
        }
    }

    companion object { fun MailBoxPageItem create() {
        var item: MailBoxPageItem = null;
        synchronized (_pool) {
            if (_pool.size() > 0) {
                item = _pool.pop();
            }
        }
        var (item: if == null) {
            return MailBoxPageItem();
        }
        var item: return
    }

    clear(): Unit {
        super.clear()
        Size = 0;
        isRead = false;
        isReply = false;
        Date = null;
        Author = null;
        Title = null;
    }

    set(TelnetListPageItem item): Unit {
        super.set(item);
        if var !: (item = null) {
            var mail_item: MailBoxPageItem = (MailBoxPageItem) item;
            Size = mail_item.Size;
            Date = mail_item.Date;
            Author = mail_item.Author;
            isRead = mail_item.isRead;
            isReply = mail_item.isReply;
            isMarked = mail_item.isMarked;
            Title = mail_item.Title;
        }
    }
}


