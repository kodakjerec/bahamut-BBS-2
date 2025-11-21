package com.kota.telnet

import android.util.Log
import com.kota.asFramework.thread.ASRunner
import com.kota.telnet.model.TelnetModel
import com.kota.telnet.reference.TelnetDef
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.textEncoder.U2BEncoder
import java.io.UnsupportedEncodingException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TelnetClient(aStateHandler: TelnetStateHandler) : TelnetConnectorListener {
    var telnetConnector: TelnetConnector? = null
    private var _model: TelnetModel? = null
    private var _receiver: TelnetReceiver? = null
    private var _state_handler: TelnetStateHandler? = null
    private var _username: String? = null
    private var _listener: TelnetClientListener? = null
    var _send_executor: ExecutorService = Executors.newSingleThreadExecutor()

    // 初始化塊 - 在主構造器之後執行
    init {
        _state_handler = aStateHandler
        _model = TelnetModel()
        telnetConnector = TelnetConnector()
        telnetConnector?.setListener(this)
        _receiver = TelnetReceiver(telnetConnector, _model)
    }

    fun clear() {
        _state_handler!!.clear()
        telnetConnector!!.clear()
        _model!!.clear()
        _receiver!!.stopReceiver()
    }

    fun connect(serverIp: String?, serverPort: Int) {
        telnetConnector!!.connect(serverIp, serverPort)
    }

    fun close() {
        try {
            telnetConnector!!.close() // 關閉連線
        } catch (e: Exception) {
            Log.e(javaClass.getSimpleName(), (if (e.message != null) e.message else "")!!)
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
        var encode_success = false
        try {
            val data2: ByteArray = (str + "\n").toByteArray(charset(TelnetDef.CHARSET))
            data = U2BEncoder.instance!!.encodeToBytes(data2, 0)
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
            val data2: ByteArray = (str + "\n").toByteArray(charset(TelnetDef.CHARSET))
            data = U2BEncoder.instance!!.encodeToBytes(data2, 0)
            encode_success = true
        } catch (e: UnsupportedEncodingException) {
            Log.e(javaClass.getSimpleName(), (if (e.message != null) e.message else "")!!)
        }
        if (encode_success) {
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
        sendDataToServer(TelnetKeyboard.getKeyData(key), channel)
    }

    @JvmOverloads
    fun sendKeyboardInputToServerInBackground(key: Int, channel: Int = 0) {
        sendDataToServerInBackground(TelnetKeyboard.getKeyData(key), channel)
    }

    fun sendDataToServer(data: ByteArray?) {
        if (ASRunner.isMainThread) {
            sendDataToServerInBackground(data, 0)
        } else {
            sendDataToServer(data, 0)
        }
    }

    fun sendDataToServer(data: ByteArray?, channel: Int) {
        if (data != null && telnetConnector!!.isConnecting) {
            telnetConnector!!.writeData(data, channel)
            telnetConnector!!.sendData(channel)
        }
    }

    fun sendDataToServerInBackground(data: ByteArray?, channel: Int) {
        if (data != null && telnetConnector!!.isConnecting) {
            _send_executor.submit(Runnable {
                this@TelnetClient.telnetConnector!!.writeData(data, channel)
                this@TelnetClient.telnetConnector!!.sendData(channel)
            })
        }
    }

    fun setListener(aListener: TelnetClientListener?) {
        _listener = aListener
    }

    // com.kota.Telnet.TelnetConnectorListener
    public override fun onTelnetConnectorConnectStart(telnetConnector: TelnetConnector) {
        _listener?.onTelnetClientConnectionStart(this)
    }

    // com.kota.Telnet.TelnetConnectorListener
    public override fun onTelnetConnectorClosed(telnetConnector: TelnetConnector) {
        clear()
        _listener?.onTelnetClientConnectionClosed(this)
    }

    // com.kota.Telnet.TelnetConnectorListener
    public override fun onTelnetConnectorConnectSuccess(telnetConnector: TelnetConnector) {
        _receiver!!.startReceiver()
        _listener?.onTelnetClientConnectionSuccess(this)
    }

    // com.kota.Telnet.TelnetConnectorListener
    public override fun onTelnetConnectorConnectFail(telnetConnector: TelnetConnector) {
        clear()
        _listener?.onTelnetClientConnectionFail(this)
    }

    // com.kota.Telnet.TelnetConnectorListener
    override fun onTelnetConnectorReceiveDataStart(telnetConnector: TelnetConnector) {
        if (_state_handler != null) {
            _model!!.cleanCachedData()
            _state_handler!!.handleState()
        }
    }

    // com.kota.Telnet.TelnetConnectorListener
    public override fun onTelnetConnectorReceiveDataFinished(telnetConnector: TelnetConnector) {
        this@TelnetClient.telnetConnector!!.cleanReadDataSize()
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
        var myInstance: TelnetClient? = null
        fun construct(aStateHandler: TelnetStateHandler) {
            myInstance = TelnetClient(aStateHandler)
        }

        val client: TelnetClient?
            get() = myInstance

        val connector: TelnetConnector
            get() = client!!.telnetConnector!!

        val model: TelnetModel
            get() = client!!._model!!
    }
}
