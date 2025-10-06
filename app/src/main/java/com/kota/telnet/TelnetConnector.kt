package com.kota.telnet

import android.util.Log
import com.kota.Bahamut.service.NotificationSettings.getConnectMethod
import com.kota.asFramework.pageController.ASDeviceController
import java.io.IOException

class TelnetConnector : TelnetChannelListener {
    private val telnetChannels: Array<TelnetChannel?> = arrayOfNulls(2)
    private var holderThread: HolderThread? = null
    var isConnecting: Boolean = false
        private set
    private var lastSentDataTime: Long = 0
    private lateinit var connectorListener: TelnetConnectorListener
    private var socketChannel: TelnetSocketChannel? = null

    // 添加設備控制器引用
    private var deviceController: ASDeviceController? = null

    fun clear() {
        synchronized(this) {
            this.telnetChannels[0] = null
            this.telnetChannels[1] = null
        }
        if (this.holderThread != null) {
            this.holderThread?.close()
        }
        this.holderThread = null
        if (this.socketChannel != null) {
            try {
                this.socketChannel?.finishConnect()
            } catch (_: IOException) {
                Log.v("SocketChannel", "IO Exception")
            }
        }
        this.socketChannel = null
        this.isConnecting = false
        this.lastSentDataTime = 0
    }

    // 添加設備控制器設定方法
    fun setDeviceController(deviceController: ASDeviceController?) {
        this.deviceController = deviceController
    }

    /* 防呆,掛網 */
    private inner class HolderThread : Thread() {
        private var _run = true

        fun close() {
            this._run = false
            // 添加中斷執行緒處理
            this.interrupt()
        }

        override fun run() {
            while (this._run && this@TelnetConnector.holderThread === this) {
                try {
                    sleep((30 * 1000).toLong())
                } catch (e: InterruptedException) {
                    Log.e(javaClass.simpleName, (if (e.message != null) e.message else "")!!)
                    // 如果被中斷且不應該繼續運行，則退出
                    if (!this._run) {
                        break
                    }
                }


                // 檢查連線狀態
                if (!this@TelnetConnector.isConnecting) {
                    Log.w("TelnetConnector", "Connection lost, breaking holder thread")
                    break
                }


                // 檢查網路連線狀態
                if (!this@TelnetConnector.checkNetworkConnectivity()) {
                    Log.w("TelnetConnector", "Network connectivity lost")
                    // 網路斷開時不立即斷線，給網路恢復的時間
                    continue
                }


                // 增強的 keep-alive 檢查
                val currentTime = System.currentTimeMillis()
                if (currentTime - this@TelnetConnector.lastSentDataTime > 150 * 1000) {
                    Log.d("TelnetConnector", "Sending keep-alive message")
                    this@TelnetConnector.sendHoldMessage()
                }


                // 添加連線健康檢查
                if (currentTime - this@TelnetConnector.lastSentDataTime > 300 * 1000) {
                    Log.w("TelnetConnector", "No data for 5 minutes, checking connection health")
                    if (!this@TelnetConnector.isConnectionHealthy) {
                        Log.e("TelnetConnector", "Connection appears to be unhealthy")
                        // 可以在這裡添加更積極的連線檢查或重連機制
                    }
                }
            }
            Log.d("TelnetConnector", "HolderThread terminated")
        }
    }

    // 移除已廢棄的 finalize 方法，改用 cleanup 方法
    fun cleanup() {
        if (this.isConnecting) {
            close()
        }
    }

    fun connect(serverIp: String?, serverPort: Int) {
        // 使用設定中的連線方式
        val connectMethod = getConnectMethod()
        if ("webSocket" == connectMethod) {
            connectWebSocket(serverIp, serverPort)
        } else {
            connectTelnet(serverIp!!, serverPort)
        }
    }

    // 原本的 Telnet 連線方法
    private fun connectTelnet(serverIp: String, serverPort: Int) {
        if (this.isConnecting) {
            close()
        }


        // 連線前先鎖定 WiFi 和 CPU
        if (this.deviceController != null) {
            this.deviceController?.lockWifi()
        }

        this.connectorListener.onTelnetConnectorConnectStart(this)
        this.isConnecting = false
        try {
            println("Connect to Telnet $serverIp:$serverPort")
            this.socketChannel = TelnetDefaultSocketChannel(serverIp, serverPort)
            synchronized(this) {
                this.telnetChannels[0] = TelnetChannel(this.socketChannel!!)
                this.telnetChannels[0]?.setListener(this)
                this.telnetChannels[1] = TelnetChannel(this.socketChannel!!)
                this.telnetChannels[1]?.setListener(this)
            }
            this.isConnecting = true
            // 初始化最後發送時間
            this.lastSentDataTime = System.currentTimeMillis()
        } catch (e: IOException) {
            Log.e("TelnetConnector", "Telnet connection failed: " + e.message)
            // 連線失敗時釋放鎖定
            if (this.deviceController != null) {
                this.deviceController?.unlockWifi()
            }
            clear()
        }
        if (this.isConnecting) {
            this.connectorListener.onTelnetConnectorConnectSuccess(this)
            this.holderThread = HolderThread()
            this.holderThread?.start()
            Log.d("TelnetConnector", "Telnet connection established, HolderThread started")
        } else {
            this.connectorListener.onTelnetConnectorConnectFail(this)
        }
    }

