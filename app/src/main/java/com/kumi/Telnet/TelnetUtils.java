package com.kumi.Telnet;

import com.kumi.Telnet.Model.TelnetRow;

public class TelnetUtils {
  public static String getHeader(String paramString) {
    String str1 = "";
    String str2 = paramString.replace(" ", "");
    paramString = str1;
    if (str2.length() > 1)
      paramString = str2.substring(0, 2); 
    return paramString;
  }
  
  public static int getIntegerFromData(TelnetRow paramTelnetRow, int paramInt1, int paramInt2) {
    boolean bool = false;
    try {
      String str = paramTelnetRow.getSpaceString(paramInt1, paramInt2).trim();
      paramInt1 = bool;
      if (str.length() > 0)
        paramInt1 = Integer.parseInt(str); 
    } catch (Exception exception) {
      exception.printStackTrace();
      paramInt1 = bool;
    } 
    return paramInt1;
  }
  
  public static int hashCode(byte[] paramArrayOfbyte) {
    int k = 0;
    int i = 1;
    for (int j = paramArrayOfbyte.length - 1; j >= 0; j--) {
      k += (paramArrayOfbyte[j] & 0xFF) * i;
      i = (i << 5) - i;
    } 
    return k;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\TelnetUtils.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */