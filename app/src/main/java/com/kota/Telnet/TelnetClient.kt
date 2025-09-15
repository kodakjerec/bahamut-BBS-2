package com.kota.Telnet

import android.util.Log
import com.kota.ASFramework.Thread.ASRunner
import com.kota.Telnet.Model.TelnetModel
import com.kota.Telnet.Reference.TelnetDefs
import com.kota.Telnet.Reference.TelnetKeyboard
import com.kota.TextEncoder.U2BEncoder
import java.io.UnsupportedEncodingException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TelnetClient private constructor(
    private val stateHandler: TelnetStateHandler
) : TelnetConnectorListener {
    
    private val connector: TelnetConnector
    private val model: TelnetModel
    private val receiver: TelnetReceiver
    private var username: String = ""
    private var listener: TelnetClientListener? = null
    private val sendExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        model = TelnetModel()
        connector = TelnetConnector()
        connector.setListener(this)
        receiver = TelnetReceiver(connector, model)
    }

    companion object {
        private var instance: TelnetClient? = null

        fun construct(stateHandler: TelnetStateHandler) {
            instance = TelnetClient(stateHandler)
        }

        fun getClient(): TelnetClient {
            return instance ?: throw IllegalStateException("TelnetClient not constructed")
        }

        fun getConnector(): TelnetConnector {
            return getClient().connector
        }

        fun getModel(): TelnetModel {
            return getClient().model
        }
    }

    fun clear() {
        stateHandler.clear()
        connector.clear()
        model.clear()
        receiver.stopReceiver()
    }

    fun connect(serverIp: String, serverPort: Int) {
        connector.connect(serverIp, serverPort)
    }

    fun close() {
        try {
            connector.close() // 關閉連線
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, e.message ?: "")
        }
    }

    fun sendStringToServer(str: String) {
        if (ASRunner.isMainThread()) {
            sendStringToServerInBackground(str, 0)
        } else {
            sendStringToServer(str, 0)
        }
    }

    private fun sendStringToServer(str: String, channel: Int) {
        var data: ByteArray? = null
        var encodeSuccess = false
        try {
            val data2 = (str + "\n").toByteArray(charset(TelnetDefs.CHARSET))
            data = U2BEncoder.getInstance().encodeToBytes(data2, 0)
            encodeSuccess = true
        } catch (e: UnsupportedEncodingException) {
            Log.e(javaClass.simpleName, e.message ?: "")
        }
        if (encodeSuccess) {
            sendDataToServer(data, channel)
        }
    }

    fun sendStringToServerInBackground(str: String) {
        sendStringToServerInBackground(str, 0)
    }

    fun sendStringToServerInBackground(str: String, channel: Int) {
        var data: ByteArray? = null
        var encodeSuccess = false
        try {
            val data2 = (str + "\n").toByteArray(charset(TelnetDefs.CHARSET))
            data = U2BEncoder.getInstance().encodeToBytes(data2, 0)
            encodeSuccess = true
        } catch (e: UnsupportedEncodingException) {
            Log.e(javaClass.simpleName, e.message ?: "")
        }
        if (encodeSuccess) {
            sendDataToServerInBackground(data, channel)
        }
    }

    fun sendKeyboardInputToServer(key: Int) {
        if (ASRunner.isMainThread()) {
            sendKeyboardInputToServerInBackground(key, 0)
        } else {
            sendKeyboardInputToServer(key, 0)
        }
    }

    private fun sendKeyboardInputToServer(key: Int, channel: Int) {
        sendDataToServer(TelnetKeyboard.getKeyData(key), channel)
    }

    fun sendKeyboardInputToServerInBackground(key: Int) {
        sendKeyboardInputToServerInBackground(key, 0)
    }

    fun sendKeyboardInputToServerInBackground(key: Int, channel: Int) {
        sendDataToServerInBackground(TelnetKeyboard.getKeyData(key), channel)
    }

    fun sendDataToServer(data: ByteArray?) {
        if (ASRunner.isMainThread()) {
            sendDataToServerInBackground(data, 0)
        } else {
            sendDataToServer(data, 0)
        }
    }

    protected fun sendDataToServer(data: ByteArray?, channel: Int) {
        if (data != null && connector.isConnecting()) {
            connector.writeData(data, channel)
            connector.sendData(channel)
        }
    }

    fun sendDataToServerInBackground(data: ByteArray?, channel: Int) {
        if (data != null && connector.isConnecting()) {
            sendExecutor.submit {
                connector.writeData(data, channel)
                connector.sendData(channel)
            }
        }
    }

    fun setListener(listener: TelnetClientListener?) {
        this.listener = listener
    }

    override fun onTelnetConnectorConnectStart(connector: TelnetConnector) {
        listener?.onTelnetClientConnectionStart(this)
    }

    override fun onTelnetConnectorClosed(connector: TelnetConnector) {
        clear()
        listener?.onTelnetClientConnectionClosed(this)
    }

    override fun onTelnetConnectorConnectSuccess(connector: TelnetConnector) {
        receiver.startReceiver()
        listener?.onTelnetClientConnectionSuccess(this)
    }

    override fun onTelnetConnectorConnectFail(connector: TelnetConnector) {
        clear()
        listener?.onTelnetClientConnectionFail(this)
    }

    override fun onTelnetConnectorReceiveDataStart(connector: TelnetConnector) {
        model.cleanCahcedData()
        stateHandler.handleState()
    }

    override fun onTelnetConnectorReceiveDataFinished(connector: TelnetConnector) {
        connector.cleanReadDataSize()
    }

    fun setUsername(username: String?) {
        this.username = username ?: ""
    }

    fun getUsername(): String {
        return username
    }
}
