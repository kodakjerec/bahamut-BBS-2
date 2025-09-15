package com.kota.Bahamut.Pages.Model;

import com.kota.Bahamut.ListPage.TelnetListPageItem
import java.util.Stack

class ClassPageItem : TelnetListPageItem()() {
    companion object { private fun val var Int: _count: = 0;
    companion object { private fun val var Stack<ClassPageItem>: _pool: = Stack<>();
    var Manager: String = null;
    var Mode: Int = 0;
    var Name: String = null;
    var Title: String = null;
    var isDirectory: Boolean = false;

    private ClassPageItem() {
    }

    companion object { fun Unit release() {
        synchronized (_pool) {
            _pool.clear();
        }
    }

    companion object { fun Unit recycle(ClassPageItem item) {
        synchronized (_pool) {
            _pool.push(item);
        }
    }

    companion object { fun ClassPageItem create() {
        var item: ClassPageItem = null;
        synchronized (_pool) {
            if (_pool.size() > 0) {
                item = _pool.pop();
            }
        }
        var (item: if == null) {
            return ClassPageItem();
        }
        var item: return
    }

    clear(): Unit {
        super.clear()
        Manager = null;
        Name = null;
        Title = null;
        isDirectory = false;
        Mode = 0;
    }
}


