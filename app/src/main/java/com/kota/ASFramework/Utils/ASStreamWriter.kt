package com.kota.ASFramework.Utils

import java.io.IOException
import java.io.OutputStream

object ASStreamWriter {
    
    @Throws(IOException::class)
    fun writeData(outputStream: OutputStream, value: Byte) {
        outputStream.write(value.toInt())
    }
    
    @Throws(IOException::class)
    fun writeData(outputStream: OutputStream, value: Char) {
        outputStream.write((value.code shr 8) and 255)
        outputStream.write(value.code and 255)
    }
    
    @Throws(IOException::class)
    fun writeData(outputStream: OutputStream, value: Double) {
        writeData(outputStream, value.toRawBits())
    }
    
    @Throws(IOException::class)
    fun writeData(outputStream: OutputStream, value: Float) {
        writeData(outputStream, value.toBits())
    }
    
    @Throws(IOException::class)
    fun writeData(outputStream: OutputStream, value: Int) {
        outputStream.write((value shr 24) and 255)
        outputStream.write((value shr 16) and 255)
        outputStream.write((value shr 8) and 255)
        outputStream.write(value and 255)
    }
    
    @Throws(IOException::class)
    fun writeData(outputStream: OutputStream, value: Long) {
        outputStream.write(((value shr 56) and 255L).toInt())
        outputStream.write(((value shr 48) and 255L).toInt())
        outputStream.write(((value shr 40) and 255L).toInt())
        outputStream.write(((value shr 32) and 255L).toInt())
        outputStream.write(((value shr 24) and 255L).toInt())
        outputStream.write(((value shr 16) and 255L).toInt())
        outputStream.write(((value shr 8) and 255L).toInt())
        outputStream.write((value and 255L).toInt())
    }
    
    @Throws(IOException::class)
    fun writeData(outputStream: OutputStream, value: String) {
        writeData(outputStream, value.toByteArray(charset("UTF-16")))
    }
    
    @Throws(IOException::class)
    fun writeData(outputStream: OutputStream, value: Short) {
        outputStream.write((value.toInt() shr 8) and 255)
        outputStream.write(value.toInt() and 255)
    }
    
    @Throws(IOException::class)
    fun writeData(outputStream: OutputStream, value: Boolean) {
        outputStream.write(if (value) 1 else 0)
    }
    
    @Throws(IOException::class)
    fun writeData(outputStream: OutputStream, value: ByteArray) {
        writeData(outputStream, value.size)
        outputStream.write(value)
    }
}
