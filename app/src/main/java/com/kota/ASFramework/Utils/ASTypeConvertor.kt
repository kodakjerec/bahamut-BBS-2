package com.kota.ASFramework.Utils

import java.io.UnsupportedEncodingException

object ASTypeConvertor {
    
    fun getData(booleanValue: Boolean): ByteArray {
        return if (booleanValue) byteArrayOf(1) else byteArrayOf(0)
    }
    
    fun getData(shortValue: Short): ByteArray {
        return byteArrayOf(
            (shortValue.toInt() shr 8).toByte(), 
            shortValue.toByte()
        )
    }
    
    fun getData(charValue: Char): ByteArray {
        return byteArrayOf(
            (charValue.code shr 8).toByte(), 
            charValue.code.toByte()
        )
    }
    
    fun getData(intValue: Int): ByteArray {
        return byteArrayOf(
            (intValue shr 24).toByte(),
            (intValue shr 16).toByte(),
            (intValue shr 8).toByte(),
            intValue.toByte()
        )
    }
    
    fun getData(longValue: Long): ByteArray {
        return byteArrayOf(
            (longValue shr 56).toByte(),
            (longValue shr 48).toByte(),
            (longValue shr 40).toByte(),
            (longValue shr 32).toByte(),
            (longValue shr 24).toByte(),
            (longValue shr 16).toByte(),
            (longValue shr 8).toByte(),
            longValue.toByte()
        )
    }
    
    fun getData(floatValue: Float): ByteArray {
        return getData(floatValue.toBits())
    }
    
    fun getData(doubleValue: Double): ByteArray {
        return getData(doubleValue.toRawBits())
    }
    
    fun getData(string: String): ByteArray {
        val stringData = try {
            string.toByteArray(charset("UTF-16"))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            byteArrayOf()
        }
        
        val sizeData = getData(stringData.size)
        val data = ByteArray(stringData.size + 4)
        
        // 複製大小資料
        System.arraycopy(sizeData, 0, data, 0, 4)
        // 複製字串資料
        System.arraycopy(stringData, 0, data, 4, stringData.size)
        
        return data
    }
}
