package com.kota.TextEncoder

import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer

class U2BEncoder private constructor(inputStream: InputStream) {
    
    private var offset: Int = 0
    private var table: CharArray? = null
    private var tableSize: Int = 0
    
    init {
        readTableFromInputStream(inputStream)
    }
    
    companion object {
        const val CHAR_MAXIMUM = 65535
        const val NULL_CHAR = 65533
        
        @Volatile
        private var instance: U2BEncoder? = null
        
        fun getInstance(): U2BEncoder? = instance
        
        fun constructInstance(tableInputStream: InputStream) {
            instance = U2BEncoder(tableInputStream)
        }
        
        fun releaseInstance() {
            instance = null
        }
    }
    
    @Throws(IOException::class)
    private fun readCharFromStream(fis: InputStream): Char {
        return ((fis.read() shl 8) + fis.read()).toChar()
    }
    
    private fun readTableFromInputStream(inputStream: InputStream) {
        var result = false
        try {
            val total = readCharFromStream(inputStream)
            offset = readCharFromStream(inputStream).code
            tableSize = readCharFromStream(inputStream).code
            table = CharArray(tableSize) { NULL_CHAR.toChar() }
            
            repeat(total.code) {
                val index = readCharFromStream(inputStream)
                table!![index.code - offset] = readCharFromStream(inputStream)
            }
            
            inputStream.close()
            result = true
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, e.message ?: "Unknown error")
        }
        
        if (result) {
            println("read U2B encode data success")
        }
    }
    
    fun encodeChar(data: Char): Char {
        val index = data.code - offset
        val currentTable = table ?: return data
        return if (index < 0 || index >= tableSize) data else currentTable[index]
    }
    
    fun encodeToBytes(data: ByteArray, start: Int): ByteArray {
        val stringBuffer = ByteBuffer.allocate(data.size)
        stringBuffer.clear()
        
        var i = start
        while (i < data.size) {
            val c = encodeChar(((data[i + 1].toInt() and 255) shl 8) + (data[i].toInt() and 255)).toChar()
            val upper = (c.code shr 8) and 255
            val lower = c.code and 255
            
            if (upper > 0) {
                stringBuffer.put(upper.toByte())
            }
            stringBuffer.put(lower.toByte())
            i += 2
        }
        
        stringBuffer.flip()
        val resultData = ByteArray(stringBuffer.limit())
        
        repeat(resultData.size) { index ->
            resultData[index] = stringBuffer.get()
        }
        
        return resultData
    }
}
