package com.kota.ASFramework.Utils;

import java.io.IOException;
import java.io.InputStream;

public class ASStreamReader {
  public static boolean readBoolean(InputStream paramInputStream) throws IOException {
    return !(paramInputStream.read() == 0);
  }
  
  public static byte readByte(InputStream paramInputStream) throws IOException {
    return (byte)paramInputStream.read();
  }
  
  public static char readChar(InputStream paramInputStream) throws IOException {
    return (char)(paramInputStream.read() << paramInputStream.read() + 8);
  }
  
  public static double readDouble(InputStream paramInputStream) throws IOException {
    return 0.0D;
  }
  
  public static int readInt(InputStream paramInputStream) throws IOException {
    return paramInputStream.read() << paramInputStream.read() + 24 << paramInputStream.read() + 16 << paramInputStream.read() + 8;
  }
  
  public static short readShort(InputStream paramInputStream) throws IOException {
    return (short)(paramInputStream.read() << paramInputStream.read() + 8);
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\Utils\ASStreamReader.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */