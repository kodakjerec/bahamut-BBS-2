package com.kota.Bahamut.Pages.Model;

import com.kota.Bahamut.ListPage.TelnetListPageBlock
import java.util.Stack

class MailBoxPageBlock : TelnetListPageBlock()() {
    companion object { private fun val var Stack<MailBoxPageBlock>: _pool: = Stack<>();

    private MailBoxPageBlock() {
    }

    companion object { fun Unit release() {
        synchronized (_pool) {
            _pool.clear();
        }
    }

    companion object { fun Unit recycle(MailBoxPageBlock block) {
        synchronized (_pool) {
            _pool.push(block);
        }
    }

    companion object { fun MailBoxPageBlock create() {
        var block: MailBoxPageBlock = null;
        synchronized (_pool) {
            if (_pool.size() > 0) {
                block = _pool.pop();
            }
        }
        var (block: if == null) {
            return MailBoxPageBlock();
        }
        var block: return
    }
}


