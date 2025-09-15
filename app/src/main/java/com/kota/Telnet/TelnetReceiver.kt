package com.kota.Telnet

import com.kota.Telnet.Model.TelnetModel

class TelnetReceiver {
    private var connector: TelnetConnector? = null
    private var model: TelnetModel? = null
    private var receiverThread: TelnetReceiverThread? = null

    constructor()

    constructor(connector: TelnetConnector, model: TelnetModel) {
        this.connector = connector
        this.model = model
    }

    fun startReceiver() {
        receiverThread = TelnetReceiverThread(connector, model)
        receiverThread?.isDaemon = true
        receiverThread?.start()
    }

    fun stopReceiver() {
        receiverThread?.let {
            it.close()
            receiverThread = null
        }
    }

    fun isReceiving(): Boolean {
        return receiverThread != null
    }
}
