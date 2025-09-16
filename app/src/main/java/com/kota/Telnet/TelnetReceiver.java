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
        // Stop existing receiver if it's running
        if (this._receiver_thread != null && this._receiver_thread.isAlive()) {
            stopReceiver();
        }
        
        // Create and start new receiver thread
        this._receiver_thread = new TelnetReceiverThread(this._connector, this._model);
        this._receiver_thread.setDaemon(true);
        this._receiver_thread.start();
    }

    public void stopReceiver() {
        if (this._receiver_thread != null) {
            this._receiver_thread.close();
            try {
                // Wait for the thread to finish, but don't wait indefinitely
                this._receiver_thread.join(1000); // Wait up to 1 second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupt status
            }
            this._receiver_thread = null;
        }
    }

    public boolean isReceiving() {
        return this._receiver_thread != null && this._receiver_thread.isAlive();
    }
}
