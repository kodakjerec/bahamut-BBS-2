package com.kota.telnet

import android.util.Log
import com.kota.dataPool.MutableByteBuffer
import com.kota.textEncoder.B2UEncoder
import java.io.IOException
import java.nio.ByteBuffer

class TelnetChannel(aSocketChannel: TelnetSocketChannel?) {
    private val _input_buffer: ByteBuffer = ByteBuffer.allocate(BUFFER_SIZE)
    private var _listener: TelnetChannelListener? = null
    private var _lock = false
    private val _output_buffer: MutableByteBuffer = MutableByteBuffer.createMutableByteBuffer()
    var readDataSize: Int = 0
        private set
    private var _socket_channel: TelnetSocketChannel? = null

    init {
        this._socket_channel = aSocketChannel
    }

    fun setListener(aListener: TelnetChannelListener?) {
        this._listener = aListener
    }

    fun clear() {
        this._input_buffer.clear()
        this._output_buffer.clear()
    }

    fun lock() {
        this._lock = true
    }

    fun unlock() {
        this._lock = false
    }

    fun writeData(data: ByteArray) {
        for (b in data) {
            writeData(b)
        }
    }

    fun writeData(data: Byte) {
        this._output_buffer.put(data)
    }

    fun sendData(): Boolean {
        if (this._lock || this._output_buffer.size() <= 0) {
            return false
        }
        try {
            this._output_buffer.close()
            for (byteBuffer in this._output_buffer) {
                this._socket_channel!!.write(byteBuffer)
            }
            //            printOutputBuffer();
            this._output_buffer.clear()
            return true
        } catch (e: Exception) {
            Log.e(javaClass.getSimpleName(), (if (e.message != null) e.message else "")!!)
            return false
        }
    }

    @Throws(TelnetConnectionClosedException::class, IOException::class)
    fun readData(): Byte {
        if (!this._input_buffer.hasRemaining()) {
            onReceiveDataStart()
            receiveData()
            onReceiveDataFinished()
        }
        this._input_buffer.mark()
        val data = this._input_buffer.get()
        this.readDataSize++
        return data
    }

    fun undoReadData() {
        this._input_buffer.reset()
        this.readDataSize--
    }

    @Throws(TelnetConnectionClosedException::class, IOException::class)
    private fun receiveData() {
        this._input_buffer.clear()
        this._socket_channel!!.read(this._input_buffer)
        if (this._input_buffer.position() != 0) {
            this._input_buffer.flip()
            return
        }
        throw TelnetConnectionClosedException()
    }

    private fun onReceiveDataStart() {
        if (this._listener != null) {
            this._listener!!.onTelnetChannelReceiveDataStart(this)
        }
    }

    private fun onReceiveDataFinished() {
        if (this._listener != null) {
            this._listener!!.onTelnetChannelReceiveDataFinished(this)
            //            printInputBuffer();
        }
    }

    /** 列印收到的資料  */
    private fun printInputBuffer() {
        val data = ByteArray(this._input_buffer.limit())
        for (i in 0..<this._input_buffer.limit()) {
            data[i] = this._input_buffer.array()[i]
        }
        try {
            println("receive data:" + B2UEncoder.getInstance().encodeToString(data))
        } catch (e: Exception) {
            Log.e(javaClass.getSimpleName(), (if (e.message != null) e.message else "")!!)
        }
    }

    /** 列印送出的資料  */
    private fun printOutputBuffer() {
        val data = ByteArray(this._output_buffer.size())
        var position = 0
        for (output_buffer in this._output_buffer) {
            for (i in 0..<output_buffer.limit()) {
                data[position] = output_buffer.array()[i]
                position++
            }
        }
        try {
            println("send data:\n" + B2UEncoder.getInstance().encodeToString(data))
            var hex_data = ""
            for (datum in data) {
                hex_data = hex_data + String.format(" %1$02x", datum)
            }
            println("send hex data:\n" + hex_data.substring(1))
        } catch (e: Exception) {
            Log.e(javaClass.getSimpleName(), (if (e.message != null) e.message else "")!!)
        }
    }

    fun cleanReadDataSize() {
        this.readDataSize = 0
    }

    companion object {
        var BUFFER_SIZE: Int = 2048
    }
}
