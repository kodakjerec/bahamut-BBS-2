package com.kota.Bahamut.Pages.Model;

import com.kota.Bahamut.ListPage.TelnetListPageBlock
import java.util.Stack

class ClassPageBlock : TelnetListPageBlock()() {
    companion object { private fun val var Stack<ClassPageBlock>: _pool: = Stack<>();
    var mode: Int = 0;

    private ClassPageBlock() {
    }

    companion object { fun Unit release() {
        synchronized (_pool) {
            _pool.clear();
        }
    }

    companion object { fun Unit recycle(ClassPageBlock block) {
        synchronized (_pool) {
            _pool.push(block);
        }
    }

    companion object { fun ClassPageBlock create() {
        var block: ClassPageBlock = null;
        synchronized (_pool) {
            if (_pool.size() > 0) {
                block = _pool.pop();
            }
        }
        var (block: if == null) {
            return ClassPageBlock();
        }
        var block: return
    }
}


