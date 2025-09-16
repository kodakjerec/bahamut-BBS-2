package com.kota.Telnet

import android.util.Log
import com.kota.ASFramework.Thread.ASRunner
import com.kota.DataPool.MutableByteBuffer
import com.kota.Telnet.Reference.TelnetDefs
import com.kota.Telnet.Reference.TelnetKeyboard.getKeyData
import com.kota.TextEncoder.U2BEncoder

class TelnetOutputBuilder {
    private val _buffer: MutableByteBuffer = MutableByteBuffer.createMutableByteBuffer()

    @JvmOverloads
    fun sendToServerInBackground(channel: Int = 0) {
        TelnetClient.Companion.getClient().sendDataToServerInBackground(build(), channel)
    }

    fun sendToServer() {
        if (ASRunner.isMainThread()) {
            sendToServerInBackground(0)
        } else {
            sendToServer(0)
        }
    }

    private fun sendToServer(channel: Int) {
        TelnetClient.Companion.getClient().sendDataToServer(build(), channel)
    }

    fun build(): ByteArray? {
        this._buffer.close()
        val data = this._buffer.toByteArray()
        MutableByteBuffer.recycleMutableByteBuffer(this._buffer)
        return data
    }

    fun pushData(data: Byte): TelnetOutputBuilder {
        this._buffer.put(data)
        return this
    }

    fun pushData(data: ByteArray) {
        for (pushData in data) {
            pushData(pushData)
        }
    }

    fun pushKey(key: Int): TelnetOutputBuilder {
        pushData(getKeyData(key))
        return this
    }

    fun pushString(str: String): TelnetOutputBuilder {
        try {
            pushData(
                U2BEncoder.getInstance()
                    .encodeToBytes(str.toByteArray(charset(TelnetDefs.CHARSET)), 0)
            )
        } catch (e: Exception) {
            Log.e(javaClass.getSimpleName(), (if (e.message != null) e.message else "")!!)
        }
        return this
    }

    companion object {
        @JvmStatic
        fun create(): TelnetOutputBuilder {
            return TelnetOutputBuilder()
        }
    }
}
