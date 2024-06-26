package com.kota.ASFramework.Utils;

import java.io.IOException;
import java.io.OutputStream;

public class ASStreamWriter {
  public static void writeData(OutputStream paramOutputStream, byte paramByte) throws IOException {
    paramOutputStream.write(paramByte);
  }
  
  public static void writeData(OutputStream paramOutputStream, char paramChar) throws IOException {
    paramOutputStream.write(paramChar >> 8 & 255);
    paramOutputStream.write(paramChar & 255);
  }
  
  public static void writeData(OutputStream paramOutputStream, double paramDouble) throws IOException {
    writeData(paramOutputStream, Double.doubleToLongBits(paramDouble));
  }
  
  public static void writeData(OutputStream paramOutputStream, float paramFloat) throws IOException {
    writeData(paramOutputStream, Float.floatToIntBits(paramFloat));
  }
  
  public static void writeData(OutputStream paramOutputStream, int paramInt) throws IOException {
    paramOutputStream.write(paramInt >> 24 & 255);
    paramOutputStream.write(paramInt >> 16 & 255);
    paramOutputStream.write(paramInt >> 8 & 255);
    paramOutputStream.write(paramInt & 255);
  }
  
  public static void writeData(OutputStream paramOutputStream, long paramLong) throws IOException {
    paramOutputStream.write((int)(paramLong >> 56L) & 255);
    paramOutputStream.write((int)(paramLong >> 48L) & 255);
    paramOutputStream.write((int)(paramLong >> 40L) & 255);
    paramOutputStream.write((int)(paramLong >> 32L) & 255);
    paramOutputStream.write((int)(paramLong >> 24L) & 255);
    paramOutputStream.write((int)(paramLong >> 16L) & 255);
    paramOutputStream.write((int)(paramLong >> 8L) & 255);
    paramOutputStream.write((int)paramLong & 255);
  }
  
  public static void writeData(OutputStream paramOutputStream, String paramString) throws IOException {
    writeData(paramOutputStream, paramString.getBytes("unicode"));
  }
  
  public static void writeData(OutputStream paramOutputStream, short paramShort) throws IOException {
    paramOutputStream.write(paramShort >> 8 & 255);
    paramOutputStream.write(paramShort & 255);
  }
  
  public static void writeData(OutputStream paramOutputStream, boolean paramBoolean) throws IOException {
    if (paramBoolean) {
      paramOutputStream.write(1);
      return;
    } 
    paramOutputStream.write(0);
  }
  
  public static void writeData(OutputStream paramOutputStream, byte[] paramArrayOfbyte) throws IOException {
    writeData(paramOutputStream, paramArrayOfbyte.length);
    paramOutputStream.write(paramArrayOfbyte);
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\Utils\ASStreamWriter.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */