package com.kota.textEncoder

import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.Vector

class B2UEncoder private constructor(inputStream: InputStream) {
    private var myBuffer: TextConverterBuffer? = null
    private var myOffset = 0
    private var myTable: CharArray? = null
    private var myTableSize = 0

    private val charSet = "UTF-16"

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
            println("read B2U encode data success")
        }
    }

    fun encodeChar(data: Char): Char {
        val index = data.code - this.myOffset
        return if (index < 0 || index >= this.myTableSize) data else this.myTable!![index]
    }

    fun encodeToString(stringData: ByteArray): String {
        try {
            return String(encodeToBytes(stringData), charset(charSet))
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, (if (e.message != null) e.message else "")!!)
            return ""
        }
    }

    private fun encodeToBytes(stringData: ByteArray): ByteArray {
        var upper = 0
        var upper2: Int
        val buffers = Vector<ByteBuffer>()
        buffers.add(this.buffer.createByteBuffer())
        buffers.firstElement().put((-2).toByte())
        buffers.firstElement().put((-1).toByte())
        var i = 0
        while (i < stringData.size && ((stringData[i].toInt() and 255).also { upper = it }) != 0) {
            var lower = upper
            if (upper <= 127 || i >= stringData.size - 1) {
                upper2 = 0
            } else {
                i++
                val c = encodeChar(((upper shl 8) + (stringData[i].toInt() and 255)).toChar()).code
                upper2 = (c shr 8) and 255
                lower = c and 255
            }
            if (!buffers.lastElement().hasRemaining()) {
                buffers.lastElement().flip()
                buffers.add(this.buffer.createByteBuffer())
            }
            buffers.lastElement().put(upper2.toByte())
            if (!buffers.lastElement().hasRemaining()) {
                buffers.lastElement().flip()
                buffers.add(this.buffer.createByteBuffer())
            }
            buffers.lastElement().put(lower.toByte())
            i++
        }
        buffers.lastElement().flip()
        var bufferSize = 0
        for (i2 in 0..<buffers.size - 1) {
            bufferSize += BUFFER_SIZE
        }
        val resultData = ByteArray((bufferSize + buffers.lastElement().limit()))
        var count = 0
        for (buffer in buffers) {
            for (i3 in 0..<buffer.limit()) {
                resultData[count] = buffer.get(i3)
                count++
            }
        }
        return resultData
    }

    private val buffer: TextConverterBuffer
        get() {
            if (this.myBuffer == null) {
                this.myBuffer = object : TextConverterBuffer {
                    override fun recycleByteBuffer(byteBuffer: ByteBuffer?) {
                    }

                    override fun createByteBuffer(): ByteBuffer {
                        return ByteBuffer.allocate(BUFFER_SIZE)
                    }
                }
            }
            return this.myBuffer!!
        }

    companion object {
        private const val BUFFER_SIZE = 1024
        const val CHAR_MAXIMUM: Int = 65535
        const val NULL_CHAR: Int = 65533
        var instance: B2UEncoder? = null
            private set

        @JvmStatic
        fun constructInstance(inputStream: InputStream) {
            instance = B2UEncoder(inputStream)
        }

        fun releaseInstance() {
            instance = null
        }
    }
}
