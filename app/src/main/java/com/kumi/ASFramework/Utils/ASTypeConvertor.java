package com.kumi.ASFramework.Utils;

public class ASTypeConvertor {
  public static byte[] getData(char paramChar) {
    return new byte[] { (byte)(paramChar >> 8), (byte)paramChar };
  }
  
  public static byte[] getData(double paramDouble) {
    return getData(Double.doubleToLongBits(paramDouble));
  }
  
  public static byte[] getData(float paramFloat) {
    return getData(Float.floatToIntBits(paramFloat));
  }
  
  public static byte[] getData(int paramInt) {
    return new byte[] { (byte)(paramInt >> 24), (byte)(paramInt >> 16), (byte)(paramInt >> 8), (byte)paramInt };
  }
  
  public static byte[] getData(long paramLong) {
    return new byte[] { (byte)(int)(paramLong >> 56L), (byte)(int)(paramLong >> 48L), (byte)(int)(paramLong >> 40L), (byte)(int)(paramLong >> 32L), (byte)(int)(paramLong >> 24L), (byte)(int)(paramLong >> 16L), (byte)(int)(paramLong >> 8L), (byte)(int)paramLong };
  }
  
  public static byte[] getData(String paramString) {
    // Byte code:
    //   0: aconst_null
    //   1: astore_2
    //   2: aload_0
    //   3: ldc 'unicode'
    //   5: invokevirtual getBytes : (Ljava/lang/String;)[B
    //   8: astore_0
    //   9: aload_0
    //   10: arraylength
    //   11: invokestatic getData : (I)[B
    //   14: astore_3
    //   15: aload_0
    //   16: arraylength
    //   17: iconst_4
    //   18: iadd
    //   19: newarray byte
    //   21: astore_2
    //   22: iconst_0
    //   23: istore_1
    //   24: iload_1
    //   25: iconst_4
    //   26: if_icmpge -> 51
    //   29: aload_2
    //   30: iload_1
    //   31: aload_3
    //   32: iload_1
    //   33: baload
    //   34: bastore
    //   35: iinc #1, 1
    //   38: goto -> 24
    //   41: astore_0
    //   42: aload_0
    //   43: invokevirtual printStackTrace : ()V
    //   46: aload_2
    //   47: astore_0
    //   48: goto -> 9
    //   51: iconst_0
    //   52: istore_1
    //   53: iload_1
    //   54: aload_0
    //   55: arraylength
    //   56: if_icmpge -> 73
    //   59: aload_2
    //   60: iload_1
    //   61: iconst_4
    //   62: iadd
    //   63: aload_0
    //   64: iload_1
    //   65: baload
    //   66: bastore
    //   67: iinc #1, 1
    //   70: goto -> 53
    //   73: aload_2
    //   74: areturn
    // Exception table:
    //   from	to	target	type
    //   2	9	41	java/io/UnsupportedEncodingException
  }
  
  public static byte[] getData(short paramShort) {
    return new byte[] { (byte)(paramShort >> 8), (byte)paramShort };
  }
  
  public static byte[] getData(boolean paramBoolean) {
    if (paramBoolean) {
      byte[] arrayOfByte1 = new byte[1];
      arrayOfByte1[0] = 1;
      return arrayOfByte1;
    } 
    byte[] arrayOfByte = new byte[1];
    arrayOfByte[0] = 0;
    return arrayOfByte;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\Utils\ASTypeConvertor.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */