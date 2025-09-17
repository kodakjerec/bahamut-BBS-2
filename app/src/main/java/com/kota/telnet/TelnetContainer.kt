package com.kota.telnet

import java.util.TreeMap

class TelnetContainer<ObjectType> {
    var objectContainer: MutableMap<Int?, ObjectType?>? = TreeMap<Int?, ObjectType?>()
    var maxIndex: Int = 0

    fun add(index: Int, `object`: ObjectType?) {
        if (this.objectContainer != null) {
            this.objectContainer!!.put(index, `object`)
            if (index > this.maxIndex) {
                this.maxIndex = index
            }
        }
    }

    fun get(index: Int): ObjectType? {
        if (index < 0 || index > this.maxIndex || this.objectContainer == null || !this.objectContainer!!.containsKey(
                index
            )
        ) {
            return null
        }
        return this.objectContainer!![index]
    }

    fun size(): Int {
        if (this.objectContainer != null) {
            return this.maxIndex + 1
        }
        return 0
    }

    fun clear() {
        this.maxIndex = 0
        this.objectContainer!!.clear()
    }
}
