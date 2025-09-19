package com.kota.Bahamut.pages.model

import com.kota.Bahamut.listPage.TelnetListPageBlock
import java.util.Stack

class MailBoxPageBlock : TelnetListPageBlock() {
    
    companion object {
        private val myPool = Stack<MailBoxPageBlock?>()

        @JvmStatic
        fun release() {
            synchronized(myPool) {
                myPool.clear()
            }
        }

        @JvmStatic
        fun recycle(block: MailBoxPageBlock?) {
            synchronized(myPool) {
                myPool.push(block)
            }
        }

        fun create(): MailBoxPageBlock {
            var block: MailBoxPageBlock? = null
            synchronized(myPool) {
                if (myPool.isNotEmpty()) {
                    block = myPool.pop()
                }
            }
            if (block == null) {
                return MailBoxPageBlock()
            }
            return block
        }
    }
}
