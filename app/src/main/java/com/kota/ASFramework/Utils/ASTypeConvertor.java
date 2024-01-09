package com.kota.ASFramework.Utils;

import java.io.UnsupportedEncodingException;

public class ASTypeConvertor {
    public static byte[] getData(boolean aBooleanValue) {
        if (aBooleanValue) {
            return new byte[]{1};
        }
        return new byte[]{0};
    }

    public static byte[] getData(short aShortValue) {
        return new byte[]{(byte) (aShortValue >> 8), (byte) aShortValue};
    }

    public static byte[] getData(char aCharValue) {
        return new byte[]{(byte) (aCharValue >> 8), (byte) aCharValue};
    }

    public static byte[] getData(int aIntValue) {
        return new byte[]{(byte) (aIntValue >> 24), (byte) (aIntValue >> 16), (byte) (aIntValue >> 8), (byte) aIntValue};
    }

    public static byte[] getData(long aLongValue) {
        return new byte[]{(byte) ((int) (aLongValue >> 56)), (byte) ((int) (aLongValue >> 48)), (byte) ((int) (aLongValue >> 40)), (byte) ((int) (aLongValue >> 32)), (byte) ((int) (aLongValue >> 24)), (byte) ((int) (aLongValue >> 16)), (byte) ((int) (aLongValue >> 8)), (byte) ((int) aLongValue)};
    }

    public static byte[] getData(float aFloatValue) {
        return getData(Float.floatToIntBits(aFloatValue));
    }

    public static byte[] getData(double aDoubleValue) {
        return getData(Double.doubleToLongBits(aDoubleValue));
    }

    public static byte[] getData(String aString) {
        byte[] string_data = null;
        try {
            string_data = aString.getBytes("unicode");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] size_data = getData(string_data.length);
        byte[] data = new byte[(string_data.length + 4)];
        for (int i = 0; i < 4; i++) {
            data[i] = size_data[i];
        }
        for (int i2 = 0; i2 < string_data.length; i2++) {
            data[i2 + 4] = string_data[i2];
        }
        return data;
    }
}
