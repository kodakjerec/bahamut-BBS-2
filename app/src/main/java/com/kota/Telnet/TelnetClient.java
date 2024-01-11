package com.kota.Telnet;

import com.kota.Bahamut.SelfFramework.Thread.ASRunner;
import com.kota.Telnet.Model.TelnetModel;
import com.kota.Telnet.Reference.TelnetDefs;
import com.kota.Telnet.Reference.TelnetKeyboard;
import com.kota.TextEncoder.U2BEncoder;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TelnetClient implements TelnetConnectorListener {
    private static TelnetClient _instance = null;
    /* access modifiers changed from: private */
    public TelnetConnector _connector = null;
    private TelnetClientListener _listener = null;
    private TelnetModel _model = null;
    private TelnetReceiver _receiver = null;
    ExecutorService _send_executor = Executors.newSingleThreadExecutor();
    private TelnetStateHandler _state_handler = null;
    private String _username;

    public static void construct(TelnetStateHandler aStateHandler) {
        _instance = new TelnetClient(aStateHandler);
    }

    public static TelnetClient getClient() {
        return _instance;
    }

    public static TelnetConnector getConnector() {
        return getClient()._connector;
    }

    public static TelnetModel getModel() {
        return getClient()._model;
    }

    private TelnetClient(TelnetStateHandler aStateHandler) {
        this._state_handler = aStateHandler;
        this._model = new TelnetModel();
        this._connector = new TelnetConnector();
        this._connector.setListener(this);
        this._receiver = new TelnetReceiver(this._connector, this._model);
    }

    public void clear() {
        this._state_handler.clear();
        this._connector.clear();
        this._model.clear();
        this._receiver.stopReceiver();
    }

    public void connect(String serverIp, int serverPort) {
        this._connector.connect(serverIp, serverPort);
    }

    public void close() {
        try {
            this._connector.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void sendStringToServer(String str) {
        if (ASRunner.isMainThread()) {
            // Running on the main thread
            sendStringToServerInBackground(str, 0);
        } else {
            // Not running on the main thread
            sendStringToServer(str, 0);
        }
    }

    private void sendStringToServer(String str, int channel) {
        byte[] data = null;
        boolean encode_success = false;
        try {
            data = U2BEncoder.getInstance().encodeToBytes((str + "\n").getBytes(TelnetDefs.CHARSET), 0);
            encode_success = true;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (encode_success) {
            sendDataToServer(data, channel);
        }
    }

    public void sendStringToServerInBackground(String str) {
        sendStringToServerInBackground(str, 0);
    }

    public void sendStringToServerInBackground(String str, int channel) {
        byte[] data = null;
        boolean encode_success = false;
        try {
            data = U2BEncoder.getInstance().encodeToBytes((str + "\n").getBytes(TelnetDefs.CHARSET), 0);
            encode_success = true;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (encode_success) {
            sendDataToServerInBackground(data, channel);
        }
    }

    public void sendKeyboardInputToServer(int key) {
        if (ASRunner.isMainThread()) {
            sendKeyboardInputToServerInBackground(key, 0);
        } else {
            sendKeyboardInputToServer(key, 0);
        }
    }

    private void sendKeyboardInputToServer(int key, int channel) {
        sendDataToServer(TelnetKeyboard.getKeyData(key), channel);
    }

    public void sendKeyboardInputToServerInBackground(int key) {
        sendKeyboardInputToServerInBackground(key, 0);
    }

    public void sendKeyboardInputToServerInBackground(int key, int channel) {
        sendDataToServerInBackground(TelnetKeyboard.getKeyData(key), channel);
    }

    public void sendDataToServer(byte[] data) {
        if (ASRunner.isMainThread()) {
            sendDataToServerInBackground(data, 0);
        } else {
            sendDataToServer(data, 0);
        }
    }

    /* access modifiers changed from: protected */
    public void sendDataToServer(byte[] data, int channel) {
        if (data != null && this._connector.isConnecting()) {
            this._connector.writeData(data, channel);
            this._connector.sendData(channel);
        }
    }

    public void sendDataToServerInBackground(byte[] data) {
        sendDataToServerInBackground(data, 0);
    }

    public void sendDataToServerInBackground(final byte[] data, final int channel) {
        if (data != null && this._connector.isConnecting()) {
            this._send_executor.submit(new Runnable() {
                public void run() {
                    TelnetClient.this._connector.writeData(data, channel);
                    TelnetClient.this._connector.sendData(channel);
                }
            });
        }
    }

    public void setListener(TelnetClientListener aListener) {
        this._listener = aListener;
    }

    public void onTelnetConnectorConnectStart(TelnetConnector aConnector) {
        if (this._listener != null) {
            this._listener.onTelnetClientConnectionStart(this);
        }
    }

    public void onTelnetConnectorClosed(TelnetConnector aConnector) {
        clear();
        if (this._listener != null) {
            this._listener.onTelnetClientConnectionClosed(this);
        }
    }

    public void onTelnetConnectorConnectSuccess(TelnetConnector aConnector) {
        this._receiver.startReceiver();
        if (this._listener != null) {
            this._listener.onTelnetClientConnectionSuccess(this);
        }
    }

    public void onTelnetConnectorConnectFail(TelnetConnector aConnector) {
        clear();
        if (this._listener != null) {
            this._listener.onTelnetClientConnectionFail(this);
        }
    }

    public void onTelnetConnectorReceiveDataStart(TelnetConnector aConnector) {
        if (this._state_handler != null) {
            this._model.cleanCahcedData();
            this._state_handler.handleState();
        }
    }

    public void onTelnetConnectorReceiveDataFinished(TelnetConnector aConnector) {
        this._connector.cleanReadDataSize();
    }

    public void setUsername(String aUsername) {
        this._username = aUsername;
    }

    public String getUsername() {
        if (this._username == null) {
            this._username = "";
        }
        return this._username;
    }
}
