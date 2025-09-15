package com.kota.ASFramework.Utils

import java.io.IOException
import java.io.InputStream

object ASStreamReader {
    
    @Throws(IOException::class)
    fun readBoolean(inputStream: InputStream): Boolean {
        return inputStream.read() != 0
    }
    
    @Throws(IOException::class)
    fun readByte(inputStream: InputStream): Byte {
        return inputStream.read().toByte()
    }
    
    @Throws(IOException::class)
    fun readChar(inputStream: InputStream): Char {
        val high = inputStream.read()
        val low = inputStream.read()
        return ((high shl 8) + low).toChar()
    }
    
    @Throws(IOException::class)
    fun readDouble(inputStream: InputStream): Double {
        // TODO: Implement proper double reading
        return 0.0
    }
    
    @Throws(IOException::class)
    fun readInt(inputStream: InputStream): Int {
        val byte1 = inputStream.read()
        val byte2 = inputStream.read()
        val byte3 = inputStream.read()
        val byte4 = inputStream.read()
        return (byte1 shl 24) + (byte2 shl 16) + (byte3 shl 8) + byte4
    }
    
    @Throws(IOException::class)
    fun readShort(inputStream: InputStream): Short {
        val high = inputStream.read()
        val low = inputStream.read()
        return ((high shl 8) + low).toShort()
    }
}
