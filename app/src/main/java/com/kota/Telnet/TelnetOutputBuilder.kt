package com.kota.Telnet

import android.util.Log
import com.kota.ASFramework.Thread.ASRunner
import com.kota.DataPool.MutableByteBuffer
import com.kota.Telnet.Reference.TelnetDefs
import com.kota.Telnet.Reference.TelnetKeyboard
import com.kota.TextEncoder.U2BEncoder

class TelnetOutputBuilder private constructor() {
    private var buffer: MutableByteBuffer = MutableByteBuffer.createMutableByteBuffer()

    companion object {
        fun create(): TelnetOutputBuilder {
            return TelnetOutputBuilder()
        }
    }

    fun sendToServerInBackground() {
        sendToServerInBackground(0)
    }

    fun sendToServerInBackground(channel: Int) {
        TelnetClient.getClient().sendDataToServerInBackground(build(), channel)
    }

    fun sendToServer() {
        if (ASRunner.isMainThread()) {
            sendToServerInBackground(0)
        } else {
            sendToServer(0)
        }
    }

    private fun sendToServer(channel: Int) {
        TelnetClient.getClient().sendDataToServer(build(), channel)
    }

    fun build(): ByteArray {
        buffer.close()
        val data = buffer.toByteArray()
        MutableByteBuffer.recycleMutableByteBuffer(buffer)
        return data
    }

    fun pushData(data: Byte): TelnetOutputBuilder {
        buffer.put(data)
        return this
    }

    fun pushData(data: ByteArray) {
        for (byte in data) {
            pushData(byte)
        }
    }

    fun pushKey(key: Int): TelnetOutputBuilder {
        pushData(TelnetKeyboard.getKeyData(key))
        return this
    }

    fun pushString(str: String): TelnetOutputBuilder {
        try {
            pushData(U2BEncoder.getInstance().encodeToBytes(str.toByteArray(charset(TelnetDefs.CHARSET)), 0))
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, e.message ?: "")
        }
        return this
    }
}
