package com.kota.telnet

import android.util.Log
import com.kota.asFramework.thread.ASRunner
import com.kota.telnet.model.TelnetModel
import com.kota.telnet.reference.TelnetDef
import com.kota.telnet.reference.TelnetKeyboard.getKeyData
import com.kota.textEncoder.U2BEncoder
import java.io.UnsupportedEncodingException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TelnetClient private constructor(private val stateHandler: TelnetStateHandler) :
    TelnetConnectorListener {
    var connector: TelnetConnector? = null
        get() = client?.connector
        private set
    var model: TelnetModel? = null
        get() = client?.model
        private set
    private var _receiver: TelnetReceiver? = null
    private var myUserName: String? = null
    private var clientListener: TelnetClientListener? = null
    var sendExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        this.model = TelnetModel()
        this.connector = TelnetConnector()
        connector?.setListener(this)
        _receiver = TelnetReceiver(this.connector, this.model)
    }

    fun clear() {
        stateHandler.clear()
        connector?.clear()
        model?.clear()
        _receiver?.stopReceiver()
    }

    fun connect(serverIp: String?, serverPort: Int) {
        connector?.connect(serverIp, serverPort)
    }

    fun close() {
        try {
            connector?.close() // 關閉連線
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, (if (e.message != null) e.message else "")!!)
        }
    }

    fun sendStringToServer(str: String?) {
        if (ASRunner.isMainThread) {
            sendStringToServerInBackground(str, 0)
        } else {
            sendStringToServer(str, 0)
        }
    }

    private fun sendStringToServer(str: String?, channel: Int) {
        var data: ByteArray? = null
        var encodeSuccess = false
        try {
            val data2: ByteArray = (str + "\n").toByteArray(charset(TelnetDef.CHARSET))
            data = U2BEncoder.instance?.encodeToBytes(data2, 0)
            encodeSuccess = true
        } catch (e: UnsupportedEncodingException) {
            Log.e(javaClass.simpleName, (if (e.message != null) e.message else "")!!)
        }
        if (encodeSuccess) {
            sendDataToServer(data, channel)
        }
    }

    @JvmOverloads
    fun sendStringToServerInBackground(str: String?, channel: Int = 0) {
        var data: ByteArray? = null
        var encodeSuccess = false
        try {
            val data2: ByteArray = (str + "\n").toByteArray(charset(TelnetDef.CHARSET))
            data = U2BEncoder.instance?.encodeToBytes(data2, 0)
            encodeSuccess = true
        } catch (e: UnsupportedEncodingException) {
            Log.e(javaClass.simpleName, (if (e.message != null) e.message else "")!!)
        }
        if (encodeSuccess) {
            sendDataToServerInBackground(data, channel)
        }
    }

    fun sendKeyboardInputToServer(key: Int) {
        if (ASRunner.isMainThread) {
            sendKeyboardInputToServerInBackground(key, 0)
        } else {
            sendKeyboardInputToServer(key, 0)
        }
    }

    private fun sendKeyboardInputToServer(key: Int, channel: Int) {
        sendDataToServer(getKeyData(key), channel)
    }

    @JvmOverloads
    fun sendKeyboardInputToServerInBackground(key: Int, channel: Int = 0) {
        sendDataToServerInBackground(getKeyData(key), channel)
    }

    fun sendDataToServer(data: ByteArray?) {
        if (ASRunner.isMainThread) {
            sendDataToServerInBackground(data, 0)
        } else {
            sendDataToServer(data, 0)
        }
    }

    fun sendDataToServer(data: ByteArray?, channel: Int) {
        if (data != null && connector?.isConnecting) {
            connector?.writeData(data, channel)
            connector?.sendData(channel)
        }
    }

    fun sendDataToServerInBackground(data: ByteArray?, channel: Int) {
        if (data != null && connector?.isConnecting) {
            sendExecutor.submit {
                this@TelnetClient.connector?.writeData(data, channel)
                this@TelnetClient.connector?.sendData(channel)
            }
        }
    }

    fun setListener(aListener: TelnetClientListener?) {
        clientListener = aListener
    }

    // com.kota.telnet.TelnetConnectorListener
    override fun onTelnetConnectorConnectStart(telnetConnector: TelnetConnector?) {
        if (clientListener != null) {
            clientListener?.onTelnetClientConnectionStart(this)
        }
    }

    // com.kota.telnet.TelnetConnectorListener
    override fun onTelnetConnectorClosed(telnetConnector: TelnetConnector?) {
        clear()
        if (clientListener != null) {
            clientListener?.onTelnetClientConnectionClosed(this)
        }
    }

    // com.kota.telnet.TelnetConnectorListener
    override fun onTelnetConnectorConnectSuccess(telnetConnector: TelnetConnector?) {
        _receiver?.startReceiver()
        if (clientListener != null) {
            clientListener?.onTelnetClientConnectionSuccess(this)
        }
    }

    // com.kota.telnet.TelnetConnectorListener
    override fun onTelnetConnectorConnectFail(telnetConnector: TelnetConnector?) {
        clear()
        if (clientListener != null) {
            clientListener?.onTelnetClientConnectionFail(this)
        }
    }

    // com.kota.telnet.TelnetConnectorListener
    override fun onTelnetConnectorReceiveDataStart(telnetConnector: TelnetConnector?) {
        model?.cleanCachedData()
        stateHandler.handleState()
    }

    // com.kota.telnet.TelnetConnectorListener
    override fun onTelnetConnectorReceiveDataFinished(telnetConnector: TelnetConnector?) {
        connector?.cleanReadDataSize()
    }

    var username: String?
        get() {
            if (myUserName == null) {
                myUserName = ""
            }
            return myUserName
        }
        set(aUsername) {
            myUserName = aUsername
        }

    companion object {
        var client: TelnetClient? = null
            private set

        @JvmStatic
        fun construct(aStateHandler: TelnetStateHandler) {
            client = TelnetClient(aStateHandler)
        }
    }
}
