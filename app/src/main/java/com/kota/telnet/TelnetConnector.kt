package com.kota.telnet

import android.util.Log
import com.kota.asFramework.pageController.ASDeviceController
import com.kota.Bahamut.service.NotificationSettings.getConnectMethod
import java.io.IOException

class TelnetConnector : TelnetChannelListener {
    private val _channel: Array<TelnetChannel?>? = arrayOfNulls<TelnetChannel>(2)
    private var _holder_thread: HolderThread? = null
    var isConnecting: Boolean = false
        private set
    private var _last_send_data_time: Long = 0
    private var _listener: TelnetConnectorListener? = null
    private var _socket_channel: TelnetSocketChannel? = null

    // 添加設備控制器引用
    private var _device_controller: ASDeviceController? = null

    fun clear() {
        synchronized(this) {
            this._channel!![0] = null
            this._channel[1] = null
        }
        if (this._holder_thread != null) {
            this._holder_thread!!.close()
        }
        this._holder_thread = null
        if (this._socket_channel != null) {
            try {
                this._socket_channel!!.finishConnect()
            } catch (e: IOException) {
                Log.v("SocketChannel", "IO Exception")
            }
        }
        this._socket_channel = null
        this.isConnecting = false
        this._last_send_data_time = 0
    }

    // 添加設備控制器設定方法
    fun setDeviceController(deviceController: ASDeviceController?) {
        this._device_controller = deviceController
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
            while (this._run && this@TelnetConnector._holder_thread === this) {
                try {
                    sleep((30 * 1000).toLong())
                } catch (e: InterruptedException) {
                    Log.e(javaClass.getSimpleName(), (if (e.message != null) e.message else "")!!)
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
                if (currentTime - this@TelnetConnector._last_send_data_time > 150 * 1000) {
                    Log.d("TelnetConnector", "Sending keep-alive message")
                    this@TelnetConnector.sendHoldMessage()
                }


                // 添加連線健康檢查
                if (currentTime - this@TelnetConnector._last_send_data_time > 300 * 1000) {
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
            connectTelnet(serverIp, serverPort)
        }
    }

    // 原本的 Telnet 連線方法
    private fun connectTelnet(serverIp: String?, serverPort: Int) {
        if (this.isConnecting) {
            close()
        }


        // 連線前先鎖定 WiFi 和 CPU
        if (this._device_controller != null) {
            this._device_controller!!.lockWifi()
        }

        if (this._listener != null) {
            this._listener!!.onTelnetConnectorConnectStart(this)
        }
        this.isConnecting = false
        try {
            println("Connect to Telnet " + serverIp + ":" + serverPort)
            this._socket_channel = TelnetDefaultSocketChannel(serverIp, serverPort)
            synchronized(this) {
                this._channel!![0] = TelnetChannel(this._socket_channel)
                this._channel[0]!!.setListener(this)
                this._channel[1] = TelnetChannel(this._socket_channel)
                this._channel[1]!!.setListener(this)
            }
            this.isConnecting = true
            // 初始化最後發送時間
            this._last_send_data_time = System.currentTimeMillis()
        } catch (e: IOException) {
            Log.e("TelnetConnector", "Telnet connection failed: " + e.message)
            // 連線失敗時釋放鎖定
            if (this._device_controller != null) {
                this._device_controller!!.unlockWifi()
            }
            clear()
        }
        if (this.isConnecting) {
            if (this._listener != null) {
                this._listener!!.onTelnetConnectorConnectSuccess(this)
            }
            this._holder_thread = HolderThread()
            this._holder_thread!!.start()
            Log.d("TelnetConnector", "Telnet connection established, HolderThread started")
        } else if (this._listener != null) {
            this._listener!!.onTelnetConnectorConnectFail(this)
        }
    }

    // 新的 WebSocket 連線方法
    private fun connectWebSocket(serverIp: String?, serverPort: Int) {
        if (this.isConnecting) {
            close()
        }


        // 連線前先鎖定 WiFi 和 CPU
        if (this._device_controller != null) {
            this._device_controller!!.lockWifi()
        }

        if (this._listener != null) {
            this._listener!!.onTelnetConnectorConnectStart(this)
        }
        this.isConnecting = false
        try {
            // 構建 WebSocket URL - 使用巴哈姆特的實際 WebSocket 端點
            val wsUrl = "wss://term.gamer.com.tw/bbs"
            println("Connect to WebSocket " + wsUrl)
            this._socket_channel = TelnetWebSocketChannel(wsUrl)
            synchronized(this) {
                this._channel!![0] = TelnetChannel(this._socket_channel)
                this._channel[0]!!.setListener(this)
                this._channel[1] = TelnetChannel(this._socket_channel)
                this._channel[1]!!.setListener(this)
            }
            this.isConnecting = true
            // 初始化最後發送時間
            this._last_send_data_time = System.currentTimeMillis()
        } catch (e: IOException) {
            Log.e("TelnetConnector", "WebSocket connection failed: " + e.message)
            // 連線失敗時釋放鎖定
            if (this._device_controller != null) {
                this._device_controller!!.unlockWifi()
            }
            clear()
        }
        if (this.isConnecting) {
            if (this._listener != null) {
                this._listener!!.onTelnetConnectorConnectSuccess(this)
            }
            this._holder_thread = HolderThread()
            this._holder_thread!!.start()
            Log.d("TelnetConnector", "WebSocket connection established, HolderThread started")
        } else if (this._listener != null) {
            this._listener!!.onTelnetConnectorConnectFail(this)
        }
    }

    fun close() {
        // 關閉連線時釋放鎖定
        if (this._device_controller != null) {
            this._device_controller!!.unlockWifi()
        }
        clear()
        if (this._listener != null) {
            this._listener!!.onTelnetConnectorClosed(this)
        }
    }

    private fun getChannel(channel: Int): TelnetChannel? {
        val telnetChannel: TelnetChannel?
        synchronized(this) {
            if (this._channel != null) {
                telnetChannel = this._channel[channel]
            } else {
                telnetChannel = null
            }
        }
        return telnetChannel
    }

    @Throws(TelnetConnectionClosedException::class)
    fun readData(channel: Int): Byte {
        val selected_channel = getChannel(channel)
        if (selected_channel != null) {
            try {
                return selected_channel.readData()
            } catch (e: IOException) {
                Log.v("SocketChannel", "readData IO Exception")
                throw TelnetConnectionClosedException()
            }
        }
        return 0
    }

    fun undoReadData(channel: Int) {
        val selected_channel = getChannel(channel)
        if (selected_channel != null) {
            selected_channel.undoReadData()
        }
    }

    fun writeData(data: ByteArray, channel: Int) {
        val selected_channel = getChannel(channel)
        if (selected_channel != null) {
            selected_channel.writeData(data)
        }
    }

    fun writeData(data: Byte, channel: Int) {
        val selected_channel = getChannel(channel)
        if (selected_channel != null) {
            selected_channel.writeData(data)
        }
    }

    fun sendData(channel: Int) {
        val selected_channel = getChannel(channel)
        if (selected_channel != null && selected_channel.sendData()) {
            this._last_send_data_time = System.currentTimeMillis()
        }
    }

    fun lockChannel(channel: Int) {
        val selected_channel = getChannel(channel)
        if (selected_channel != null) {
            selected_channel.lock()
        }
    }

    fun unlockChannel(channel: Int) {
        val selected_channel = getChannel(channel)
        if (selected_channel != null) {
            selected_channel.unlock()
            sendData(channel)
        }
    }

    fun cleanReadDataSize() {
        val selected_channel = getChannel(0)
        if (selected_channel != null) {
            selected_channel.cleanReadDataSize()
        }
    }

    val readDataSize: Int
        get() {
            val selected_channel = getChannel(0)
            if (selected_channel != null) {
                return selected_channel.getReadDataSize()
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
            this._last_send_data_time = System.currentTimeMillis()
            Log.d("TelnetConnector", "Keep-alive message sent")
        } catch (e: Exception) {
            Log.e("TelnetConnector", "Failed to send keep-alive message: " + e.message)
            // 如果發送失敗，可能連線已斷開
            if (this._listener != null) {
                // 這裡可以觸發重連機制
            }
        }
    }

    val isConnectionHealthy: Boolean
        // 添加連線狀態檢查方法
        get() = this.isConnecting && this._socket_channel != null &&
                (System.currentTimeMillis() - this._last_send_data_time < 300 * 1000)

    // 添加網路連線檢查方法
    fun checkNetworkConnectivity(): Boolean {
        if (this._device_controller != null) {
            val networkType = this._device_controller!!.isNetworkAvailable()
            return networkType != -1
        }
        return true // 如果沒有設備控制器，假設網路正常
    }

    fun setListener(aListener: TelnetConnectorListener?) {
        this._listener = aListener
    }

    override fun onTelnetChannelReceiveDataStart(aChannel: TelnetChannel?) {
        if (this._listener != null) {
            this._listener!!.onTelnetConnectorReceiveDataStart(this)
        }
    }

    override fun onTelnetChannelReceiveDataFinished(aChannel: TelnetChannel?) {
        if (this._listener != null) {
            this._listener!!.onTelnetConnectorReceiveDataFinished(this)
        }
    }
}
