package com.kota.Telnet

import android.util.Log
import com.kota.Bahamut.BuildConfig
import com.kota.Telnet.Model.TelnetModel
import com.kota.Telnet.Model.TelnetModel.pushData
import java.io.IOException

class TelnetReceiverThread(aConnector: TelnetConnector?, aModel: TelnetModel?) : Thread() {
    private val _command = TelnetCommand()
    private var _connector: TelnetConnector? = null
    private var _model: TelnetModel? = null
    private var _receiving = true

    init {
        this._connector = aConnector
        this._model = aModel
    }

    fun close() {
        this._receiving = false
        this._connector = null
        this._model = null
    }

    /* JADX WARNING: Removed duplicated region for block: B:0:0x0000 A[LOOP:0: B:0:0x0000->B:3:0x0008, LOOP_START, MTH_ENTER_BLOCK] */ /* Code decompiled incorrectly, please refer to instructions dump. */
    override fun run() {
        do {
        } while (this._receiving && receiveData())
    }

    @Throws(TelnetConnectionClosedException::class, IOException::class)
    private fun readData(): Byte {
        return this._connector!!.readData(0)
    }

    private fun receiveData(): Boolean {
        var result = true
        try {
            val data = readData()
            if (data.toInt() == -1) {
                val action = readData()
                val option = readData()
                this._command.header = data
                this._command.action = action
                this._command.option = option
                handleCommand()
            } else if (data.toInt() == 13) {
                this._model!!.moveCursorColumnToBegin()
            } else if (data.toInt() == 10) {
                this._model!!.moveCursorToNextLine()
            } else if (data.toInt() == 7) {
                println("get BEL")
            } else if (data.toInt() == 8) {
                this._model!!.moveCursorColumnLeft()
            } else if (data.toInt() != 27) {
                this._model!!.pushData(data)
            } else if (readData().toInt() == 91) {
                this._model!!.cleanAnsiBuffer()
                var data2 = readData()
                this._model!!.pushAnsiBuffer(data2)
                while (true) {
                    if ((data2 < 48 || data2 > 57) && data2.toInt() != 59) {
                        break
                    }
                    data2 = readData()
                    this._model!!.pushAnsiBuffer(data2)
                }
                this._model!!.parseAnsiBuffer()
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e(
                javaClass.getSimpleName(),
                (if (e.message != null) e.message else "")!!
            )

            Log.v("SocketChannel", "receiveData Exception")
            result = false
            if (this._connector != null && this._connector!!.isConnecting()) {
                this._connector!!.close()
            }
        }
        return result
    }

    @Throws(TelnetConnectionClosedException::class, IOException::class)
    fun handleCommand() {
        if (this._command.isEqualTo(-1, -3, 37)) {
            sendCommandToServer(-1, -4, 37)
        } else if (this._command.isEqualTo(-1, -5, 1)) {
            sendCommandToServer(-1, -2, 1)
        } else if (this._command.isEqualTo(-1, -3, 1)) {
            sendCommandToServer(-1, -4, 1)
        } else if (this._command.isEqualTo(-1, -5, 3)) {
            sendCommandToServer(-1, -2, 3)
        } else if (this._command.isEqualTo(-1, -3, 39)) {
            sendCommandToServer(-1, -4, 39)
        } else if (this._command.isEqualTo(-1, -3, 31)) {
            sendCommandToServer(-1, -4, 31)
        } else if (this._command.isEqualTo(-1, -3, 0)) {
            sendCommandToServer(-1, -4, 0)
        } else if (this._command.isEqualTo(-1, -5, 0)) {
            sendCommandToServer(-1, -2, 0)
        } else if (this._command.isEqualTo(-1, -3, 24)) {
            sendCommandToServer(-1, -5, 24)
        } else if (this._command.isEqualTo(-1, -3, 0)) {
            sendCommandToServer(-1, -4, 0)
        } else if (this._command.isEqualTo(-1, -6, 24)) {
            TelnetClient.Companion.getConnector().readData(0)
            TelnetClient.Companion.getConnector().readData(0)
            TelnetClient.Companion.getConnector().readData(0)
            TelnetOutputBuilder.Companion.create().pushData(-1.toByte()).pushData(-6.toByte())
                .pushData(
                    TelnetCommand.Companion.TERMINAL_TYPE as Byte
                ).pushData(0.toByte()).pushData(65.toByte()).pushData(78.toByte())
                .pushData(83.toByte()).pushData(73.toByte()).pushData(-1.toByte()).pushData(
                    TelnetCommand.Companion.SE as Byte
                ).sendToServer()
        } else {
            println("[MuTelnetCommandHandler]Unimplement command : " + this._command.toString())
        }
    }

    /* access modifiers changed from: package-private */
    fun sendCommandToServer(aHeader: Int, aAction: Int, aOption: Int) {
        TelnetOutputBuilder.Companion.create().pushData(aHeader.toByte()).pushData(aAction.toByte())
            .pushData(aOption.toByte()).sendToServer()
    }

    companion object {
        val UNSET: Int = -1
    }
}
