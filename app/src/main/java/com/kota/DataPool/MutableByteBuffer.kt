package com.kota.DataPool

import android.util.Log
import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.ArrayList

class MutableByteBuffer private constructor() : Iterable<ByteBuffer> {
    
    companion object {
        var BUFFER_SIZE = 128
        private val bufferPool: Stack<ByteBuffer> = Stack()
        private val pool: Stack<MutableByteBuffer> = Stack()
        
        fun createMutableByteBuffer(): MutableByteBuffer {
            var buffer: MutableByteBuffer? = null
            synchronized(pool) {
                if (pool.isNotEmpty()) {
                    buffer = pool.pop()
                }
            }
            return buffer ?: MutableByteBuffer()
        }
        
        fun recycleMutableByteBuffer(buffer: MutableByteBuffer?) {
            buffer?.let {
                it.clear()
                synchronized(pool) {
                    pool.add(it)
                }
            }
        }
        
        private fun createByteBuffer(): ByteBuffer {
            var buffer: ByteBuffer? = null
            synchronized(bufferPool) {
                if (bufferPool.isNotEmpty()) {
                    buffer = bufferPool.pop()
                }
            }
            return buffer ?: ByteBuffer.allocate(BUFFER_SIZE)
        }
        
        private fun recycleByteBuffer(buffer: ByteBuffer?) {
            buffer?.let {
                it.clear()
                synchronized(bufferPool) {
                    bufferPool.add(it)
                }
            }
        }
    }
    
    private var isClosed = false
    private var size = 0
    private var writtenBuffers: Vector<ByteBuffer> = Vector()
    private var writingBuffer: ByteBuffer = createByteBuffer()
    
    fun releasePool() {
        synchronized(pool) {
            pool.clear()
        }
    }
    
    fun recycle() {
        recycleMutableByteBuffer(this)
    }
    
    fun put(data: Byte): MutableByteBuffer {
        if (isClosed) {
            Log.e("ERROR", "This buffer has been closed.")
            return this
        }
        
        if (!writingBuffer.hasRemaining()) {
            writingBuffer.flip()
            writtenBuffers.add(writingBuffer)
            writingBuffer = createByteBuffer()
        }
        
        writingBuffer.put(data)
        size++
        return this
    }
    
    fun put(data: ByteArray): MutableByteBuffer {
        for (byteData in data) {
            put(byteData)
        }
        return this
    }
    
    fun size(): Int = size
    
    fun close() {
        writingBuffer.flip()
        writtenBuffers.add(writingBuffer)
        writingBuffer = createByteBuffer()
        isClosed = true
    }
    
    fun isClosed(): Boolean = isClosed
    
    fun clear() {
        for (writtenBuffer in writtenBuffers) {
            recycleByteBuffer(writtenBuffer)
        }
        writtenBuffers.clear()
        
        writingBuffer.clear()
        size = 0
        isClosed = false
    }
    
    fun toByteArray(): ByteArray {
        if (!isClosed) {
            throw IllegalStateException("This buffer has not been closed.")
        }
        
        val data = ByteArray(size)
        var position = 0
        
        try {
            for (buffer in this) {
                for (i in 0 until buffer.limit()) {
                    data[position] = buffer.get(i)
                    position++
                    if (position == size) {
                        break
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return data
    }
    
    override fun iterator(): Iterator<ByteBuffer> {
        return object : Iterator<ByteBuffer> {
            private var position = 0
            
            override fun hasNext(): Boolean {
                return position < writtenBuffers.size
            }
            
            override fun next(): ByteBuffer {
                position++
                return writtenBuffers[position - 1]
            }
        }
    }
    
    fun releaseBufferPool() {
        synchronized(bufferPool) {
            bufferPool.clear()
        }
    }
    
    fun print() {
        // 保留空方法以保持向後相容性
    }
}
