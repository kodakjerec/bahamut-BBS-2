package com.kota.Telnet

import android.util.Log
import okhttp3.*
import okio.ByteString
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class TelnetWebSocketChannel @Throws(IOException::class) constructor(
    serverUrl: String
) : TelnetSocketChannel {
    
    private var webSocket: WebSocket? = null
    private var client: OkHttpClient? = null
    private var isConnected = false
    private val readLock = Any()
    private var pendingData: ByteBuffer = ByteBuffer.allocate(8192)
    private var connectLatch: CountDownLatch = CountDownLatch(1)

    init {
        client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS) // No timeout for reading
            .build()

        val requestBuilder = Request.Builder()
            .url(serverUrl)

        // 如果是巴哈姆特的 WebSocket，添加特定的標頭
        requestBuilder
            .addHeader("Origin", "https://term.gamer.com.tw")
            .addHeader("Sec-WebSocket-Key", "OYYH/aLfG4nMS1p4+EAS7A==")

        val request = requestBuilder.build()

        webSocket = client?.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected = true
                connectLatch.countDown()
                Log.d("WebSocket", "Connected to $serverUrl")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                // 處理文字訊息，轉換為字節
                synchronized(readLock) {
                    val textBytes = text.toByteArray()
                    if (pendingData.remaining() >= textBytes.size) {
                        pendingData.put(textBytes)
                    }
                    (readLock as Object).notifyAll()
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                synchronized(readLock) {
                    if (pendingData.remaining() >= bytes.size()) {
                        pendingData.put(bytes.toByteArray())
                    }
                    (readLock as Object).notifyAll()
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                synchronized(readLock) {
                    (readLock as Object).notifyAll()
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                connectLatch.countDown()
                Log.e("WebSocket", "Connection failed: ${t.message}")
                synchronized(readLock) {
                    (readLock as Object).notifyAll()
                }
            }
        })

        // 等待連線建立
        try {
            if (!connectLatch.await(10, TimeUnit.SECONDS)) {
                throw IOException("WebSocket connection timeout")
            }
        } catch (e: InterruptedException) {
            throw IOException("WebSocket connection interrupted")
        }

        if (!isConnected) {
            throw IOException("WebSocket connection failed")
        }
    }

    @Throws(IOException::class)
    override fun read(buffer: ByteBuffer): Int {
        if (!isConnected) {
            throw IOException("WebSocket not connected")
        }

        synchronized(readLock) {
            // 如果沒有數據，等待
            while (pendingData.position() == 0 && isConnected) {
                try {
                    (readLock as Object).wait(1000) // 1秒超時
                } catch (e: InterruptedException) {
                    throw IOException("Read interrupted")
                }
            }

            if (!isConnected) {
                throw IOException("WebSocket disconnected")
            }

            if (pendingData.position() == 0) {
                return 0 // 超時但連線還在
            }

            // 複製數據到輸出緩衝區
            pendingData.flip()
            val bytesToRead = minOf(buffer.remaining(), pendingData.remaining())
            val temp = ByteArray(bytesToRead)
            pendingData.get(temp)
            buffer.put(temp)

            // 如果還有剩餘數據，保留到下次
            if (pendingData.hasRemaining()) {
                val remaining = ByteArray(pendingData.remaining())
                pendingData.get(remaining)
                pendingData.clear()
                pendingData.put(remaining)
            } else {
                pendingData.clear()
            }

            return bytesToRead
        }
    }

    @Throws(IOException::class)
    override fun write(buffer: ByteBuffer): Int {
        if (!isConnected) {
            throw IOException("WebSocket not connected")
        }

        val data = ByteArray(buffer.remaining())
        buffer.get(data)

        return if (webSocket?.send(ByteString.of(*data)) == true) {
            data.size
        } else {
            throw IOException("Failed to send data")
        }
    }

    @Throws(IOException::class)
    override fun finishConnect(): Boolean {
        webSocket?.close(1000, "Normal closure")
        isConnected = false
        client?.dispatcher()?.executorService()?.shutdown()
        return true
    }

    fun isConnected(): Boolean = isConnected
}
