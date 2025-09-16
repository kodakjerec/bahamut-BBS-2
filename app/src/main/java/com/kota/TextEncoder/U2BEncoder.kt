package com.kota.TextEncoder

import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer

class U2BEncoder private constructor(inputStream: InputStream) {
    private var _offset = 0
    private var _table: CharArray? = null
    private var _table_size = 0

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
            this._offset = readCharFromStream(inputStream).code
            this._table_size = readCharFromStream(inputStream).code
            this._table = CharArray(this._table_size)
            for (i in this._table!!.indices) {
                this._table!![i] = NULL_CHAR.toChar()
            }
            var i2 = 0
            while (i2 < total.code) {
                val index = readCharFromStream(inputStream)
                this._table!![index.code - this._offset] = readCharFromStream(inputStream)
                i2++
            }
            inputStream.close()
            result = true
        } catch (e: Exception) {
            Log.e(javaClass.getSimpleName(), (if (e.message != null) e.message else "")!!)
        }
        if (result) {
            println("read U2B encode data success")
        }
    }

    fun encodeChar(data: Char): Char {
        val index = data.code - this._offset
        return if (index < 0 || index >= this._table_size) data else this._table!![index]
    }

    fun encodeToBytes(data: ByteArray, start: Int): ByteArray {
        val string_buffer = ByteBuffer.allocate(data.size)
        string_buffer.clear()
        var i = start
        while (i < data.size) {
            val c =
                encodeChar((((data[i + 1].toInt() and 255) shl 8) + (data[i].toInt() and 255)).toChar()).code
            val upper = (c shr 8) and 255
            val lower = c and 255
            if (upper > 0) {
                string_buffer.put(upper.toByte())
            }
            string_buffer.put(lower.toByte())
            i += 2
        }
        string_buffer.flip()
        val result_data = ByteArray(string_buffer.limit())
        for (i2 in result_data.indices) {
            result_data[i2] = string_buffer.get()
        }
        return result_data
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
