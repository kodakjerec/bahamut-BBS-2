package com.kota.Telnet;

import android.util.Log;

import java.io.IOException;

public class TelnetConnector implements TelnetChannelListener {
    private TelnetChannel[] _channel = new TelnetChannel[2];
    private HolderThread _holder_thread = null;
    private boolean _is_connecting = false;
    private long _last_send_data_time = 0;
    private TelnetConnectorListener _listener = null;
    private TelnetSocketChannel _socket_channel = null;

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

    /* 防呆,掛網 */
    private class HolderThread extends Thread {
        private boolean _run;

        private HolderThread() {
            this._run = true;
        }

        public void close() {
            this._run = false;
        }

        public void run() {
            while (this._run && TelnetConnector.this._holder_thread == this) {
                try {
                    sleep(30 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (System.currentTimeMillis() - TelnetConnector.this._last_send_data_time > 150 * 1000) {
                    TelnetConnector.this.sendHoldMessage();
                }
            }
        }
    }

    protected void finalize() throws Throwable {
        if (isConnecting()) {
            close();
        }
        super.finalize();
    }

    public void connect(String serverIp, int serverPort) {
        if (isConnecting()) {
            close();
        }
        if (this._listener != null) {
            this._listener.onTelnetConnectorConnectStart(this);
        }
        this._is_connecting = false;
        try {
            System.out.println("Connect to " + serverIp + ":" + serverPort);
            this._socket_channel = new TelnetDefaultSocketChannel(serverIp, serverPort);
            synchronized (this) {
                this._channel[0] = new TelnetChannel(this._socket_channel);
                this._channel[0].setListener(this);
                this._channel[1] = new TelnetChannel(this._socket_channel);
                this._channel[1].setListener(this);
            }
            this._is_connecting = true;
        } catch (IOException e) {
            Log.v("SocketChannel", "Connection Fail");
            clear();
        }
        if (this._is_connecting) {
            if (this._listener != null) {
                this._listener.onTelnetConnectorConnectSuccess(this);
            }
            this._holder_thread = new HolderThread();
            this._holder_thread.start();
        } else if (this._listener != null) {
            this._listener.onTelnetConnectorConnectFail(this);
        }
    }

    public void close() {
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
                return 0;
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
        TelnetOutputBuilder.create()
                .pushData((byte) 0)
//                .pushData((byte) 27)
//                .pushData((byte) 91)
//                .pushData((byte) 65)
//                .pushData((byte) 27)
//                .pushData((byte) 91)
//                .pushData((byte) 66)
                .sendToServer();
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
