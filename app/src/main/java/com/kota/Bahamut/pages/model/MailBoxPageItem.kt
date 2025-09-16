package com.kota.Bahamut.pages.model

import com.kota.Bahamut.listPage.TelnetListPageItem
import java.util.Stack

class MailBoxPageItem private constructor() : TelnetListPageItem() {
    @JvmField
    var Author: String? = null
    @JvmField
    var Date: String? = null
    var Size: Int = 0
    @JvmField
    var Title: String? = null
    @JvmField
    var isMarked: Boolean = false
    var isOrigin: Boolean = false
    @JvmField
    var isRead: Boolean = false
    @JvmField
    var isReply: Boolean = false

    public override fun clear() {
        super.clear()
        this.Size = 0
        this.isRead = false
        this.isReply = false
        this.Date = null
        this.Author = null
        this.Title = null
    }

    public override fun set(item: TelnetListPageItem?) {
        super.set(item)
        if (item != null) {
            val mail_item = item as MailBoxPageItem
            this.Size = mail_item.Size
            this.Date = mail_item.Date
            this.Author = mail_item.Author
            this.isRead = mail_item.isRead
            this.isReply = mail_item.isReply
            this.isMarked = mail_item.isMarked
            this.Title = mail_item.Title
        }
    }

    companion object {
        private const val _count = 0
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
                if (_pool.size > 0) {
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
