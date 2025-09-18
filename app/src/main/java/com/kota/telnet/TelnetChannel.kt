package com.kota.telnet

import android.util.Log
import com.kota.dataPool.MutableByteBuffer
import com.kota.textEncoder.B2UEncoder
import java.io.IOException
import java.nio.ByteBuffer

class TelnetChannel(aSocketChannel: TelnetSocketChannel) {
    private val inputBuffer: ByteBuffer = ByteBuffer.allocate(BUFFER_SIZE)
    private var channelListener: TelnetChannelListener? = null
    private var isLocked = false
    private val outputBuffer: MutableByteBuffer = MutableByteBuffer.createMutableByteBuffer()
    var readDataSize: Int = 0
        private set
    private var socketChannel: TelnetSocketChannel? = null

    init {
        this.socketChannel = aSocketChannel
    }

    fun setListener(aListener: TelnetChannelListener?) {
        this.channelListener = aListener
    }

    fun clear() {
        this.inputBuffer.clear()
        this.outputBuffer.clear()
    }

    fun lock() {
        this.isLocked = true
    }

    fun unlock() {
        this.isLocked = false
    }

    fun writeData(data: ByteArray) {
        for (b in data) {
            writeData(b)
        }
    }

    fun writeData(data: Byte) {
        this.outputBuffer.put(data)
    }

    fun sendData(): Boolean {
        if (this.isLocked || this.outputBuffer.size() <= 0) {
            return false
        }
        try {
            this.outputBuffer.close()
            for (byteBuffer in this.outputBuffer) {
                this.socketChannel!!.write(byteBuffer)
            }
            //            printOutputBuffer();
            this.outputBuffer.clear()
            return true
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, (if (e.message != null) e.message else "")!!)
            return false
        }
    }

    @Throws(TelnetConnectionClosedException::class, IOException::class)
    fun readData(): Byte {
        if (!this.inputBuffer.hasRemaining()) {
            onReceiveDataStart()
            receiveData()
            onReceiveDataFinished()
        }
        this.inputBuffer.mark()
        val data = this.inputBuffer.get()
        this.readDataSize++
        return data
    }

    fun undoReadData() {
        this.inputBuffer.reset()
        this.readDataSize--
    }

    @Throws(TelnetConnectionClosedException::class, IOException::class)
    private fun receiveData() {
        this.inputBuffer.clear()
        this.socketChannel!!.read(this.inputBuffer)
        if (this.inputBuffer.position() != 0) {
            this.inputBuffer.flip()
            return
        }
        throw TelnetConnectionClosedException
    }

    private fun onReceiveDataStart() {
        if (this.channelListener != null) {
            this.channelListener!!.onTelnetChannelReceiveDataStart(this)
        }
    }

    private fun onReceiveDataFinished() {
        if (this.channelListener != null) {
            this.channelListener!!.onTelnetChannelReceiveDataFinished(this)
            //            printInputBuffer();
        }
    }

    /** 列印收到的資料  */
    private fun printInputBuffer() {
        val data = ByteArray(this.inputBuffer.limit())
        for (i in 0..<this.inputBuffer.limit()) {
            data[i] = this.inputBuffer.array()[i]
        }
        try {
            println("receive data:" + B2UEncoder.instance!!.encodeToString(data))
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, (if (e.message != null) e.message else "")!!)
        }
    }

    /** 列印送出的資料  */
    private fun printOutputBuffer() {
        val data = ByteArray(this.outputBuffer.size())
        var position = 0
        for (outputBuffer in this.outputBuffer) {
            for (i in 0..<outputBuffer!!.limit()) {
                data[position] = outputBuffer.array()[i]
                position++
            }
        }
        try {
            println("send data:\n" + B2UEncoder.instance!!.encodeToString(data))
            var hexData = ""
            for (datum in data) {
                hexData = hexData + String.format(" %1$02x", datum)
            }
            println("send hex data:\n" + hexData.substring(1))
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, (if (e.message != null) e.message else "")!!)
        }
    }

    fun cleanReadDataSize() {
        this.readDataSize = 0
    }

    companion object {
        var BUFFER_SIZE: Int = 2048
    }
}
