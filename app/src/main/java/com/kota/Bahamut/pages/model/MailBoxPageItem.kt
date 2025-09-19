package com.kota.Bahamut.pages.model

import com.kota.Bahamut.listPage.TelnetListPageItem
import java.util.Stack

class MailBoxPageItem private constructor() : TelnetListPageItem() {
    @JvmField
    var author: String? = null
    @JvmField
    var date: String? = null
    var size: Int = 0
    @JvmField
    var title: String? = null
    @JvmField
    var isMarked: Boolean = false
    var isOrigin: Boolean = false
    @JvmField
    var isRead: Boolean = false
    @JvmField
    var isReply: Boolean = false

    override fun clear() {
        super.clear()
        this.size = 0
        this.isRead = false
        this.isReply = false
        this.date = null
        this.author = null
        this.title = null
    }

    override fun set(aData: TelnetListPageItem?) {
        super.set(aData)
        if (aData != null) {
            val mailItem = aData as MailBoxPageItem
            this.size = mailItem.size
            this.date = mailItem.date
            this.author = mailItem.author
            this.isRead = mailItem.isRead
            this.isReply = mailItem.isReply
            this.isMarked = mailItem.isMarked
            this.title = mailItem.title
        }
    }

    companion object {
        private const val COUNT = 0
        private val _pool = Stack<MailBoxPageItem?>()
        @JvmStatic
        fun release() {
            synchronized(_pool) {
                _pool.clear()
            }
        }

        @JvmStatic
        fun recycle(item: MailBoxPageItem?) {
            synchronized(_pool) {
                _pool.push(item)
            }
        }

        fun create(): MailBoxPageItem {
            var item: MailBoxPageItem? = null
            synchronized(_pool) {
                if (_pool.isNotEmpty()) {
                    item = _pool.pop()
                }
            }
            if (item == null) {
                return MailBoxPageItem()
            }
            return item
        }
    }
}
