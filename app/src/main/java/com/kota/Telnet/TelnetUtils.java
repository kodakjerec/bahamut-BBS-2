package com.kota.Telnet;

import com.kota.Telnet.Model.TelnetRow;

public class TelnetUtils {
    public static int hashCode(byte[] aData) {
        int hash = 0;
        int multiplier = 1;
        for (int i = aData.length - 1; i >= 0; i--) {
            hash += (aData[i] & 255) * multiplier;
            multiplier = (multiplier << 5) - multiplier;
        }
        return hash;
    }

    public static int getIntegerFromData(TelnetRow aRow, int from, int to) {
        try {
            String temp = aRow.getSpaceString(from, to).trim();
            if (temp.length() > 0) {
                return Integer.parseInt(temp);
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String getHeader(String source) {
        String trim_source = source.replace(" ", "");
        if (trim_source.length() > 1) {
            return trim_source.substring(0, 2);
        }
        return "";
    }
}
