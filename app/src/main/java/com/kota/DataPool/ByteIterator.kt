package com.kota.DataPool

interface ByteIterator {
    fun hasNext(): Boolean

    fun next(): Int

    fun reset()
}
