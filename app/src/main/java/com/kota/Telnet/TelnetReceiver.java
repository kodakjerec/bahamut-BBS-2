package com.kota.Telnet;

import com.kota.Telnet.Model.TelnetModel;

public class TelnetReceiver {
    private TelnetConnector _connector = null;
    private TelnetModel _model = null;
    private TelnetReceiverThread _receiver_thread = null;

    public TelnetReceiver() {
    }

    public TelnetReceiver(TelnetConnector aConnector, TelnetModel aModel) {
        this._connector = aConnector;
        this._model = aModel;
    }

    public void startReceiver() {
        this._receiver_thread = new TelnetReceiverThread(this._connector, this._model);
        this._receiver_thread.setDaemon(true);
        this._receiver_thread.start();
    }

    public void stopReceiver() {
        if (this._receiver_thread != null) {
            this._receiver_thread.close();
            this._receiver_thread = null;
        }
    }

    public boolean isReceiving() {
        if (this._receiver_thread != null) {
            return true;
        }
        return false;
    }
}
