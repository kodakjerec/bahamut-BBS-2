package com.kota.Telnet;

import android.util.Log;
import okhttp3.*;
import okio.ByteString;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.io.IOException;

public class TelnetWebSocketChannel implements TelnetSocketChannel {
    private WebSocket webSocket;
    private OkHttpClient client;
    private boolean isConnected = false;
    private final Object readLock = new Object();
    private ByteBuffer pendingData;
    private CountDownLatch connectLatch;

    public TelnetWebSocketChannel(String serverUrl) throws IOException {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)  // Increased from 10 to 30 seconds
                .readTimeout(30, TimeUnit.SECONDS)     // Set reasonable read timeout
                .writeTimeout(30, TimeUnit.SECONDS)    // Set write timeout
                .callTimeout(60, TimeUnit.SECONDS)     // Overall call timeout
                .retryOnConnectionFailure(true)        // Enable retry on connection failure
                .build();

        connectLatch = new CountDownLatch(1);
        pendingData = ByteBuffer.allocate(8192);

        Request.Builder requestBuilder = new Request.Builder()
                .url(serverUrl);

        // 如果是巴哈姆特的 WebSocket，添加特定的標頭
        requestBuilder
                .addHeader("Origin", "https://term.gamer.com.tw")
                .addHeader("Sec-WebSocket-Key", "OYYH/aLfG4nMS1p4+EAS7A==");

        Request request = requestBuilder.build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                isConnected = true;
                connectLatch.countDown();
                Log.d(getClass().getSimpleName(), "Connected to " + serverUrl);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                // 處理文字訊息，轉換為字節
                synchronized (readLock) {
                    byte[] textBytes = text.getBytes();
                    if (pendingData.remaining() >= textBytes.length) {
                        pendingData.put(textBytes);
                    }
                    readLock.notifyAll();
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                synchronized (readLock) {
                    if (pendingData.remaining() >= bytes.size()) {
                        pendingData.put(bytes.toByteArray());
                    }
                    readLock.notifyAll();
                }
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                isConnected = false;
                synchronized (readLock) {
                    readLock.notifyAll();
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                isConnected = false;
                connectLatch.countDown();
                
                // 更詳細的錯誤日誌記錄
                if (t instanceof java.net.SocketTimeoutException) {
                    Log.e(getClass().getSimpleName(), "Socket timeout exception: " + t.getMessage());
                } else if (t instanceof java.net.ConnectException) {
                    Log.e(getClass().getSimpleName(), "Connection exception: " + t.getMessage());
                } else if (t instanceof java.net.UnknownHostException) {
                    Log.e(getClass().getSimpleName(), "Unknown host exception: " + t.getMessage());
                } else if (t instanceof IOException) {
                    Log.e(getClass().getSimpleName(), "IO exception: " + t.getMessage());
                } else {
                    Log.e(getClass().getSimpleName(), "Connection failed: " + t.getClass().getSimpleName() + " - " + t.getMessage());
                }
                
                if (response != null) {
                    Log.e(getClass().getSimpleName(), "Response code: " + response.code() + ", message: " + response.message());
                }
                
                synchronized (readLock) {
                    readLock.notifyAll();
                }
            }
        });

        // 等待連線建立 - 增加等待時間以配合更長的超時設定
        try {
            if (!connectLatch.await(30, TimeUnit.SECONDS)) {
                // 清理資源
                if (webSocket != null) {
                    webSocket.close(1000, "Connection timeout");
                }
                throw new IOException("WebSocket connection timeout after 30 seconds");
            }
        } catch (InterruptedException e) {
            // 清理資源
            if (webSocket != null) {
                webSocket.close(1000, "Connection interrupted");
            }
            Thread.currentThread().interrupt(); // 恢復中斷狀態
            throw new IOException("WebSocket connection interrupted");
        }

        if (!isConnected) {
            // 清理資源
            if (webSocket != null) {
                webSocket.close(1000, "Connection failed");
            }
            throw new IOException("WebSocket connection failed");
        }
    }

    @Override
    public int read(ByteBuffer buffer) throws IOException {
        if (!isConnected) {
            throw new IOException("WebSocket not connected");
        }

        synchronized (readLock) {
            // 如果沒有數據，等待
            while (pendingData.position() == 0 && isConnected) {
                try {
                    readLock.wait(1000); // 1秒超時
                } catch (InterruptedException e) {
                    throw new IOException("Read interrupted");
                }
            }

            if (!isConnected) {
                throw new IOException("WebSocket disconnected");
            }

            if (pendingData.position() == 0) {
                return 0; // 超時但連線還在
            }

            // 複製數據到輸出緩衝區
            pendingData.flip();
            int bytesToRead = Math.min(buffer.remaining(), pendingData.remaining());
            byte[] temp = new byte[bytesToRead];
            pendingData.get(temp);
            buffer.put(temp);

            // 如果還有剩餘數據，保留到下次
            if (pendingData.hasRemaining()) {
                byte[] remaining = new byte[pendingData.remaining()];
                pendingData.get(remaining);
                pendingData.clear();
                pendingData.put(remaining);
            } else {
                pendingData.clear();
            }

            return bytesToRead;
        }
    }

    @Override
    public int write(ByteBuffer buffer) throws IOException {
        if (!isConnected) {
            throw new IOException("WebSocket not connected");
        }

        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);

        if (webSocket.send(ByteString.of(data))) {
            return data.length;
        } else {
            throw new IOException("Failed to send data");
        }
    }

    @Override
    public boolean finishConnect() throws IOException {
        if (webSocket != null) {
            webSocket.close(1000, "Normal closure");
            isConnected = false;
        }
        if (client != null) {
            client.dispatcher().executorService().shutdown();
        }
        return true;
    }

    public boolean isConnected() {
        return isConnected;
    }
}
