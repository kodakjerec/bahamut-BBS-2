package com.kota.Bahamut.pages.model

import com.kota.Bahamut.listPage.TelnetListPageItem
import java.util.Stack

class ClassPageItem private constructor() : TelnetListPageItem() {
    @JvmField
    var manager: String = "" // 版主群
    var mode: Int = 0 // 0: 看板分類, 1: 看板列表
    @JvmField
    var name: String = "" // 看板英文名稱
    @JvmField
    var title: String = "" // 看板中文名稱
    @JvmField
    var isDirectory: Boolean = false // 是否為目錄

    override fun clear() {
        super.clear()
        this.manager = ""
        this.name = ""
        this.title = ""
        this.isDirectory = false
        this.mode = 0
    }

    companion object {
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
