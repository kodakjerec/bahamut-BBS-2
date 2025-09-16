package com.kota.Telnet

import java.util.TreeMap

class TelnetContainer<ObjectType> {
    var _container: MutableMap<Int?, ObjectType?>? = TreeMap<Int?, ObjectType?>()
    var _max_index: Int = 0

    fun add(index: Int, `object`: ObjectType?) {
        if (this._container != null) {
            this._container!!.put(index, `object`)
            if (index > this._max_index) {
                this._max_index = index
            }
        }
    }

    fun get(index: Int): ObjectType? {
        if (index < 0 || index > this._max_index || this._container == null || !this._container!!.containsKey(
                index
            )
        ) {
            return null
        }
        return this._container!!.get(index)
    }

    fun size(): Int {
        if (this._container != null) {
            return this._max_index + 1
        }
        return 0
    }

    fun clear() {
        this._max_index = 0
        this._container!!.clear()
    }
}
