package com.kota.Bahamut.Pages.Model;

import com.kota.Bahamut.ListPage.TelnetListPageItem;
import java.util.Stack;

public class ClassPageItem extends TelnetListPageItem {
    private static final int _count = 0;
    private static final Stack<ClassPageItem> _pool = new Stack<>();
    public String Manager = null;
    public int Mode = 0;
    public String Name = null;
    public String Title = null;
    public boolean isDirectory = false;

    private ClassPageItem() {
    }

    public static void release() {
        synchronized (_pool) {
            _pool.clear();
        }
    }

    public static void recycle(ClassPageItem item) {
        synchronized (_pool) {
            _pool.push(item);
        }
    }

    public static ClassPageItem create() {
        ClassPageItem item = null;
        synchronized (_pool) {
            if (_pool.size() > 0) {
                item = _pool.pop();
            }
        }
        if (item == null) {
            return new ClassPageItem();
        }
        return item;
    }

    public void clear() {
        super.clear();
        this.Manager = null;
        this.Name = null;
        this.Title = null;
        this.isDirectory = false;
        this.Mode = 0;
    }

    /* access modifiers changed from: protected */
    protected void finalize() throws Throwable {
        super.finalize();
    }
}
