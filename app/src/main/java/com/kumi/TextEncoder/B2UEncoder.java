package com.kumi.TextEncoder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Vector;

public class B2UEncoder {
  private static final int BUFFER_SIZE = 1024;
  
  public static final int CHAR_MAXIMUM = 65535;
  
  public static final int NULL_CHAR = 65533;
  
  private static B2UEncoder _instance = null;
  
  private TextConverterBuffer _buffer = null;
  
  private int _offset = 0;
  
  private char[] _table = null;
  
  private int _table_size = 0;
  
  private B2UEncoder(InputStream paramInputStream) {
    readTableFromInputStream(paramInputStream);
  }
  
  public static void constructInstance(InputStream paramInputStream) {
    _instance = new B2UEncoder(paramInputStream);
  }
  
  private byte[] encodeToBytes(byte[] paramArrayOfbyte) {
    Vector<ByteBuffer> vector = new Vector();
    vector.add(getBuffer().createByteBuffer());
    ((ByteBuffer)vector.firstElement()).put((byte)-2);
    ((ByteBuffer)vector.firstElement()).put((byte)-1);
    int i = 0;
    while (true) {
      if (i < paramArrayOfbyte.length) {
        int j = paramArrayOfbyte[i] & 0xFF;
        if (j != 0) {
          int m;
          int k = j;
          if (j > 127 && i < paramArrayOfbyte.length - 1) {
            m = i + 1;
            k = encodeChar((char)((j << 8) + (paramArrayOfbyte[m] & 0xFF)));
            i = k >> 8 & 0xFF;
            k &= 0xFF;
          } else {
            j = 0;
            m = i;
            i = j;
          } 
          if (!((ByteBuffer)vector.lastElement()).hasRemaining()) {
            ((ByteBuffer)vector.lastElement()).flip();
            vector.add(getBuffer().createByteBuffer());
          } 
          ((ByteBuffer)vector.lastElement()).put((byte)i);
          if (!((ByteBuffer)vector.lastElement()).hasRemaining()) {
            ((ByteBuffer)vector.lastElement()).flip();
            vector.add(getBuffer().createByteBuffer());
          } 
          ((ByteBuffer)vector.lastElement()).put((byte)k);
          i = m + 1;
          continue;
        } 
      } 
      ((ByteBuffer)vector.lastElement()).flip();
      byte b = 0;
      for (i = 0; i < vector.size() - 1; i++)
        b += true; 
      paramArrayOfbyte = new byte[b + ((ByteBuffer)vector.lastElement()).limit()];
      i = 0;
      label33: for (ByteBuffer byteBuffer : vector) {
        b = 0;
        int j = i;
        while (true) {
          i = j;
          if (b < byteBuffer.limit()) {
            paramArrayOfbyte[j] = byteBuffer.get(b);
            j++;
            b++;
            continue;
          } 
          continue label33;
        } 
      } 
      return paramArrayOfbyte;
    } 
  }
  
  private TextConverterBuffer getBuffer() {
    if (this._buffer == null)
      this._buffer = new TextConverterBuffer() {
          final B2UEncoder this$0;
          
          public ByteBuffer createByteBuffer() {
            return ByteBuffer.allocate(1024);
          }
          
          public void recycleByteBuffer(ByteBuffer param1ByteBuffer) {}
        }; 
    return this._buffer;
  }
  
  public static B2UEncoder getInstance() {
    return _instance;
  }
  
  private char readCharFromStream(InputStream paramInputStream) throws IOException {
    return (char)((paramInputStream.read() << 8) + paramInputStream.read());
  }
  
  private void readTableFromInputStream(InputStream paramInputStream) {
    boolean bool1;
    boolean bool2 = false;
    try {
      char c = readCharFromStream(paramInputStream);
      this._offset = readCharFromStream(paramInputStream);
      this._table_size = readCharFromStream(paramInputStream);
      this._table = new char[this._table_size];
      for (bool1 = false; bool1 < this._table.length; bool1++)
        this._table[bool1] = '�'; 
      for (bool1 = false; bool1 < c; bool1++) {
        char c2 = readCharFromStream(paramInputStream);
        char c1 = readCharFromStream(paramInputStream);
        this._table[c2 - this._offset] = c1;
      } 
      paramInputStream.close();
      bool1 = true;
    } catch (Exception exception) {
      exception.printStackTrace();
      bool1 = bool2;
    } 
    if (bool1)
      System.out.println("read B2U encode data success"); 
  }
  
  public static void releaseInstance() {
    _instance = null;
  }
  
  public char encodeChar(char paramChar) {
    int i = paramChar - this._offset;
    null = paramChar;
    if (i >= 0) {
      if (i >= this._table_size)
        return paramChar; 
    } else {
      return null;
    } 
    return this._table[i];
  }
  
  public String encodeToString(byte[] paramArrayOfbyte) {
    String str1;
    String str2 = "";
    try {
      byte[] arrayOfByte = encodeToBytes(paramArrayOfbyte);
      str1 = new String();
      this(arrayOfByte, "unicode");
    } catch (Exception exception) {
      exception.printStackTrace();
      str1 = str2;
    } 
    return str1;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\TextEncoder\B2UEncoder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */