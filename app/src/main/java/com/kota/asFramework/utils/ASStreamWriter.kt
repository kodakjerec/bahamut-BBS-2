package com.kota.asFramework.utils

import java.io.IOException
import java.io.OutputStream
import java.lang.Double
import java.lang.Float
import kotlin.Boolean
import kotlin.Byte
import kotlin.ByteArray
import kotlin.Char
import kotlin.Int
import kotlin.Long
import kotlin.Short
import kotlin.String
import kotlin.Throws
import kotlin.code

object ASStreamWriter {
    @Throws(IOException::class)
    fun writeData(paramOutputStream: OutputStream, paramByte: Byte) {
        paramOutputStream.write(paramByte.toInt())
    }

    @Throws(IOException::class)
    fun writeData(paramOutputStream: OutputStream, paramChar: Char) {
        paramOutputStream.write(paramChar.code shr 8 and 255)
        paramOutputStream.write(paramChar.code and 255)
    }

    @Throws(IOException::class)
    fun writeData(paramOutputStream: OutputStream, paramDouble: Double) {
        writeData(paramOutputStream, Double.doubleToLongBits(paramDouble))
    }

    @Throws(IOException::class)
    fun writeData(paramOutputStream: OutputStream, paramFloat: Float) {
        writeData(paramOutputStream, Float.floatToIntBits(paramFloat))
    }

    @Throws(IOException::class)
    fun writeData(paramOutputStream: OutputStream, paramInt: Int) {
        paramOutputStream.write(paramInt shr 24 and 255)
        paramOutputStream.write(paramInt shr 16 and 255)
        paramOutputStream.write(paramInt shr 8 and 255)
        paramOutputStream.write(paramInt and 255)
    }

    @Throws(IOException::class)
    fun writeData(paramOutputStream: OutputStream, paramLong: Long) {
        paramOutputStream.write((paramLong shr 56L.toInt()).toInt() and 255)
        paramOutputStream.write((paramLong shr 48L.toInt()).toInt() and 255)
        paramOutputStream.write((paramLong shr 40L.toInt()).toInt() and 255)
        paramOutputStream.write((paramLong shr 32L.toInt()).toInt() and 255)
        paramOutputStream.write((paramLong shr 24L.toInt()).toInt() and 255)
        paramOutputStream.write((paramLong shr 16L.toInt()).toInt() and 255)
        paramOutputStream.write((paramLong shr 8L.toInt()).toInt() and 255)
        paramOutputStream.write(paramLong.toInt() and 255)
    }

    @Throws(IOException::class)
    fun writeData(paramOutputStream: OutputStream, paramString: String) {
        writeData(paramOutputStream, paramString.toByteArray(charset("unicode")))
    }

    @Throws(IOException::class)
    fun writeData(paramOutputStream: OutputStream, paramShort: Short) {
        paramOutputStream.write(paramShort.toInt() shr 8 and 255)
        paramOutputStream.write(paramShort.toInt() and 255)
    }

    @Throws(IOException::class)
    fun writeData(paramOutputStream: OutputStream, paramBoolean: Boolean) {
        if (paramBoolean) {
            paramOutputStream.write(1)
            return
        }
        paramOutputStream.write(0)
    }

    @Throws(IOException::class)
    fun writeData(paramOutputStream: OutputStream, paramArrayOfbyte: ByteArray) {
        writeData(paramOutputStream, paramArrayOfbyte.size)
        paramOutputStream.write(paramArrayOfbyte)
    }
} /* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\Utils\ASStreamWriter.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */


