package com.kota.Telnet;

import android.util.Log;
import com.kota.ASFramework.PageController.ASDeviceController;
import com.kota.Bahamut.Service.NotificationSettings;

import java.io.IOException;
import java.util.Objects;

public class TelnetConnector implements TelnetChannelListener {
    private TelnetChannel[] _channel = new TelnetChannel[2];
    private HolderThread _holder_thread = null;
    private boolean _is_connecting = false;
    private long _last_send_data_time = 0;
    private TelnetConnectorListener _listener = null;
    private TelnetSocketChannel _socket_channel = null;
    // 添加設備控制器引用
    private ASDeviceController _device_controller = null;

    public void clear() {
        synchronized (this) {
            this._channel[0] = null;
            this._channel[1] = null;
        }
        if (this._holder_thread != null) {
            this._holder_thread.close();
        }
        this._holder_thread = null;
        if (this._socket_channel != null) {
            try {
                this._socket_channel.finishConnect();
            } catch (IOException e) {
                Log.v("SocketChannel", "IO Exception");
            }
        }
        this._socket_channel = null;
        this._is_connecting = false;
        this._last_send_data_time = 0;
    }

    // 添加設備控制器設定方法
    public void setDeviceController(ASDeviceController deviceController) {
        this._device_controller = deviceController;
    }

    /* 防呆,掛網 */
    private class HolderThread extends Thread {
        private boolean _run;

        private HolderThread() {
            this._run = true;
        }

        public void close() {
            this._run = false;
            // 添加中斷執行緒處理
            this.interrupt();
        }

        public void run() {
            while (this._run && TelnetConnector.this._holder_thread == this) {
                try {
                    sleep(30 * 1000);
                } catch (InterruptedException e) {
                    Log.e(getClass().getSimpleName(), e.getMessage()!=null?e.getMessage():"");
                    // 如果被中斷且不應該繼續運行，則退出
                    if (!this._run) {
                        break;
                    }
                }
                
                // 檢查連線狀態
                if (!TelnetConnector.this._is_connecting) {
                    Log.w("TelnetConnector", "Connection lost, breaking holder thread");
                    break;
                }
                
                // 檢查網路連線狀態
                if (!TelnetConnector.this.checkNetworkConnectivity()) {
                    Log.w("TelnetConnector", "Network connectivity lost");
                    // 網路斷開時不立即斷線，給網路恢復的時間
                    continue;
                }
                
                // 增強的 keep-alive 檢查
                long currentTime = System.currentTimeMillis();
                if (currentTime - TelnetConnector.this._last_send_data_time > 150 * 1000) {
                    Log.d("TelnetConnector", "Sending keep-alive message");
                    TelnetConnector.this.sendHoldMessage();
                }
                
                // 添加連線健康檢查
                if (currentTime - TelnetConnector.this._last_send_data_time > 300 * 1000) {
                    Log.w("TelnetConnector", "No data for 5 minutes, checking connection health");
                    if (!TelnetConnector.this.isConnectionHealthy()) {
                        Log.e("TelnetConnector", "Connection appears to be unhealthy");
                        // 可以在這裡添加更積極的連線檢查或重連機制
                    }
                }
            }
            Log.d("TelnetConnector", "HolderThread terminated");
        }
    }

    // 移除已廢棄的 finalize 方法，改用 cleanup 方法
    public void cleanup() {
        if (isConnecting()) {
            close();
        }
    }

    public void connect(String serverIp, int serverPort) {
        // 使用設定中的連線方式
        String connectMethod = NotificationSettings.getConnectMethod();
        if ("webSocket".equals(connectMethod)) {
            connectWebSocket(serverIp, serverPort);
        } else {
            connectTelnet(serverIp, serverPort);
        }
    }

    // 原本的 Telnet 連線方法
    private void connectTelnet(String serverIp, int serverPort) {
        if (isConnecting()) {
            close();
        }
        
        // 連線前先鎖定 WiFi 和 CPU
        if (this._device_controller != null) {
            this._device_controller.lockWifi();
        }
        
        if (this._listener != null) {
            this._listener.onTelnetConnectorConnectStart(this);
        }
        this._is_connecting = false;
        try {
            System.out.println("Connect to Telnet " + serverIp + ":" + serverPort);
            this._socket_channel = new TelnetDefaultSocketChannel(serverIp, serverPort);
            synchronized (this) {
                this._channel[0] = new TelnetChannel(this._socket_channel);
                this._channel[0].setListener(this);
                this._channel[1] = new TelnetChannel(this._socket_channel);
                this._channel[1].setListener(this);
            }
            this._is_connecting = true;
            // 初始化最後發送時間
            this._last_send_data_time = System.currentTimeMillis();
        } catch (IOException e) {
            Log.e("TelnetConnector", "Telnet connection failed: " + e.getMessage());
            // 連線失敗時釋放鎖定
            if (this._device_controller != null) {
                this._device_controller.unlockWifi();
            }
            clear();
        }
        if (this._is_connecting) {
            if (this._listener != null) {
                this._listener.onTelnetConnectorConnectSuccess(this);
            }
            this._holder_thread = new HolderThread();
            this._holder_thread.start();
            Log.d("TelnetConnector", "Telnet connection established, HolderThread started");
        } else if (this._listener != null) {
            this._listener.onTelnetConnectorConnectFail(this);
        }
    }

    // 新的 WebSocket 連線方法
    private void connectWebSocket(String serverIp, int serverPort) {
        if (isConnecting()) {
            close();
        }
        
        // 連線前先鎖定 WiFi 和 CPU
        if (this._device_controller != null) {
            this._device_controller.lockWifi();
        }
        
        if (this._listener != null) {
            this._listener.onTelnetConnectorConnectStart(this);
        }
        this._is_connecting = false;
        try {
            // 構建 WebSocket URL - 使用巴哈姆特的實際 WebSocket 端點
            String wsUrl = "wss://term.gamer.com.tw/bbs";
            System.out.println("Connect to WebSocket " + wsUrl);
            this._socket_channel = new TelnetWebSocketChannel(wsUrl);
            synchronized (this) {
                this._channel[0] = new TelnetChannel(this._socket_channel);
                this._channel[0].setListener(this);
                this._channel[1] = new TelnetChannel(this._socket_channel);
                this._channel[1].setListener(this);
            }
            this._is_connecting = true;
            // 初始化最後發送時間
            this._last_send_data_time = System.currentTimeMillis();
        } catch (IOException e) {
            Log.e("TelnetConnector", "WebSocket connection failed: " + e.getMessage());
            // 連線失敗時釋放鎖定
            if (this._device_controller != null) {
                this._device_controller.unlockWifi();
            }
            clear();
        }
        if (this._is_connecting) {
            if (this._listener != null) {
                this._listener.onTelnetConnectorConnectSuccess(this);
            }
            this._holder_thread = new HolderThread();
            this._holder_thread.start();
            Log.d("TelnetConnector", "WebSocket connection established, HolderThread started");
        } else if (this._listener != null) {
            this._listener.onTelnetConnectorConnectFail(this);
        }
    }

    public void close() {
        // 關閉連線時釋放鎖定
        if (this._device_controller != null) {
            this._device_controller.unlockWifi();
        }
        clear();
        if (this._listener != null) {
            this._listener.onTelnetConnectorClosed(this);
        }
    }

    private TelnetChannel getChannel(int channel) {
        TelnetChannel telnetChannel;
        synchronized (this) {
            if (this._channel != null) {
                telnetChannel = this._channel[channel];
            } else {
                telnetChannel = null;
            }
        }
        return telnetChannel;
    }

    public byte readData(int channel) throws TelnetConnectionClosedException {
        TelnetChannel selected_channel = getChannel(channel);
        if (selected_channel != null) {
            try {
                return selected_channel.readData();
            } catch (IOException e) {
                Log.v("SocketChannel", "readData IO Exception");
                throw new TelnetConnectionClosedException();
            }
        }
        return 0;
    }

    public void undoReadData(int channel) {
        TelnetChannel selected_channel = getChannel(channel);
        if (selected_channel != null) {
            selected_channel.undoReadData();
        }
    }

    public void writeData(byte[] data, int channel) {
        TelnetChannel selected_channel = getChannel(channel);
        if (selected_channel != null) {
            selected_channel.writeData(data);
        }
    }

    public void writeData(byte data, int channel) {
        TelnetChannel selected_channel = getChannel(channel);
        if (selected_channel != null) {
            selected_channel.writeData(data);
        }
    }

    public void sendData(int channel) {
        TelnetChannel selected_channel = getChannel(channel);
        if (selected_channel != null && selected_channel.sendData()) {
            this._last_send_data_time = System.currentTimeMillis();
        }
    }

    public void lockChannel(int channel) {
        TelnetChannel selected_channel = getChannel(channel);
        if (selected_channel != null) {
            selected_channel.lock();
        }
    }

    public void unlockChannel(int channel) {
        TelnetChannel selected_channel = getChannel(channel);
        if (selected_channel != null) {
            selected_channel.unlock();
            sendData(channel);
        }
    }

    public boolean isConnecting() {
        return this._is_connecting;
    }

    public void cleanReadDataSize() {
        TelnetChannel selected_channel = getChannel(0);
        if (selected_channel != null) {
            selected_channel.cleanReadDataSize();
        }
    }

    public int getReadDataSize() {
        TelnetChannel selected_channel = getChannel(0);
        if (selected_channel != null) {
            return selected_channel.getReadDataSize();
        }
        return 0;
    }

    private void sendHoldMessage() {
        try {
            TelnetOutputBuilder.create()
                    .pushData((byte) 0)
    //                .pushData((byte) 27)
    //                .pushData((byte) 91)
    //                .pushData((byte) 65)
    //                .pushData((byte) 27)
    //                .pushData((byte) 91)
    //                .pushData((byte) 66)
                    .sendToServer();
            
            // 更新最後發送時間
            this._last_send_data_time = System.currentTimeMillis();
            Log.d("TelnetConnector", "Keep-alive message sent");
        } catch (Exception e) {
            Log.e("TelnetConnector", "Failed to send keep-alive message: " + e.getMessage());
            // 如果發送失敗，可能連線已斷開
            if (this._listener != null) {
                // 這裡可以觸發重連機制
            }
        }
    }

    // 添加連線狀態檢查方法
    public boolean isConnectionHealthy() {
        return this._is_connecting && 
               this._socket_channel != null && 
               (System.currentTimeMillis() - this._last_send_data_time < 300 * 1000);
    }

    // 添加網路連線檢查方法
    public boolean checkNetworkConnectivity() {
        if (this._device_controller != null) {
            int networkType = this._device_controller.isNetworkAvailable();
            return networkType != -1;
        }
        return true; // 如果沒有設備控制器，假設網路正常
    }

    public void setListener(TelnetConnectorListener aListener) {
        this._listener = aListener;
    }

    public void onTelnetChannelReceiveDataStart(TelnetChannel aChannel) {
        if (this._listener != null) {
            this._listener.onTelnetConnectorReceiveDataStart(this);
        }
    }

    public void onTelnetChannelReceiveDataFinished(TelnetChannel aChannel) {
        if (this._listener != null) {
            this._listener.onTelnetConnectorReceiveDataFinished(this);
        }
    }
}
