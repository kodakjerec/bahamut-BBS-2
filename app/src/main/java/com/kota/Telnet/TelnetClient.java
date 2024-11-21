package com.kota.Telnet;

import android.util.Log;

import com.kota.ASFramework.Thread.ASRunner;
import com.kota.Bahamut.DataModels.UrlDatabase;
import com.kota.Bahamut.MyApplication;
import com.kota.Bahamut.Service.TempSettings;
import com.kota.Telnet.Model.TelnetModel;
import com.kota.Telnet.Reference.TelnetDefs;
import com.kota.Telnet.Reference.TelnetKeyboard;
import com.kota.TextEncoder.U2BEncoder;
import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* loaded from: classes.dex */
public class TelnetClient implements TelnetConnectorListener {
    private static TelnetClient _instance = null;
    private TelnetConnector _connector;
    private TelnetModel _model;
    private TelnetReceiver _receiver;
    private final TelnetStateHandler _state_handler;
    private String _username;
    private TelnetClientListener _listener = null;
    ExecutorService _send_executor = Executors.newSingleThreadExecutor();

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
        _connector = null;
        _receiver = null;
        _model = null;
        _state_handler = aStateHandler;
        _model = new TelnetModel();
        _connector = new TelnetConnector();
        _connector.setListener(this);
        _receiver = new TelnetReceiver(_connector, _model);
    }

    public void clear() {
        _state_handler.clear();
        _connector.clear();
        _model.clear();
        _receiver.stopReceiver();
    }

    public void connect(String serverIp, int serverPort) {
        _connector.connect(serverIp, serverPort);
    }

    public void close() {
        try {
            _connector.close(); // 關閉連線
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), Objects.requireNonNull(e.getMessage()));
        }
    }

    public void sendStringToServer(String str) {
        if (ASRunner.isMainThread()) {
            sendStringToServerInBackground(str, 0);
        } else {
            sendStringToServer(str, 0);
        }
    }

    private void sendStringToServer(String str, int channel) {
        byte[] data = null;
        boolean encode_success = false;
        try {
            byte[] data2 = (str + "\n").getBytes(TelnetDefs.CHARSET);
            data = U2BEncoder.getInstance().encodeToBytes(data2, 0);
            encode_success = true;
        } catch (UnsupportedEncodingException e) {
            Log.e(getClass().getSimpleName(), Objects.requireNonNull(e.getMessage()));
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
            byte[] data2 = (str + "\n").getBytes(TelnetDefs.CHARSET);
            data = U2BEncoder.getInstance().encodeToBytes(data2, 0);
            encode_success = true;
        } catch (UnsupportedEncodingException e) {
            Log.e(getClass().getSimpleName(), Objects.requireNonNull(e.getMessage()));
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

    protected void sendDataToServer(byte[] data, int channel) {
        if (data != null && _connector.isConnecting()) {
            _connector.writeData(data, channel);
            _connector.sendData(channel);
        }
    }

    public void sendDataToServerInBackground(final byte[] data, final int channel) {
        if (data != null && _connector.isConnecting()) {
            _send_executor.submit(() -> {
                TelnetClient.this._connector.writeData(data, channel);
                TelnetClient.this._connector.sendData(channel);
            });
        }
    }

    public void setListener(TelnetClientListener aListener) {
        _listener = aListener;
    }

    @Override // com.kota.Telnet.TelnetConnectorListener
    public void onTelnetConnectorConnectStart(TelnetConnector aConnector) {
        if (_listener != null) {
            _listener.onTelnetClientConnectionStart(this);
        }
    }

    @Override // com.kota.Telnet.TelnetConnectorListener
    public void onTelnetConnectorClosed(TelnetConnector aConnector) {
        clear();
        if (_listener != null) {
            _listener.onTelnetClientConnectionClosed(this);
        }
    }

    @Override // com.kota.Telnet.TelnetConnectorListener
    public void onTelnetConnectorConnectSuccess(TelnetConnector aConnector) {
        _receiver.startReceiver();
        if (_listener != null) {
            _listener.onTelnetClientConnectionSuccess(this);
        }
    }

    @Override // com.kota.Telnet.TelnetConnectorListener
    public void onTelnetConnectorConnectFail(TelnetConnector aConnector) {
        clear();
        if (_listener != null) {
            _listener.onTelnetClientConnectionFail(this);
        }
    }

    @Override // com.kota.Telnet.TelnetConnectorListener
    public void onTelnetConnectorReceiveDataStart(TelnetConnector aConnector) {
        if (_state_handler != null) {
            _model.cleanCahcedData();
            _state_handler.handleState();
        }
    }

    @Override // com.kota.Telnet.TelnetConnectorListener
    public void onTelnetConnectorReceiveDataFinished(TelnetConnector aConnector) {
        _connector.cleanReadDataSize();
    }

    public void setUsername(String aUsername) {
        _username = aUsername;
    }

    public String getUsername() {
        if (_username == null) {
            _username = "";
        }
        return _username;
    }
}
