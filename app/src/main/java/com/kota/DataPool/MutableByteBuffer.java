package com.kota.DataPool;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

public class MutableByteBuffer implements Iterable<ByteBuffer> {
    public static int BUFFER_SIZE = 128;
    private static Stack<ByteBuffer> _buffer_pool = new Stack<>();
    private static Stack<MutableByteBuffer> _pool = new Stack<>();
    private boolean _is_closed = false;
    private int _size = 0;
    /* access modifiers changed from: private */
    public Vector<ByteBuffer> _writed_buffers = new Vector<>();
    private ByteBuffer _writing_buffer = createByteBuffer();

    public static MutableByteBuffer createMutableByteBuffer() {
        MutableByteBuffer buffer = null;
        synchronized (_pool) {
            if (_pool.size() > 0) {
                buffer = _pool.pop();
            }
        }
        if (buffer == null) {
            return new MutableByteBuffer();
        }
        return buffer;
    }

    public static void recycleMutableByteBuffer(MutableByteBuffer aBuffer) {
        if (aBuffer != null) {
            aBuffer.clear();
            synchronized (_pool) {
                _pool.add(aBuffer);
            }
        }
    }

    public void releasePool() {
        synchronized (_pool) {
            _pool.clear();
        }
    }

    public void recycle() {
        recycleMutableByteBuffer(this);
    }

    private MutableByteBuffer() {
    }

    public MutableByteBuffer put(byte data) {
        if (this._is_closed) {
            Log.e("ERROR","This buffer has been closed.");
            return this;
        }
        if (!this._writing_buffer.hasRemaining()) {
            this._writing_buffer.flip();
            this._writed_buffers.add(this._writing_buffer);
            this._writing_buffer = createByteBuffer();
        }
        this._writing_buffer.put(data);
        this._size++;
        return this;
    }

    public MutableByteBuffer put(byte[] data) {
        for (byte byte_data : data) {
            put(byte_data);
        }
        return this;
    }

    public int size() {
        return this._size;
    }

    public void close() {
        this._writing_buffer.flip();
        this._writed_buffers.add(this._writing_buffer);
        this._writing_buffer = createByteBuffer();
        this._is_closed = true;
    }

    public boolean isClosed() {
        return this._is_closed;
    }

    public void clear() {
        Iterator<ByteBuffer> it = this._writed_buffers.iterator();
        while (it.hasNext()) {
            recycleByteBuffer(it.next());
        }
        this._writed_buffers.clear();
        if (this._writing_buffer != null) {
            this._writing_buffer.clear();
        }
        this._size = 0;
        this._is_closed = false;
    }

    public byte[] toByteArray() {
        if (!this._is_closed) {
            throw new IllegalStateException("This buffer has not been closed.");
        }
        byte[] data = new byte[this._size];
        int position = 0;
        Iterator<ByteBuffer> it = iterator();
        while (it.hasNext()) {
            ByteBuffer buffer = it.next();
            for (int i = 0; i < buffer.limit(); i++) {
                data[position] = buffer.get(i);
                position++;
                if (position == this._size) {
                    break;
                }
            }
        }
        return data;
    }

    public Iterator<ByteBuffer> iterator() {
        return new Iterator<ByteBuffer>() {
            private int _position = 0;

            public boolean hasNext() {
                return this._position < MutableByteBuffer.this._writed_buffers.size();
            }

            public ByteBuffer next() {
                this._position++;
                return (ByteBuffer) MutableByteBuffer.this._writed_buffers.get(this._position - 1);
            }

            public void remove() {
                throw new UnsupportedOperationException("MutableByteBuffer not support this operation.");
            }
        };
    }

    private static ByteBuffer createByteBuffer() {
        ByteBuffer buffer = null;
        synchronized (_buffer_pool) {
            if (_buffer_pool.size() > 0) {
                buffer = _buffer_pool.pop();
            }
        }
        if (buffer == null) {
            return ByteBuffer.allocate(BUFFER_SIZE);
        }
        return buffer;
    }

    private static void recycleByteBuffer(ByteBuffer aBuffer) {
        if (aBuffer != null) {
            aBuffer.clear();
            synchronized (_buffer_pool) {
                _buffer_pool.add(aBuffer);
            }
        }
    }

    public void releaseBufferPool() {
        synchronized (_buffer_pool) {
            _buffer_pool.clear();
        }
    }

    public void print() {
    }
}
