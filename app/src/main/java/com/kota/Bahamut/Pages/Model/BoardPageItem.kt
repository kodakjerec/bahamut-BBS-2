package com.kota.Bahamut.Pages.Model

import com.kota.Bahamut.ListPage.TelnetListPageItem
import java.util.Stack

class BoardPageItem private constructor() : TelnetListPageItem() {
    var Author: String? = null
    var Date: String? = null
    var GY: Int = 0
    var Size: Int = 0
    var Title: String? = null
    var isMarked: Boolean = false
    var isRead: Boolean = false
    var isReply: Boolean = false

    public override fun clear() {
        super.clear()
        this.Size = 0
        this.isRead = false
        this.isMarked = false
        this.isReply = false
        this.GY = 0
        this.Date = null
        this.Author = null
        this.Title = null
    }

    public override fun set(item: TelnetListPageItem?) {
        super.set(item)
        if (item != null) {
            val article_item = item as BoardPageItem
            this.Size = article_item.Size
            this.Date = article_item.Date
            this.Author = article_item.Author
            this.isRead = article_item.isRead
            this.isMarked = article_item.isMarked
            this.isReply = article_item.isReply
            this.GY = article_item.GY
            this.Title = article_item.Title
        }
    }

    companion object {
        private val _pool = Stack<BoardPageItem?>()
        @JvmStatic
        fun release() {
            synchronized(_pool) {
                _pool.clear()
            }
        }

        fun recycle(item: BoardPageItem?) {
            synchronized(_pool) {
                _pool.push(item)
            }
        }

        fun create(): BoardPageItem {
            var item: BoardPageItem? = null
            synchronized(_pool) {
                if (_pool.size > 0) {
                    item = _pool.pop()
                }
            }
            if (item == null) {
                return BoardPageItem()
            }
            return item
        }
    }
}
