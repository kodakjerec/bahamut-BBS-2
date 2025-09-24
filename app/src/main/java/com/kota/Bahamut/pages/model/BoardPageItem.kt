package com.kota.Bahamut.pages.model

import com.kota.Bahamut.listPage.TelnetListPageItem
import java.util.Stack

class BoardPageItem private constructor() : TelnetListPageItem() {
    var author: String? = null
    var date: String? = null
    var gy: Int = 0
    var size: Int = 0
    var title: String? = null
    var isMarked: Boolean = false
    var isRead: Boolean = false
    var isReply: Boolean = false

    override fun clear() {
        super.clear()
        this.size = 0
        this.isRead = false
        this.isMarked = false
        this.isReply = false
        this.gy = 0
        this.date = null
        this.author = null
        this.title = null
    }

    override fun set(aData: TelnetListPageItem) {
        super.set(aData)
        val articleItem = aData as BoardPageItem
        this.size = articleItem.size
        this.date = articleItem.date
        this.author = articleItem.author
        this.isRead = articleItem.isRead
        this.isMarked = articleItem.isMarked
        this.isReply = articleItem.isReply
        this.gy = articleItem.gy
        this.title = articleItem.title
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
                if (_pool.isNotEmpty()) {
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
