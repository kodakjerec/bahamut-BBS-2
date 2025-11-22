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
    private var telnetModel: TelnetModel? = null
    private var telnetReceiver: TelnetReceiver? = null
    private var stateHandler: TelnetStateHandler? = null
    private var myUsername: String = ""
    private var telnetClientListener: TelnetClientListener? = null
    var executorService: ExecutorService = Executors.newSingleThreadExecutor()

    // 初始化塊 - 在主構造器之後執行
    init {
        stateHandler = aStateHandler
        telnetModel = TelnetModel()
        telnetConnector = TelnetConnector()
        telnetConnector?.setListener(this)
        telnetReceiver = TelnetReceiver(telnetConnector, telnetModel)
    }

    fun clear() {
        stateHandler!!.clear()
        telnetConnector!!.clear()
        telnetModel!!.clear()
        telnetReceiver!!.stopReceiver()
    }

    fun connect(serverIp: String?, serverPort: Int) {
        telnetConnector!!.connect(serverIp, serverPort)
    }

    fun close() {
        try {
            telnetConnector!!.close() // 關閉連線
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
            data = U2BEncoder.instance!!.encodeToBytes(data2, 0)
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
            data = U2BEncoder.instance!!.encodeToBytes(data2, 0)
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
            executorService.submit {
                this@TelnetClient.telnetConnector!!.writeData(data, channel)
                this@TelnetClient.telnetConnector!!.sendData(channel)
            }
        }
    }

    fun setListener(aListener: TelnetClientListener?) {
        telnetClientListener = aListener
    }

    // com.kota.Telnet.TelnetConnectorListener
    override fun onTelnetConnectorConnectStart(telnetConnector: TelnetConnector) {
        telnetClientListener?.onTelnetClientConnectionStart(this)
    }

    // com.kota.Telnet.TelnetConnectorListener
    override fun onTelnetConnectorClosed(telnetConnector: TelnetConnector) {
        clear()
        telnetClientListener?.onTelnetClientConnectionClosed(this)
    }

    // com.kota.Telnet.TelnetConnectorListener
    override fun onTelnetConnectorConnectSuccess(telnetConnector: TelnetConnector) {
        telnetReceiver!!.startReceiver()
        telnetClientListener?.onTelnetClientConnectionSuccess(this)
    }

    // com.kota.Telnet.TelnetConnectorListener
    override fun onTelnetConnectorConnectFail(telnetConnector: TelnetConnector) {
        clear()
        telnetClientListener?.onTelnetClientConnectionFail(this)
    }

    // com.kota.Telnet.TelnetConnectorListener
    override fun onTelnetConnectorReceiveDataStart(telnetConnector: TelnetConnector) {
        if (stateHandler != null) {
            telnetModel!!.cleanCachedData()
            stateHandler!!.handleState()
        }
    }

    // com.kota.Telnet.TelnetConnectorListener
    override fun onTelnetConnectorReceiveDataFinished(telnetConnector: TelnetConnector) {
        this@TelnetClient.telnetConnector!!.cleanReadDataSize()
    }

    var username: String
        get() {
            return myUsername
        }
        set(aUsername) {
            myUsername = aUsername
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
            get() = client!!.telnetModel!!
    }
}
