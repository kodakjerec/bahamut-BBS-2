package com.kota.Bahamut.Pages.Model;

import com.kota.Bahamut.ListPage.TelnetListPageItem;
import java.util.Stack;

public class MailBoxPageItem extends TelnetListPageItem {
    private static int _count = 0;
    private static Stack<MailBoxPageItem> _pool = new Stack<>();
    public String Author = null;
    public String Date = null;
    public int Size = 0;
    public String Title = null;
    public boolean isMarked = false;
    public boolean isOrigin = false;
    public boolean isRead = false;
    public boolean isReply = false;

    private MailBoxPageItem() {
    }

    public static void release() {
        synchronized (_pool) {
            _pool.clear();
        }
    }

    public static void recycle(MailBoxPageItem item) {
        synchronized (_pool) {
            _pool.push(item);
        }
    }

    public static MailBoxPageItem create() {
        MailBoxPageItem item = null;
        synchronized (_pool) {
            if (_pool.size() > 0) {
                item = _pool.pop();
            }
        }
        if (item == null) {
            return new MailBoxPageItem();
        }
        return item;
    }

    public void clear() {
        super.clear();
        this.Size = 0;
        this.isRead = false;
        this.isReply = false;
        this.Date = null;
        this.Author = null;
        this.Title = null;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        super.finalize();
    }

    public void set(TelnetListPageItem item) {
        super.set(item);
        if (item != null) {
            MailBoxPageItem mail_item = (MailBoxPageItem) item;
            this.Size = mail_item.Size;
            this.Date = mail_item.Date;
            this.Author = mail_item.Author;
            this.isRead = mail_item.isRead;
            this.isReply = mail_item.isReply;
            this.isMarked = mail_item.isMarked;
            this.Title = mail_item.Title;
        }
    }
}
