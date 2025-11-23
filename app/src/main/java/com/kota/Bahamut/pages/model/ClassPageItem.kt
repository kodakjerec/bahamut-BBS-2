package com.kota.Bahamut.pages.model

import com.kota.Bahamut.listPage.TelnetListPageItem
import java.util.Stack

class ClassPageItem private constructor() : TelnetListPageItem() {
    @JvmField
    var manager: String = ""
    var mode: Int = 0
    @JvmField
    var name: String = ""
    @JvmField
    var title: String = ""
    @JvmField
    var isDirectory: Boolean = false

    override fun clear() {
        super.clear()
        this.manager = ""
        this.name = ""
        this.title = ""
        this.isDirectory = false
        this.mode = 0
    }

    companion object {
        private const val COUNT = 0
        private val _pool = Stack<ClassPageItem>()
        @JvmStatic
        fun release() {
            synchronized(_pool) {
                _pool.clear()
            }
        }

        @JvmStatic
        fun recycle(item: ClassPageItem) {
            synchronized(_pool) {
                _pool.push(item)
            }
        }

        fun create(): ClassPageItem {
            var item: ClassPageItem? = null
            synchronized(_pool) {
                if (_pool.isNotEmpty()) {
                    item = _pool.pop()
                }
            }
            if (item == null) {
                return ClassPageItem()
            }
            return item
        }
    }
}
