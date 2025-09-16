package com.kota.telnet

import com.kota.telnet.model.TelnetModel

class TelnetReceiver {
    private var _connector: TelnetConnector? = null
    private var _model: TelnetModel? = null
    private var _receiver_thread: TelnetReceiverThread? = null

    constructor()

    constructor(aConnector: TelnetConnector?, aModel: TelnetModel?) {
        this._connector = aConnector
        this._model = aModel
    }

    fun startReceiver() {
        this._receiver_thread = TelnetReceiverThread(this._connector, this._model)
        this._receiver_thread!!.setDaemon(true)
        this._receiver_thread!!.start()
    }

    fun stopReceiver() {
        if (this._receiver_thread != null) {
            this._receiver_thread!!.close()
            this._receiver_thread = null
        }
    }

    val isReceiving: Boolean
        get() {
            if (this._receiver_thread != null) {
                return true
            }
            return false
        }
}
