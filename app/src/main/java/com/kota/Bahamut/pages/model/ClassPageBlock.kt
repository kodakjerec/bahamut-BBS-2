package com.kota.Bahamut.pages.model

import com.kota.Bahamut.listPage.TelnetListPageBlock
import java.util.Stack

class ClassPageBlock private constructor() : TelnetListPageBlock() {
    var mode: Int = 0

    companion object {
        private val _pool = Stack<ClassPageBlock?>()
        @JvmStatic
        fun release() {
            synchronized(_pool) {
                _pool.clear()
            }
        }

        @JvmStatic
        fun recycle(block: ClassPageBlock?) {
            synchronized(_pool) {
                _pool.push(block)
            }
        }

        fun create(): ClassPageBlock {
            var block: ClassPageBlock? = null
            synchronized(_pool) {
                if (_pool.size > 0) {
                    block = _pool.pop()
                }
            }
            if (block == null) {
                return ClassPageBlock()
            }
            return block
        }
    }
}
