package com.kota.textEncoder

import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer

class U2BEncoder private constructor(inputStream: InputStream) {
    private var myOffset = 0
    private var myTable: CharArray? = null
    private var myTableSize = 0

    init {
        readTableFromInputStream(inputStream)
    }

    @Throws(IOException::class)
    private fun readCharFromStream(fis: InputStream): Char {
        return ((fis.read() shl 8) + fis.read()).toChar()
    }

    private fun readTableFromInputStream(inputStream: InputStream) {
        var result = false
        try {
            val total = readCharFromStream(inputStream)
            this.myOffset = readCharFromStream(inputStream).code
            this.myTableSize = readCharFromStream(inputStream).code
            this.myTable = CharArray(this.myTableSize)
            for (i in this.myTable?.indices) {
                this.myTable!![i] = NULL_CHAR.toChar()
            }
            var i2 = 0
            while (i2 < total.code) {
                val index = readCharFromStream(inputStream)
                this.myTable!![index.code - this.myOffset] = readCharFromStream(inputStream)
                i2++
            }
            inputStream.close()
            result = true
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, (if (e.message != null) e.message else "")!!)
        }
        if (result) {
            println("read U2B encode data success")
        }
    }

    fun encodeChar(data: Char): Char {
        val index = data.code - this.myOffset
        return if (index < 0 || index >= this.myTableSize) data else this.myTable!![index]
    }

    fun encodeToBytes(data: ByteArray, start: Int): ByteArray {
        val stringBuffer = ByteBuffer.allocate(data.size)
        stringBuffer.clear()
        var i = start
        while (i < data.size) {
            val c =
                encodeChar((((data[i + 1].toInt() and 255) shl 8) + (data[i].toInt() and 255)).toChar()).code
            val upper = (c shr 8) and 255
            val lower = c and 255
            if (upper > 0) {
                stringBuffer.put(upper.toByte())
            }
            stringBuffer.put(lower.toByte())
            i += 2
        }
        stringBuffer.flip()
        val resultData = ByteArray(stringBuffer.limit())
        for (i2 in resultData.indices) {
            resultData[i2] = stringBuffer.get()
        }
        return resultData
    }

    companion object {
        const val CHAR_MAXIMUM: Int = 65535
        const val NULL_CHAR: Int = 65533
        var instance: U2BEncoder? = null
            private set

        @JvmStatic
        fun constructInstance(tableInputStream: InputStream) {
            instance = U2BEncoder(tableInputStream)
        }

        fun releaseInstance() {
            instance = null
        }
    }
}
