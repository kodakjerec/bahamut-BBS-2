package com.kota.Telnet

import android.util.Log
import com.kota.DataPool.MutableByteBuffer
import com.kota.TextEncoder.B2UEncoder
import java.io.IOException
import java.nio.ByteBuffer

class TelnetChannel(private val _socket_channel: TelnetSocketChannel) {
    companion object {
        var BUFFER_SIZE = 2048
    }

    private val _input_buffer = ByteBuffer.allocate(BUFFER_SIZE)
    private var _listener: TelnetChannelListener? = null
    private var _lock = false
    private val _output_buffer = MutableByteBuffer.createMutableByteBuffer()
    private var _read_data_size = 0

    fun setListener(aListener: TelnetChannelListener) {
        _listener = aListener
    }

    fun clear() {
        _input_buffer.clear()
        _output_buffer.clear()
    }

    fun lock() {
        _lock = true
    }

    fun unlock() {
        _lock = false
    }

    fun writeData(data: ByteArray) {
        for (b in data) {
            writeData(b)
        }
    }

    fun writeData(data: Byte) {
        _output_buffer.put(data)
    }

    fun sendData(): Boolean {
        if (_lock || _output_buffer.size() <= 0) {
            return false
        }
        return try {
            _output_buffer.close()
            for (byteBuffer in _output_buffer) {
                _socket_channel.write(byteBuffer)
            }
            // printOutputBuffer()
            _output_buffer.clear()
            true
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, e.message ?: "")
            false
        }
    }

    @Throws(TelnetConnectionClosedException::class, IOException::class)
    fun readData(): Byte {
        if (!_input_buffer.hasRemaining()) {
            onReceiveDataStart()
            receiveData()
            onReceiveDataFinished()
        }
        _input_buffer.mark()
        val data = _input_buffer.get()
        _read_data_size++
        return data
    }

    fun undoReadData() {
        _input_buffer.reset()
        _read_data_size--
    }

    @Throws(TelnetConnectionClosedException::class, IOException::class)
    private fun receiveData() {
        _input_buffer.clear()
        _socket_channel.read(_input_buffer)
        if (_input_buffer.position() != 0) {
            _input_buffer.flip()
            return
        }
        throw TelnetConnectionClosedException()
    }

    private fun onReceiveDataStart() {
        _listener?.onTelnetChannelReceiveDataStart(this)
    }

    private fun onReceiveDataFinished() {
        _listener?.onTelnetChannelReceiveDataFinished(this)
        // printInputBuffer()
    }

    /** 列印收到的資料 */
    private fun printInputBuffer() {
        val data = ByteArray(_input_buffer.limit())
        for (i in 0 until _input_buffer.limit()) {
            data[i] = _input_buffer.array()[i]
        }
        try {
            println("receive data:${B2UEncoder.getInstance().encodeToString(data)}")
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, e.message ?: "")
        }
    }

    /** 列印送出的資料 */
    private fun printOutputBuffer() {
        val data = ByteArray(_output_buffer.size())
        var position = 0
        for (outputBuffer in _output_buffer) {
            for (i in 0 until outputBuffer.limit()) {
                data[position] = outputBuffer.array()[i]
                position++
            }
        }
        try {
            println("send data:\n${B2UEncoder.getInstance().encodeToString(data)}")
            var hexData = ""
            for (datum in data) {
                hexData += String.format(" %1\$02x", datum)
            }
            println("send hex data:\n${hexData.substring(1)}")
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, e.message ?: "")
        }
    }

    fun cleanReadDataSize() {
        _read_data_size = 0
    }

    fun getReadDataSize(): Int = _read_data_size
}
