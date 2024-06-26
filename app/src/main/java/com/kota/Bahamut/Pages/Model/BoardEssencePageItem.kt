package com.kota.Bahamut.Pages.Model

import com.kota.Bahamut.ListPage.TelnetListPageItem
import java.util.Stack

class BoardEssencePageItem private constructor() : TelnetListPageItem() {
    @JvmField
    var author: String? = null
    @JvmField
    var date: String? = null
    var size = 0
    @JvmField
    var title: String? = null
    @JvmField
    var isBBSClickable = false
    @JvmField
    var isDirectory = false
    override fun clear() {
        super.clear()
        size = 0
        isBBSClickable = false
        isDirectory = false
        date = null
        author = null
        title = null
    }

    override fun set(item: TelnetListPageItem) {
        super.set(item)
        val articleItem = item as BoardEssencePageItem
        size = articleItem.size
        date = articleItem.date
        author = articleItem.author
        isBBSClickable = articleItem.isBBSClickable
        isDirectory = articleItem.isDirectory
        title = articleItem.title
    }

    companion object {
        private val _pool = Stack<BoardEssencePageItem>()
        fun release() {
            synchronized(_pool) { _pool.clear() }
        }

        fun recycle(item: BoardEssencePageItem) {
            synchronized(_pool) { _pool.push(item) }
        }

        fun create(): BoardEssencePageItem {
            var item: BoardEssencePageItem? = null
            synchronized(_pool) {
                if (_pool.size > 0) {
                    item = _pool.pop()
                }
            }
            if (item == null)
                item = BoardEssencePageItem()

            return item as BoardEssencePageItem
        }
    }
}
