package com.kota.Telnet;

import com.kota.ASFramework.Thread.ASRunner;
import com.kota.DataPool.MutableByteBuffer;
import com.kota.Telnet.Reference.TelnetDefs;
import com.kota.Telnet.Reference.TelnetKeyboard;
import com.kota.TextEncoder.U2BEncoder;

public class TelnetOutputBuilder {
    private MutableByteBuffer _buffer = MutableByteBuffer.createMutableByteBuffer();

    public static TelnetOutputBuilder create() {
        return new TelnetOutputBuilder();
    }

    public void sendToServerInBackground() {
        sendToServerInBackground(0);
    }

    public void sendToServerInBackground(int channel) {
        TelnetClient.getClient().sendDataToServerInBackground(build(), channel);
    }

    public void sendToServer() {
        if (ASRunner.isMainThread()) {
            sendToServerInBackground(0);
        } else {
            sendToServer(0);
        }
    }

    private void sendToServer(int channel) {
        TelnetClient.getClient().sendDataToServer(build(), channel);
    }

    public byte[] build() {
        this._buffer.close();
        byte[] data = this._buffer.toByteArray();
        MutableByteBuffer.recycleMutableByteBuffer(this._buffer);
        return data;
    }

    public TelnetOutputBuilder pushData(byte data) {
        this._buffer.put(data);
        return this;
    }

    public void pushData(byte[] data) {
        for (byte pushData : data) {
            pushData(pushData);
        }
    }

    public TelnetOutputBuilder pushKey(int key) {
        pushData(TelnetKeyboard.getKeyData(key));
        return this;
    }

    public TelnetOutputBuilder pushString(String str) {
        try {
            pushData(U2BEncoder.getInstance().encodeToBytes(str.getBytes(TelnetDefs.CHARSET), 0));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }
}
