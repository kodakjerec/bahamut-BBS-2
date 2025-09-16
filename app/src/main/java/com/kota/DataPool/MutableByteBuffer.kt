package com.kota.DataPool

import android.util.Log
import java.nio.ByteBuffer
import java.util.Stack
import java.util.Vector

class MutableByteBuffer private constructor() : Iterable<ByteBuffer> {
    var isClosed: Boolean = false
        private set
    private var _size = 0

    /* access modifiers changed from: private */
    var _written_buffers: Vector<ByteBuffer?> = Vector<ByteBuffer?>()
    private var _writing_buffer: ByteBuffer? = createByteBuffer()

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
        if (!this._writing_buffer!!.hasRemaining()) {
            this._writing_buffer!!.flip()
            this._written_buffers.add(this._writing_buffer)
            this._writing_buffer = createByteBuffer()
        }
        this._writing_buffer!!.put(data)
        this._size++
        return this
    }

    fun put(data: ByteArray): MutableByteBuffer {
        for (byte_data in data) {
            put(byte_data)
        }
        return this
    }

    fun size(): Int {
        return this._size
    }

    fun close() {
        this._writing_buffer!!.flip()
        this._written_buffers.add(this._writing_buffer)
        this._writing_buffer = createByteBuffer()
        this.isClosed = true
    }

    fun clear() {
        for (written_buffer in this._written_buffers) {
            recycleByteBuffer(written_buffer)
        }
        this._written_buffers.clear()
        if (this._writing_buffer != null) {
            this._writing_buffer!!.clear()
        }
        this._size = 0
        this.isClosed = false
    }

    fun toByteArray(): ByteArray {
        check(this.isClosed) { "This buffer has not been closed." }
        val data = ByteArray(this._size)
        var position = 0
        try {
            for (buffer in this) {
                for (i in 0..<buffer!!.limit()) {
                    data[position] = buffer.get(i)
                    position++
                    if (position == this._size) {
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
                return this._position < this@MutableByteBuffer._written_buffers.size
            }

            override fun next(): ByteBuffer? {
                this._position++
                return this@MutableByteBuffer._written_buffers.get(this._position - 1)
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
                if (_pool.size > 0) {
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
                if (_buffer_pool.size > 0) {
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
