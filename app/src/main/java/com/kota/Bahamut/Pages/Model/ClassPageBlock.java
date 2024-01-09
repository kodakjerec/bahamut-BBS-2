package com.kota.Bahamut.Pages.Model;

import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import java.util.Stack;

public class ClassPageBlock extends TelnetListPageBlock {
    private static final Stack<ClassPageBlock> _pool = new Stack<>();
    public int mode = 0;

    private ClassPageBlock() {
    }

    public static void release() {
        synchronized (_pool) {
            _pool.clear();
        }
    }

    public static void recycle(ClassPageBlock block) {
        synchronized (_pool) {
            _pool.push(block);
        }
    }

    public static ClassPageBlock create() {
        ClassPageBlock block = null;
        synchronized (_pool) {
            if (_pool.size() > 0) {
                block = _pool.pop();
            }
        }
        if (block == null) {
            return new ClassPageBlock();
        }
        return block;
    }
}
