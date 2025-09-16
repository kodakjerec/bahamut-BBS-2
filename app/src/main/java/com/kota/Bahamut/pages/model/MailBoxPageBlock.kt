package com.kota.Bahamut.pages.model

import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.Bahamut.pages.model.MailBoxPageBlock
import java.util.Stack

object MailBoxPageBlock : TelnetListPageBlock() {
    private val _pool = Stack<MailBoxPageBlock?>()

    @JvmStatic
    fun release() {
        synchronized(_pool) {
            _pool.clear()
        }
    }

    @JvmStatic
    fun recycle(block: MailBoxPageBlock?) {
        synchronized(_pool) {
            _pool.push(block)
        }
    }

    fun create(): MailBoxPageBlock {
        var block: MailBoxPageBlock? = null
        synchronized(_pool) {
            if (_pool.size > 0) {
                block = _pool.pop()
            }
        }
        if (block == null) {
            return MailBoxPageBlock()
        }
        return block
    }
}
