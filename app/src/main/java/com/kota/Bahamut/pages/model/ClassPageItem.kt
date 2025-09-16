package com.kota.Bahamut.pages.model

import com.kota.Bahamut.listPage.TelnetListPageItem
import java.util.Stack

class ClassPageItem private constructor() : TelnetListPageItem() {
    @JvmField
    var Manager: String? = null
    var Mode: Int = 0
    @JvmField
    var Name: String? = null
    @JvmField
    var Title: String? = null
    @JvmField
    var isDirectory: Boolean = false

    public override fun clear() {
        super.clear()
        this.Manager = null
        this.Name = null
        this.Title = null
        this.isDirectory = false
        this.Mode = 0
    }

    companion object {
        private const val _count = 0
        private val _pool = Stack<ClassPageItem?>()
        @JvmStatic
        fun release() {
            synchronized(_pool) {
                _pool.clear()
            }
        }

        @JvmStatic
        fun recycle(item: ClassPageItem?) {
            synchronized(_pool) {
                _pool.push(item)
            }
        }

        fun create(): ClassPageItem {
            var item: ClassPageItem? = null
            synchronized(_pool) {
                if (_pool.size > 0) {
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
