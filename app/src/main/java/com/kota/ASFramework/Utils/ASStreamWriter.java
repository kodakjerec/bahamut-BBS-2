package com.kota.ASFramework.Utils;

import java.io.IOException;
import java.io.OutputStream;

public class ASStreamWriter {
    public static void writeData(OutputStream aOutputStream, boolean aBooleanValue) throws IOException {
        if (aBooleanValue) {
            aOutputStream.write(1);
        } else {
            aOutputStream.write(0);
        }
    }

    public static void writeData(OutputStream aOutputStream, byte aByteValue) throws IOException {
        aOutputStream.write(aByteValue);
    }

    public static void writeData(OutputStream aOutputStream, short aShortValue) throws IOException {
        aOutputStream.write((aShortValue >> 8) & 255);
        aOutputStream.write(aShortValue & 255);
    }

    public static void writeData(OutputStream aOutputStream, char aCharValue) throws IOException {
        aOutputStream.write((aCharValue >> 8) & 255);
        aOutputStream.write(aCharValue & 255);
    }

    public static void writeData(OutputStream aOutputStream, int aIntValue) throws IOException {
        aOutputStream.write((aIntValue >> 24) & 255);
        aOutputStream.write((aIntValue >> 16) & 255);
        aOutputStream.write((aIntValue >> 8) & 255);
        aOutputStream.write(aIntValue & 255);
    }

    public static void writeData(OutputStream aOutputStream, long aLongValue) throws IOException {
        aOutputStream.write(((int) (aLongValue >> 56)) & 255);
        aOutputStream.write(((int) (aLongValue >> 48)) & 255);
        aOutputStream.write(((int) (aLongValue >> 40)) & 255);
        aOutputStream.write(((int) (aLongValue >> 32)) & 255);
        aOutputStream.write(((int) (aLongValue >> 24)) & 255);
        aOutputStream.write(((int) (aLongValue >> 16)) & 255);
        aOutputStream.write(((int) (aLongValue >> 8)) & 255);
        aOutputStream.write(((int) aLongValue) & 255);
    }

    public static void writeData(OutputStream aOutputStream, float aFloatValue) throws IOException {
        writeData(aOutputStream, Float.floatToIntBits(aFloatValue));
    }

    public static void writeData(OutputStream aOutputStream, double aDoubleValue) throws IOException {
        writeData(aOutputStream, Double.doubleToLongBits(aDoubleValue));
    }

    public static void writeData(OutputStream aOutputStream, byte[] aBinaryData) throws IOException {
        writeData(aOutputStream, aBinaryData.length);
        aOutputStream.write(aBinaryData);
    }

    public static void writeData(OutputStream aOutputStream, String aStringValue) throws IOException {
        writeData(aOutputStream, aStringValue.getBytes("unicode"));
    }
}
