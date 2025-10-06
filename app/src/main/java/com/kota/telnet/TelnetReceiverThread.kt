package com.kota.telnet

import android.util.Log
import com.kota.Bahamut.BuildConfig
import com.kota.telnet.model.TelnetModel
import java.io.IOException

class TelnetReceiverThread(aConnector: TelnetConnector, aModel: TelnetModel) : Thread() {
    private val telnetCommand = TelnetCommand()
    private var telnetConnector: TelnetConnector? = null
    private var telnetModel: TelnetModel? = null
    private var isReceiving = true

    init {
        this.telnetConnector = aConnector
        this.telnetModel = aModel
    }

    fun close() {
        this.isReceiving = false
        this.telnetConnector = null
        this.telnetModel = null
    }

    override fun run() {
        do {
        } while (this.isReceiving && receiveData())
    }

    @Throws(TelnetConnectionClosedException::class, IOException::class)
    private fun readData(): Byte {
        return this.telnetConnector?.readData(0)!!
    }

    private fun receiveData(): Boolean {
        var result = true
        try {
            val data = readData()
            if (data.toInt() == -1) {
                val action = readData()
                val option = readData()
                this.telnetCommand.header = data
                this.telnetCommand.action = action
                this.telnetCommand.option = option
                handleCommand()
            } else if (data.toInt() == 13) {
                this.telnetModel?.moveCursorColumnToBegin()
            } else if (data.toInt() == 10) {
                this.telnetModel?.moveCursorToNextLine()
            } else if (data.toInt() == 7) {
                println("get BEL")
            } else if (data.toInt() == 8) {
                this.telnetModel?.moveCursorColumnLeft()
            } else if (data.toInt() != 27) {
                this.telnetModel?.pushData(data)
            } else if (readData().toInt() == 91) {
                this.telnetModel?.cleanAnsiBuffer()
                var data2 = readData()
                this.telnetModel?.pushAnsiBuffer(data2)
                while (true) {
                    if ((data2 < 48 || data2 > 57) && data2.toInt() != 59) {
                        break
                    }
                    data2 = readData()
                    this.telnetModel?.pushAnsiBuffer(data2)
                }
                this.telnetModel?.parseAnsiBuffer()
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e(
                javaClass.simpleName,
                (if (e.message != null) e.message else "")!!
            )

            Log.v("SocketChannel", "receiveData Exception")
            result = false
            if (this.telnetConnector != null && this.telnetConnector!!.isConnecting) {
                this.telnetConnector?.close()
            }
        }
        return result
    }

    @Throws(TelnetConnectionClosedException::class, IOException::class)
    fun handleCommand() {
        if (this.telnetCommand.isEqualTo(-1, -3, 37)) {
            sendCommandToServer(-1, -4, 37)
        } else if (this.telnetCommand.isEqualTo(-1, -5, 1)) {
            sendCommandToServer(-1, -2, 1)
        } else if (this.telnetCommand.isEqualTo(-1, -3, 1)) {
            sendCommandToServer(-1, -4, 1)
        } else if (this.telnetCommand.isEqualTo(-1, -5, 3)) {
            sendCommandToServer(-1, -2, 3)
        } else if (this.telnetCommand.isEqualTo(-1, -3, 39)) {
            sendCommandToServer(-1, -4, 39)
        } else if (this.telnetCommand.isEqualTo(-1, -3, 31)) {
            sendCommandToServer(-1, -4, 31)
        } else if (this.telnetCommand.isEqualTo(-1, -3, 0)) {
            sendCommandToServer(-1, -4, 0)
        } else if (this.telnetCommand.isEqualTo(-1, -5, 0)) {
            sendCommandToServer(-1, -2, 0)
        } else if (this.telnetCommand.isEqualTo(-1, -3, 24)) {
            sendCommandToServer(-1, -5, 24)
        } else if (this.telnetCommand.isEqualTo(-1, -3, 0)) {
            sendCommandToServer(-1, -4, 0)
        } else if (this.telnetCommand.isEqualTo(-1, -6, 24)) {
            this.telnetConnector?.readData(0)
            this.telnetConnector?.readData(0)
            this.telnetConnector?.readData(0)
            TelnetOutputBuilder.create().pushData((-1).toByte()).pushData((-6).toByte())
                .pushData(TelnetCommand.TERMINAL_TYPE).pushData(0.toByte())
                .pushData(65.toByte()).pushData(78.toByte())
                .pushData(83.toByte()).pushData(73.toByte()).pushData((-1).toByte())
                .pushData(TelnetCommand.SE).sendToServer()
        } else {
            println("[MuTelnetCommandHandler]Unimplement command : " + this.telnetCommand.toString())
        }
    }

    /* access modifiers changed from: package-private */
    fun sendCommandToServer(aHeader: Int, aAction: Int, aOption: Int) {
        TelnetOutputBuilder.create().pushData(aHeader.toByte()).pushData(aAction.toByte())
            .pushData(aOption.toByte()).sendToServer()
    }

    companion object {
        const val UNSET: Int = -1
    }
}
