package com.kota.telnet

import android.util.Log
import com.kota.asFramework.thread.ASRunner
import com.kota.dataPool.MutableByteBuffer
import com.kota.telnet.reference.TelnetDef
import com.kota.telnet.reference.TelnetKeyboard.getKeyData
import com.kota.textEncoder.U2BEncoder

class TelnetOutputBuilder {
    private val byteBuffers: MutableByteBuffer = MutableByteBuffer.createMutableByteBuffer()

    @JvmOverloads
    fun sendToServerInBackground(channel: Int = 0) {
        TelnetClient.Companion.client?.sendDataToServerInBackground(build(), channel)
    }

    fun sendToServer() {
        if (ASRunner.isMainThread) {
            sendToServerInBackground(0)
        } else {
            sendToServer(0)
        }
    }

    private fun sendToServer(channel: Int) {
        TelnetClient.Companion.client?.sendDataToServer(build(), channel)
    }

    fun build(): ByteArray? {
        this.byteBuffers.close()
        val data = this.byteBuffers.toByteArray()
        MutableByteBuffer.recycleMutableByteBuffer(this.byteBuffers)
        return data
    }

    fun pushData(data: Byte): TelnetOutputBuilder {
        this.byteBuffers.put(data)
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
                U2BEncoder.instance!!.encodeToBytes(str.toByteArray(charset(TelnetDef.CHARSET)), 0)
            )
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, (if (e.message != null) e.message else "")!!)
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
