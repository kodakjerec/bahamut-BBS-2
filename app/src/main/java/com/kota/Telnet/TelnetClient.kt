package com.kota.Telnet

import android.util.Log
import com.kota.ASFramework.Thread.ASRunner
import com.kota.Telnet.Model.TelnetModel
import com.kota.Telnet.Reference.TelnetDefs
import com.kota.Telnet.Reference.TelnetKeyboard.getKeyData
import com.kota.TextEncoder.U2BEncoder
import java.io.UnsupportedEncodingException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/* loaded from: classes.dex */
class TelnetClient private constructor(private val _state_handler: TelnetStateHandler?) :
    TelnetConnectorListener {
    var connector: TelnetConnector? = null
        get() = client.field
        private set
    var model: TelnetModel? = null
        get() = client.field
        private set
    private var _receiver: TelnetReceiver? = null
    private var _username: String? = null
    private var _listener: TelnetClientListener? = null
    var _send_executor: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        this.model = TelnetModel()
        this.connector = TelnetConnector()
        connector!!.setListener(this)
        _receiver = TelnetReceiver(this.connector, this.model)
    }

    fun clear() {
        _state_handler!!.clear()
        connector!!.clear()
        model!!.clear()
        _receiver!!.stopReceiver()
    }

    fun connect(serverIp: String?, serverPort: Int) {
        connector!!.connect(serverIp, serverPort)
    }

    fun close() {
        try {
            connector!!.close() // 關閉連線
        } catch (e: Exception) {
            Log.e(javaClass.getSimpleName(), (if (e.message != null) e.message else "")!!)
        }
    }

    fun sendStringToServer(str: String?) {
        if (ASRunner.isMainThread()) {
            sendStringToServerInBackground(str, 0)
        } else {
            sendStringToServer(str, 0)
        }
    }

    private fun sendStringToServer(str: String?, channel: Int) {
        var data: ByteArray? = null
        var encode_success = false
        try {
            val data2: ByteArray? = (str + "\n").toByteArray(charset(TelnetDefs.CHARSET))
            data = U2BEncoder.getInstance().encodeToBytes(data2, 0)
            encode_success = true
        } catch (e: UnsupportedEncodingException) {
            Log.e(javaClass.getSimpleName(), (if (e.message != null) e.message else "")!!)
        }
        if (encode_success) {
            sendDataToServer(data, channel)
        }
    }

    @JvmOverloads
    fun sendStringToServerInBackground(str: String?, channel: Int = 0) {
        var data: ByteArray? = null
        var encode_success = false
        try {
            val data2: ByteArray? = (str + "\n").toByteArray(charset(TelnetDefs.CHARSET))
            data = U2BEncoder.getInstance().encodeToBytes(data2, 0)
            encode_success = true
        } catch (e: UnsupportedEncodingException) {
            Log.e(javaClass.getSimpleName(), (if (e.message != null) e.message else "")!!)
        }
        if (encode_success) {
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
        sendDataToServer(getKeyData(key), channel)
    }

    @JvmOverloads
    fun sendKeyboardInputToServerInBackground(key: Int, channel: Int = 0) {
        sendDataToServerInBackground(getKeyData(key), channel)
    }

    fun sendDataToServer(data: ByteArray?) {
        if (ASRunner.isMainThread()) {
            sendDataToServerInBackground(data, 0)
        } else {
            sendDataToServer(data, 0)
        }
    }

    fun sendDataToServer(data: ByteArray?, channel: Int) {
        if (data != null && connector!!.isConnecting()) {
            connector!!.writeData(data, channel)
            connector!!.sendData(channel)
        }
    }

    fun sendDataToServerInBackground(data: ByteArray?, channel: Int) {
        if (data != null && connector!!.isConnecting()) {
            _send_executor.submit(Runnable {
                this@TelnetClient.connector!!.writeData(data, channel)
                this@TelnetClient.connector!!.sendData(channel)
            })
        }
    }

    fun setListener(aListener: TelnetClientListener?) {
        _listener = aListener
    }

    // com.kota.Telnet.TelnetConnectorListener
    override fun onTelnetConnectorConnectStart(aConnector: TelnetConnector?) {
        if (_listener != null) {
            _listener!!.onTelnetClientConnectionStart(this)
        }
    }

    // com.kota.Telnet.TelnetConnectorListener
    override fun onTelnetConnectorClosed(aConnector: TelnetConnector?) {
        clear()
        if (_listener != null) {
            _listener!!.onTelnetClientConnectionClosed(this)
        }
    }

    // com.kota.Telnet.TelnetConnectorListener
    override fun onTelnetConnectorConnectSuccess(aConnector: TelnetConnector?) {
        _receiver!!.startReceiver()
        if (_listener != null) {
            _listener!!.onTelnetClientConnectionSuccess(this)
        }
    }

    // com.kota.Telnet.TelnetConnectorListener
    override fun onTelnetConnectorConnectFail(aConnector: TelnetConnector?) {
        clear()
        if (_listener != null) {
            _listener!!.onTelnetClientConnectionFail(this)
        }
    }

    // com.kota.Telnet.TelnetConnectorListener
    override fun onTelnetConnectorReceiveDataStart(aConnector: TelnetConnector?) {
        if (_state_handler != null) {
            model!!.cleanCahcedData()
            _state_handler.handleState()
        }
    }

    // com.kota.Telnet.TelnetConnectorListener
    override fun onTelnetConnectorReceiveDataFinished(aConnector: TelnetConnector?) {
        connector!!.cleanReadDataSize()
    }

    var username: String?
        get() {
            if (_username == null) {
                _username = ""
            }
            return _username
        }
        set(aUsername) {
            _username = aUsername
        }

    companion object {
        var client: TelnetClient? = null
            private set

        @JvmStatic
        fun construct(aStateHandler: TelnetStateHandler?) {
            client = TelnetClient(aStateHandler)
        }
    }
}
