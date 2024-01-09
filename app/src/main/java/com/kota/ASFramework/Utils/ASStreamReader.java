package com.kota.ASFramework.Utils;

import java.io.IOException;
import java.io.InputStream;

public class ASStreamReader {
    public static boolean readBoolean(InputStream aInputStream) throws IOException {
        return aInputStream.read() != 0;
    }

    public static byte readByte(InputStream aInputStream) throws IOException {
        return (byte) aInputStream.read();
    }

    public static short readShort(InputStream aInputStream) throws IOException {
        return (short) (aInputStream.read() << (aInputStream.read() + 8));
    }

    public static char readChar(InputStream aInputStream) throws IOException {
        return (char) (aInputStream.read() << (aInputStream.read() + 8));
    }

    public static int readInt(InputStream aInputStream) throws IOException {
        return ((aInputStream.read() << (aInputStream.read() + 24)) << (aInputStream.read() + 16)) << (aInputStream.read() + 8);
    }

    public static double readDouble(InputStream aInputStream) throws IOException {
        return 0.0d;
    }
}
