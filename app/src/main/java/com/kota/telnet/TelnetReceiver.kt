package com.kota.telnet

import com.kota.telnet.model.TelnetModel

class TelnetReceiver {
    private var telnetConnector: TelnetConnector? = null
    private var telnetModel: TelnetModel? = null
    private var receiverThread: TelnetReceiverThread? = null

    constructor(aConnector: TelnetConnector?, aModel: TelnetModel?) {
        this.telnetConnector = aConnector
        this.telnetModel = aModel
    }

    fun startReceiver() {
        this.receiverThread = TelnetReceiverThread(this.telnetConnector, this.telnetModel)
        this.receiverThread?.isDaemon = true
        this.receiverThread?.start()
    }

    fun stopReceiver() {
        if (this.receiverThread != null) {
            this.receiverThread?.close()
            this.receiverThread = null
        }
    }

    val isReceiving: Boolean
        get() {
            return this.receiverThread != null
        }
}
