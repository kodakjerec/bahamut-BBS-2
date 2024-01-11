package com.kumi.DataPool;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

public class MutableByteBuffer implements Iterable<ByteBuffer> {
  public static int BUFFER_SIZE = 128;
  
  private static Stack<ByteBuffer> _buffer_pool;
  
  private static Stack<MutableByteBuffer> _pool = new Stack<MutableByteBuffer>();
  
  private boolean _is_closed = false;
  
  private int _size = 0;
  
  private Vector<ByteBuffer> _writed_buffers = new Vector<ByteBuffer>();
  
  private ByteBuffer _writing_buffer = createByteBuffer();
  
  static {
    _buffer_pool = new Stack<ByteBuffer>();
  }
  
  private static ByteBuffer createByteBuffer() {
    Stack<ByteBuffer> stack;
    ByteBuffer byteBuffer;
    null = null;
    synchronized (_buffer_pool) {
      if (_buffer_pool.size() > 0)
        null = _buffer_pool.pop(); 
      byteBuffer = null;
      if (null == null)
        byteBuffer = ByteBuffer.allocate(BUFFER_SIZE); 
      return byteBuffer;
    } 
  }
  
  public static MutableByteBuffer createMutableByteBuffer() {
    Stack<MutableByteBuffer> stack;
    MutableByteBuffer mutableByteBuffer;
    null = null;
    synchronized (_pool) {
      if (_pool.size() > 0)
        null = _pool.pop(); 
      mutableByteBuffer = null;
      if (null == null)
        mutableByteBuffer = new MutableByteBuffer(); 
      return mutableByteBuffer;
    } 
  }
  
  private static void recycleByteBuffer(ByteBuffer paramByteBuffer) {
    if (paramByteBuffer != null) {
      paramByteBuffer.clear();
      synchronized (_buffer_pool) {
        _buffer_pool.add(paramByteBuffer);
        return;
      } 
    } 
  }
  
  public static void recycleMutableByteBuffer(MutableByteBuffer paramMutableByteBuffer) {
    if (paramMutableByteBuffer != null) {
      paramMutableByteBuffer.clear();
      synchronized (_pool) {
        _pool.add(paramMutableByteBuffer);
        return;
      } 
    } 
  }
  
  public void clear() {
    Iterator<ByteBuffer> iterator = this._writed_buffers.iterator();
    while (iterator.hasNext())
      recycleByteBuffer(iterator.next()); 
    this._writed_buffers.clear();
    if (this._writing_buffer != null)
      this._writing_buffer.clear(); 
    this._size = 0;
    this._is_closed = false;
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
  
  public Iterator<ByteBuffer> iterator() {
    return new Iterator<ByteBuffer>() {
        private int _position = 0;
        
        final MutableByteBuffer this$0;
        
        public boolean hasNext() {
          return (this._position < MutableByteBuffer.this._writed_buffers.size());
        }
        
        public ByteBuffer next() {
          this._position++;
          return MutableByteBuffer.this._writed_buffers.get(this._position - 1);
        }
        
        public void remove() {
          throw new UnsupportedOperationException("MutableByteBuffer not support this operation.");
        }
      };
  }
  
  public void print() {}
  
  public MutableByteBuffer put(byte paramByte) {
    if (this._is_closed)
      throw new IllegalStateException("This buffer has been closed."); 
    if (!this._writing_buffer.hasRemaining()) {
      this._writing_buffer.flip();
      this._writed_buffers.add(this._writing_buffer);
      this._writing_buffer = createByteBuffer();
    } 
    this._writing_buffer.put(paramByte);
    this._size++;
    return this;
  }
  
  public MutableByteBuffer put(byte[] paramArrayOfbyte) {
    int i = paramArrayOfbyte.length;
    for (byte b = 0; b < i; b++)
      put(paramArrayOfbyte[b]); 
    return this;
  }
  
  public void recycle() {
    recycleMutableByteBuffer(this);
  }
  
  public void releaseBufferPool() {
    synchronized (_buffer_pool) {
      _buffer_pool.clear();
      return;
    } 
  }
  
  public void releasePool() {
    synchronized (_pool) {
      _pool.clear();
      return;
    } 
  }
  
  public int size() {
    return this._size;
  }
  
  public byte[] toByteArray() {
    if (!this._is_closed)
      throw new IllegalStateException("This buffer has not been closed."); 
    byte[] arrayOfByte = new byte[this._size];
    byte b = 0;
    label16: for (ByteBuffer byteBuffer : this) {
      byte b2 = 0;
      byte b1 = b;
      while (true) {
        b = b1;
        if (b2 < byteBuffer.limit()) {
          arrayOfByte[b1] = byteBuffer.get(b2);
          b = ++b1;
          if (b1 != this._size) {
            b2++;
            continue;
          } 
          continue label16;
        } 
        continue label16;
      } 
    } 
    return arrayOfByte;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\DataPool\MutableByteBuffer.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */