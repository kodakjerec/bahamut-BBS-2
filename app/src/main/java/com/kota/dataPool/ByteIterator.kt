package com.kota.dataPool

interface ByteIterator {
    fun hasNext(): Boolean

    fun next(): Int

    fun reset()
}
