package com.kota.dataPool

import android.util.Log
import java.nio.ByteBuffer
import java.util.Stack
import java.util.Vector

class MutableByteBuffer private constructor() : Iterable<ByteBuffer?> {
    var isClosed: Boolean = false
        private set
    private var bufferSize = 0

    var writtenBuffers: Vector<ByteBuffer?> = Vector<ByteBuffer?>()
    private var writingBuffer: ByteBuffer? = createByteBuffer()

    fun releasePool() {
        synchronized(_pool) {
            _pool.clear()
        }
    }

    fun recycle() {
        recycleMutableByteBuffer(this)
    }

    fun put(data: Byte): MutableByteBuffer {
        if (this.isClosed) {
            Log.e("ERROR", "This buffer has been closed.")
            return this
        }
        if (!this.writingBuffer!!.hasRemaining()) {
            this.writingBuffer!!.flip()
            this.writtenBuffers.add(this.writingBuffer)
            this.writingBuffer = createByteBuffer()
        }
        this.writingBuffer?.put(data)
        this.bufferSize++
        return this
    }

    fun put(data: ByteArray): MutableByteBuffer {
        for (byteData in data) {
            put(byteData)
        }
        return this
    }

    fun size(): Int {
        return this.bufferSize
    }

    fun close() {
        this.writingBuffer?.flip()
        this.writtenBuffers.add(this.writingBuffer)
        this.writingBuffer = createByteBuffer()
        this.isClosed = true
    }

    fun clear() {
        for (writtenBuffer in this.writtenBuffers) {
            recycleByteBuffer(writtenBuffer)
        }
        this.writtenBuffers.clear()
        if (this.writingBuffer != null) {
            this.writingBuffer?.clear()
        }
        this.bufferSize = 0
        this.isClosed = false
    }

    fun toByteArray(): ByteArray {
        check(this.isClosed) { "This buffer has not been closed." }
        val data = ByteArray(this.bufferSize)
        var position = 0
        try {
            for (buffer in this) {
                for (i in 0..<buffer!!.limit()) {
                    data[position] = buffer.get(i)
                    position++
                    if (position == this.bufferSize) {
                        break
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return data
    }

    override fun iterator(): MutableIterator<ByteBuffer?> {
        return object : MutableIterator<ByteBuffer?> {
            private var _position = 0

            override fun hasNext(): Boolean {
                return this._position < this@MutableByteBuffer.writtenBuffers.size
            }

            override fun next(): ByteBuffer? {
                this._position++
                return this@MutableByteBuffer.writtenBuffers[this._position - 1]
            }

            override fun remove() {
                throw UnsupportedOperationException("MutableByteBuffer not support this operation.")
            }
        }
    }

    fun releaseBufferPool() {
        synchronized(_buffer_pool) {
            _buffer_pool.clear()
        }
    }

    fun print() {
    }

    companion object {
        var BUFFER_SIZE: Int = 128
        private val _buffer_pool = Stack<ByteBuffer?>()
        private val _pool = Stack<MutableByteBuffer?>()
        fun createMutableByteBuffer(): MutableByteBuffer {
            var buffer: MutableByteBuffer? = null
            synchronized(_pool) {
                if (_pool.isNotEmpty()) {
                    buffer = _pool.pop()
                }
            }
            if (buffer == null) {
                return MutableByteBuffer()
            }
            return buffer
        }

        fun recycleMutableByteBuffer(aBuffer: MutableByteBuffer?) {
            if (aBuffer != null) {
                aBuffer.clear()
                synchronized(_pool) {
                    _pool.add(aBuffer)
                }
            }
        }

        private fun createByteBuffer(): ByteBuffer {
            var buffer: ByteBuffer? = null
            synchronized(_buffer_pool) {
                if (_buffer_pool.isNotEmpty()) {
                    buffer = _buffer_pool.pop()
                }
            }
            if (buffer == null) {
                return ByteBuffer.allocate(BUFFER_SIZE)
            }
            return buffer
        }

        private fun recycleByteBuffer(aBuffer: ByteBuffer?) {
            if (aBuffer != null) {
                aBuffer.clear()
                synchronized(_buffer_pool) {
                    _buffer_pool.add(aBuffer)
                }
            }
        }
    }
}
