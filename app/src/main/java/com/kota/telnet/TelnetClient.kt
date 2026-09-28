package com.kota.telnet

import android.util.Log
import com.kota.telnet.model.TelnetModel
import com.kota.telnet.reference.TelnetDef
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.textEncoder.U2BEncoder
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

    /** 傳送原始 ByteArray 至伺服器（背景線程安全發送） */
    @JvmOverloads
    fun sendDataToServer(data: ByteArray?, channel: Int = 0) {
        if (data != null && telnetConnector?.isConnecting == true) {
            executorService.submit {
                telnetConnector?.writeData(data, channel)
                telnetConnector?.sendData(channel)
            }
        }
    }

    /** 傳送字串至伺服器（自動加上 \n 與 Big5 編碼） */
    @JvmOverloads
    fun sendStringToServer(str: String?, channel: Int = 0) {
        if (str == null) return
        try {
            val raw = (str + "\n").toByteArray(charset(TelnetDef.CHARSET))
            val encoded = U2BEncoder.instance!!.encodeToBytes(raw, 0)
            sendDataToServer(encoded, channel)
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, e.message ?: "sendStringToServer error")
        }
    }

    /** 傳送鍵盤按鍵 (TelnetKeyboard) 至伺服器 */
    @JvmOverloads
    fun sendKeyboardInputToServer(key: Int, channel: Int = 0) {
        sendDataToServer(TelnetKeyboard.getKeyData(key), channel)
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
