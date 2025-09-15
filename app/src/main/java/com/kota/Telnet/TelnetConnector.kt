package com.kota.Telnet

import android.util.Log
import com.kota.ASFramework.PageController.ASDeviceController
import com.kota.Bahamut.Service.NotificationSettings
import java.io.IOException

class TelnetConnector : TelnetChannelListener {
    private val channel = arrayOfNulls<TelnetChannel>(2)
    private var holderThread: HolderThread? = null
    private var isConnecting = false
    private var lastSendDataTime: Long = 0
    private var listener: TelnetConnectorListener? = null
    private var socketChannel: TelnetSocketChannel? = null
    private var deviceController: ASDeviceController? = null

    fun clear() {
        synchronized(this) {
            channel[0] = null
            channel[1] = null
        }
        holderThread?.close()
        holderThread = null
        socketChannel?.let {
            try {
                it.finishConnect()
            } catch (e: IOException) {
                Log.v("SocketChannel", "IO Exception")
            }
        }
        socketChannel = null
        isConnecting = false
        lastSendDataTime = 0
    }

    fun setDeviceController(deviceController: ASDeviceController?) {
        this.deviceController = deviceController
    }

    /* 防呆,掛網 */
    private inner class HolderThread : Thread() {
        @Volatile
        private var run = true

        fun close() {
            run = false
            interrupt()
        }

        override fun run() {
            while (run && this@TelnetConnector.holderThread === this) {
                try {
                    sleep(30 * 1000)
                } catch (e: InterruptedException) {
                    Log.e(javaClass.simpleName, e.message ?: "")
                    if (!run) {
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
                    continue
                }

                // 增強的 keep-alive 檢查
                val currentTime = System.currentTimeMillis()
                if (currentTime - this@TelnetConnector.lastSendDataTime > 150 * 1000) {
                    Log.d("TelnetConnector", "Sending keep-alive message")
                    this@TelnetConnector.sendHoldMessage()
                }

                // 添加連線健康檢查
                if (currentTime - this@TelnetConnector.lastSendDataTime > 300 * 1000) {
                    Log.w("TelnetConnector", "No data for 5 minutes, checking connection health")
                    if (!this@TelnetConnector.isConnectionHealthy()) {
                        Log.e("TelnetConnector", "Connection appears to be unhealthy")
                    }
                }
            }
            Log.d("TelnetConnector", "HolderThread terminated")
        }
    }

    fun cleanup() {
        if (isConnecting()) {
            close()
        }
    }

    fun connect(serverIp: String, serverPort: Int) {
        // 使用設定中的連線方式
        val connectMethod = NotificationSettings.getConnectMethod()
        if ("webSocket" == connectMethod) {
            connectWebSocket(serverIp, serverPort)
        } else {
            connectTelnet(serverIp, serverPort)
        }
    }

    // 原本的 Telnet 連線方法
    private fun connectTelnet(serverIp: String, serverPort: Int) {
        if (isConnecting()) {
            close()
        }

        // 連線前先鎖定 WiFi 和 CPU
        deviceController?.lockWifi()

        listener?.onTelnetConnectorConnectStart(this)
        isConnecting = false
        try {
            println("Connect to Telnet $serverIp:$serverPort")
            socketChannel = TelnetDefaultSocketChannel(serverIp, serverPort)
            synchronized(this) {
                channel[0] = TelnetChannel(socketChannel!!)
                channel[0]?.setListener(this)
                channel[1] = TelnetChannel(socketChannel!!)
                channel[1]?.setListener(this)
            }
            isConnecting = true
            // 初始化最後發送時間
            lastSendDataTime = System.currentTimeMillis()
        } catch (e: IOException) {
            Log.e("TelnetConnector", "Telnet connection failed: ${e.message}")
            // 連線失敗時釋放鎖定
            deviceController?.unlockWifi()
            clear()
        }
        if (isConnecting) {
            listener?.onTelnetConnectorConnectSuccess(this)
            holderThread = HolderThread()
            holderThread?.start()
            Log.d("TelnetConnector", "Telnet connection established, HolderThread started")
        } else {
            listener?.onTelnetConnectorConnectFail(this)
        }
    }

    // 新的 WebSocket 連線方法
    private fun connectWebSocket(serverIp: String, serverPort: Int) {
        if (isConnecting()) {
            close()
        }

        // 連線前先鎖定 WiFi 和 CPU
        deviceController?.lockWifi()

        listener?.onTelnetConnectorConnectStart(this)
        isConnecting = false
        try {
            // 構建 WebSocket URL - 使用巴哈姆特的實際 WebSocket 端點
            val wsUrl = "wss://term.gamer.com.tw/bbs"
            println("Connect to WebSocket $wsUrl")
            socketChannel = TelnetWebSocketChannel(wsUrl)
            synchronized(this) {
                channel[0] = TelnetChannel(socketChannel!!)
                channel[0]?.setListener(this)
                channel[1] = TelnetChannel(socketChannel!!)
                channel[1]?.setListener(this)
            }
            isConnecting = true
            // 初始化最後發送時間
            lastSendDataTime = System.currentTimeMillis()
        } catch (e: IOException) {
            Log.e("TelnetConnector", "WebSocket connection failed: ${e.message}")
            // 連線失敗時釋放鎖定
            deviceController?.unlockWifi()
            clear()
        }
        if (isConnecting) {
            listener?.onTelnetConnectorConnectSuccess(this)
            holderThread = HolderThread()
            holderThread?.start()
            Log.d("TelnetConnector", "WebSocket connection established, HolderThread started")
        } else {
            listener?.onTelnetConnectorConnectFail(this)
        }
    }

    fun close() {
        // 關閉連線時釋放鎖定
        deviceController?.unlockWifi()
        clear()
        listener?.onTelnetConnectorClosed(this)
    }

    private fun getChannel(channel: Int): TelnetChannel? {
        return synchronized(this) {
            this.channel.takeIf { it != null }?.get(channel)
        }
    }

    @Throws(TelnetConnectionClosedException::class)
    fun readData(channel: Int): Byte {
        val selectedChannel = getChannel(channel)
        if (selectedChannel != null) {
            try {
                return selectedChannel.readData()
            } catch (e: IOException) {
                Log.v("SocketChannel", "readData IO Exception")
                throw TelnetConnectionClosedException()
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
            lastSendDataTime = System.currentTimeMillis()
        }
    }

    fun lockChannel(channel: Int) {
        val selectedChannel = getChannel(channel)
        selectedChannel?.lock()
    }

    fun unlockChannel(channel: Int) {
        val selectedChannel = getChannel(channel)
        selectedChannel?.unlock()
        sendData(channel)
    }

    fun isConnecting(): Boolean {
        return isConnecting
    }

    fun cleanReadDataSize() {
        val selectedChannel = getChannel(0)
        selectedChannel?.cleanReadDataSize()
    }

    fun getReadDataSize(): Int {
        val selectedChannel = getChannel(0)
        return selectedChannel?.getReadDataSize() ?: 0
    }

    private fun sendHoldMessage() {
        try {
            TelnetOutputBuilder.create()
                .pushData(0.toByte())
                .sendToServer()

            // 更新最後發送時間
            lastSendDataTime = System.currentTimeMillis()
            Log.d("TelnetConnector", "Keep-alive message sent")
        } catch (e: Exception) {
            Log.e("TelnetConnector", "Failed to send keep-alive message: ${e.message}")
        }
    }

    // 添加連線狀態檢查方法
    fun isConnectionHealthy(): Boolean {
        return isConnecting &&
                socketChannel != null &&
                (System.currentTimeMillis() - lastSendDataTime < 300 * 1000)
    }

    // 添加網路連線檢查方法
    fun checkNetworkConnectivity(): Boolean {
        deviceController?.let {
            val networkType = it.isNetworkAvailable()
            return networkType != -1
        }
        return true // 如果沒有設備控制器，假設網路正常
    }

    fun setListener(listener: TelnetConnectorListener?) {
        this.listener = listener
    }

    override fun onTelnetChannelReceiveDataStart(channel: TelnetChannel) {
        listener?.onTelnetConnectorReceiveDataStart(this)
    }

    override fun onTelnetChannelReceiveDataFinished(channel: TelnetChannel) {
        listener?.onTelnetConnectorReceiveDataFinished(this)
    }
}
