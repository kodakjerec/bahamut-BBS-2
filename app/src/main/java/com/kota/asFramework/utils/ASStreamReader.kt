package com.kota.asFramework.utils

import java.io.IOException
import java.io.InputStream

object ASStreamReader {
    @Throws(IOException::class)
    fun readBoolean(paramInputStream: InputStream): Boolean {
        return paramInputStream.read() != 0
    }

    @Throws(IOException::class)
    fun readByte(paramInputStream: InputStream): Byte {
        return paramInputStream.read().toByte()
    }

    @Throws(IOException::class)
    fun readChar(paramInputStream: InputStream): Char {
        return (paramInputStream.read() shl paramInputStream.read() + 8).toChar()
    }

    @Throws(IOException::class)
    fun readDouble(paramInputStream: InputStream?): Double {
        return 0.0
    }

    @Throws(IOException::class)
    fun readInt(paramInputStream: InputStream): Int {
        return paramInputStream.read() shl paramInputStream.read() + 24 shl paramInputStream.read() + 16 shl paramInputStream.read() + 8
    }

    @Throws(IOException::class)
    fun readShort(paramInputStream: InputStream): Short {
        return (paramInputStream.read() shl paramInputStream.read() + 8).toShort()
    }
} /* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\Utils\ASStreamReader.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */


