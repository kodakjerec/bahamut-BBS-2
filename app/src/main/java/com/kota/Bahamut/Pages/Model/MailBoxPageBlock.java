package com.kota.Bahamut.Pages.Model;

import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import java.util.Stack;

public class MailBoxPageBlock extends TelnetListPageBlock {
    private static Stack<MailBoxPageBlock> _pool = new Stack<>();

    private MailBoxPageBlock() {
    }

    public static void release() {
        synchronized (_pool) {
            _pool.clear();
        }
    }

    public static void recycle(MailBoxPageBlock block) {
        synchronized (_pool) {
            _pool.push(block);
        }
    }

    public static MailBoxPageBlock create() {
        MailBoxPageBlock block = null;
        synchronized (_pool) {
            if (_pool.size() > 0) {
                block = _pool.pop();
            }
        }
        if (block == null) {
            return new MailBoxPageBlock();
        }
        return block;
    }
}
