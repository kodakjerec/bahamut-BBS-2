package com.kota.Telnet

import java.util.*

class TelnetContainer<ObjectType> {
    private var _container: MutableMap<Int, ObjectType> = TreeMap()
    private var _max_index = 0

    fun add(index: Int, obj: ObjectType) {
        _container[index] = obj
        if (index > _max_index) {
            _max_index = index
        }
    }

    fun get(index: Int): ObjectType? {
        return if (index < 0 || index > _max_index || !_container.containsKey(index)) {
            null
        } else {
            _container[index]
        }
    }

    fun size(): Int = _max_index + 1

    fun clear() {
        _max_index = 0
        _container.clear()
    }
}
