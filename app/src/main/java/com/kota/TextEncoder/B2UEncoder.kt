package com.kota.TextEncoder

import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.*

class B2UEncoder private constructor(inputStream: InputStream) {
    
    private var buffer: TextConverterBuffer? = null
    private var offset: Int = 0
    private var table: CharArray? = null
    private var tableSize: Int = 0
    private val charSet: String = "UTF-16"
    
    init {
        readTableFromInputStream(inputStream)
    }
    
    companion object {
        private const val BUFFER_SIZE = 1024
        const val CHAR_MAXIMUM = 65535
        const val NULL_CHAR = 65533
        
        @Volatile
        private var instance: B2UEncoder? = null
        
        fun getInstance(): B2UEncoder? = instance
        
        fun constructInstance(inputStream: InputStream) {
            instance = B2UEncoder(inputStream)
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
            println("read B2U encode data success")
        }
    }
    
    fun encodeChar(data: Char): Char {
        val index = data.code - offset
        val currentTable = table ?: return data
        return if (index < 0 || index >= tableSize) data else currentTable[index]
    }
    
    fun encodeToString(stringData: ByteArray): String {
        return try {
            String(encodeToBytes(stringData), charset(charSet))
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, e.message ?: "Unknown error")
            ""
        }
    }
    
    private fun encodeToBytes(stringData: ByteArray): ByteArray {
        val buffers = Vector<ByteBuffer>()
        buffers.add(getBuffer().createByteBuffer())
        buffers.first().put((-2).toByte())
        buffers.first().put((-1).toByte())
        
        var i = 0
        while (i < stringData.size) {
            val upper = stringData[i].toInt() and 255
            if (upper == 0) break
            
            var lower = upper
            var upper2 = 0
            
            if (upper > 127 && i < stringData.size - 1) {
                i++
                val c = encodeChar(((upper shl 8) + (stringData[i].toInt() and 255)).toChar())
                upper2 = (c.code shr 8) and 255
                lower = c.code and 255
            }
            
            // 確保緩衝區有足夠空間
            if (!buffers.last().hasRemaining()) {
                buffers.last().flip()
                buffers.add(getBuffer().createByteBuffer())
            }
            buffers.last().put(upper2.toByte())
            
            if (!buffers.last().hasRemaining()) {
                buffers.last().flip()
                buffers.add(getBuffer().createByteBuffer())
            }
            buffers.last().put(lower.toByte())
            
            i++
        }
        
        buffers.last().flip()
        
        // 計算總大小
        var bufferSize = 0
        for (j in 0 until buffers.size - 1) {
            bufferSize += BUFFER_SIZE
        }
        
        val resultData = ByteArray(bufferSize + buffers.last().limit())
        var count = 0
        
        for (buffer in buffers) {
            repeat(buffer.limit()) { index ->
                resultData[count] = buffer.get(index)
                count++
            }
        }
        
        return resultData
    }
    
    private fun getBuffer(): TextConverterBuffer {
        return buffer ?: object : TextConverterBuffer {
            override fun recycleByteBuffer(byteBuffer: ByteBuffer) {
                // 不做任何處理
            }
            
            override fun createByteBuffer(): ByteBuffer {
                return ByteBuffer.allocate(BUFFER_SIZE)
            }
        }.also { buffer = it }
    }
}