    // 新的 WebSocket 連線方法
    private fun connectWebSocket(serverIp: String?, serverPort: Int) {
        if (this.isConnecting) {
            close()
        }


        // 連線前先鎖定 WiFi 和 CPU
        if (this.deviceController != null) {
            this.deviceController?.lockWifi()
        }

        this.connectorListener.onTelnetConnectorConnectStart(this)
        this.isConnecting = false
        try {
            // 構建 WebSocket URL - 使用巴哈姆特的實際 WebSocket 端點
            val wsUrl = "wss://term.gamer.com.tw/bbs"
            println("Connect to WebSocket $wsUrl")
            this.socketChannel = TelnetWebSocketChannel(wsUrl)
            synchronized(this) {
                this.telnetChannels[0] = TelnetChannel(this.socketChannel!!)
                this.telnetChannels[0]?.setListener(this)
                this.telnetChannels[1] = TelnetChannel(this.socketChannel!!)
                this.telnetChannels[1]?.setListener(this)
            }
            this.isConnecting = true
            // 初始化最後發送時間
            this.lastSentDataTime = System.currentTimeMillis()
        } catch (e: IOException) {
            Log.e("TelnetConnector", "WebSocket connection failed: " + e.message)
            // 連線失敗時釋放鎖定
            if (this.deviceController != null) {
                this.deviceController?.unlockWifi()
            }
            clear()
        }
        if (this.isConnecting) {
            this.connectorListener.onTelnetConnectorConnectSuccess(this)
            this.holderThread = HolderThread()
            this.holderThread?.start()
            Log.d("TelnetConnector", "WebSocket connection established, HolderThread started")
        } else {
            this.connectorListener.onTelnetConnectorConnectFail(this)
        }
    }

    fun close() {
        // 關閉連線時釋放鎖定
        if (this.deviceController != null) {
            this.deviceController?.unlockWifi()
        }
        clear()
        this.connectorListener.onTelnetConnectorClosed(this)
    }

    private fun getChannel(channel: Int): TelnetChannel? {
        val telnetChannel: TelnetChannel?
        synchronized(this) {
            telnetChannel = this.telnetChannels[channel]
        }
        return telnetChannel
    }

    @Throws(TelnetConnectionClosedException::class)
    fun readData(channel: Int): Byte {
        val selectedChannel = getChannel(channel)
        if (selectedChannel != null) {
            try {
                return selectedChannel.readData()
            } catch (_: IOException) {
                Log.v("SocketChannel", "readData IO Exception")
                throw TelnetConnectionClosedException
            }
        }
        return 0
    }

    fun undoReadData(channel: Int) {
        val selectedChannel = getChannel(channel)
        selectedChannel?.undoReadData()
    }

    fun writeData(data: ByteArray, channel: Int) {
        val selectedChannel = getChannel(channel)
        selectedChannel?.writeData(data)
    }

    fun writeData(data: Byte, channel: Int) {
        val selectedChannel = getChannel(channel)
        selectedChannel?.writeData(data)
    }

    fun sendData(channel: Int) {
        val selectedChannel = getChannel(channel)
        if (selectedChannel != null && selectedChannel.sendData()) {
            this.lastSentDataTime = System.currentTimeMillis()
        }
    }

    fun lockChannel(channel: Int) {
        val selectedChannel = getChannel(channel)
        selectedChannel?.lock()
    }

    fun unlockChannel(channel: Int) {
        val selectedChannel = getChannel(channel)
        if (selectedChannel != null) {
            selectedChannel.unlock()
            sendData(channel)
        }
    }

    fun cleanReadDataSize() {
        val selectedChannel = getChannel(0)
        selectedChannel?.cleanReadDataSize()
    }

    val readDataSize: Int
        get() {
            val selectedChannel = getChannel(0)
            if (selectedChannel != null) {
                return selectedChannel.readDataSize
            }
            return 0
        }

    private fun sendHoldMessage() {
        try {
            TelnetOutputBuilder.Companion.create()
                .pushData(0.toByte()) //                .pushData((byte) 27)
                //                .pushData((byte) 91)
                //                .pushData((byte) 65)
                //                .pushData((byte) 27)
                //                .pushData((byte) 91)
                //                .pushData((byte) 66)
                .sendToServer()


            // 更新最後發送時間
            this.lastSentDataTime = System.currentTimeMillis()
            Log.d("TelnetConnector", "Keep-alive message sent")
        } catch (e: Exception) {
            Log.e("TelnetConnector", "Failed to send keep-alive message: " + e.message)
            // 如果發送失敗，可能連線已斷開
            // 這裡可以觸發重連機制
        }
    }

    val isConnectionHealthy: Boolean
        // 添加連線狀態檢查方法
        get() = this.isConnecting && this.socketChannel != null &&
                (System.currentTimeMillis() - this.lastSentDataTime < 300 * 1000)

    // 添加網路連線檢查方法
    fun checkNetworkConnectivity(): Boolean {
        if (this.deviceController != null) {
            val networkType = this.deviceController?.isNetworkAvailable
            return networkType != -1
        }
        return true // 如果沒有設備控制器，假設網路正常
    }

    fun setListener(aListener: TelnetConnectorListener) {
        this.connectorListener = aListener
    }

    override fun onTelnetChannelReceiveDataStart(telnetChannel: TelnetChannel) {
        this.connectorListener.onTelnetConnectorReceiveDataStart(this)
    }

    override fun onTelnetChannelReceiveDataFinished(telnetChannel: TelnetChannel) {
        this.connectorListener.onTelnetConnectorReceiveDataFinished(this)
    }
}
