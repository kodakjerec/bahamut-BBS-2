package com.kumi.TextEncoder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class U2BEncoder {
  public static final int CHAR_MAXIMUM = 65535;
  
  public static final int NULL_CHAR = 65533;
  
  private static U2BEncoder _instance = null;
  
  private int _offset = 0;
  
  private char[] _table = null;
  
  private int _table_size = 0;
  
  private U2BEncoder(InputStream paramInputStream) {
    readTableFromInputStream(paramInputStream);
  }
  
  public static void constructInstance(InputStream paramInputStream) {
    _instance = new U2BEncoder(paramInputStream);
  }
  
  public static U2BEncoder getInstance() {
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
      System.out.println("read U2B encode data success"); 
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
  
  public byte[] encodeToBytes(byte[] paramArrayOfbyte, int paramInt) {
    ByteBuffer byteBuffer = ByteBuffer.allocate(paramArrayOfbyte.length);
    byteBuffer.clear();
    while (paramInt < paramArrayOfbyte.length) {
      byte b = paramArrayOfbyte[paramInt];
      char c = encodeChar((char)(((paramArrayOfbyte[paramInt + 1] & 0xFF) << 8) + (b & 0xFF)));
      int i = c >> 8 & 0xFF;
      if (i > 0)
        byteBuffer.put((byte)i); 
      byteBuffer.put((byte)(c & 0xFF));
      paramInt += 2;
    } 
    byteBuffer.flip();
    paramArrayOfbyte = new byte[byteBuffer.limit()];
    for (paramInt = 0; paramInt < paramArrayOfbyte.length; paramInt++)
      paramArrayOfbyte[paramInt] = byteBuffer.get(); 
    return paramArrayOfbyte;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\TextEncoder\U2BEncoder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */