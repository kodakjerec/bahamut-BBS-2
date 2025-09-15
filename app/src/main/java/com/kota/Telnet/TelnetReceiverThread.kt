package com.kota.Telnet

import android.util.Log
import com.kota.Bahamut.BuildConfig
import com.kota.Telnet.Model.TelnetModel
import java.io.IOException

class TelnetReceiverThread(
    private var connector: TelnetConnector?,
    private var model: TelnetModel?
) : Thread() {
    
    companion object {
        const val UNSET = -1
    }
    
    private val command = TelnetCommand()
    private var receiving = true

    fun close() {
        receiving = false
        connector = null
        model = null
    }

    override fun run() {
        while (receiving && receiveData()) {
            // Continue receiving data
        }
    }

    @Throws(TelnetConnectionClosedException::class, IOException::class)
    private fun readData(): Byte {
        return connector?.readData(0) ?: throw IOException("Connector is null")
    }

    private fun receiveData(): Boolean {
        var result = true
        try {
            val data = readData()
            when {
                data == (-1).toByte() -> {
                    val action = readData()
                    val option = readData()
                    command.header = data
                    command.action = action
                    command.option = option
                    handleCommand()
                }
                data == 13.toByte() -> {
                    model?.moveCursorColumnToBegin()
                }
                data == 10.toByte() -> {
                    model?.moveCursorToNextLine()
                }
                data == 7.toByte() -> {
                    println("get BEL")
                }
                data == 8.toByte() -> {
                    model?.moveCursorColumnLeft()
                }
                data == 27.toByte() -> {
                    if (readData() == 91.toByte()) {
                        model?.cleanAnsiBuffer()
                        var data2 = readData()
                        model?.pushAnsiBuffer(data2)
                        while ((data2 in 48..57 || data2 == 59.toByte())) {
                            data2 = readData()
                            model?.pushAnsiBuffer(data2)
                        }
                        model?.parseAnsiBuffer()
                    }
                }
                else -> {
                    model?.pushData(data)
                }
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e(javaClass.simpleName, e.message ?: "")
            }
            Log.v("SocketChannel", "receiveData Exception")
            result = false
            if (connector?.isConnecting() == true) {
                connector?.close()
            }
        }
        return result
    }

    @Throws(TelnetConnectionClosedException::class, IOException::class)
    fun handleCommand() {
        when {
            command.isEqualTo(-1, -3, 37) -> {
                sendCommandToServer(-1, -4, 37)
            }
            command.isEqualTo(-1, -5, 1) -> {
                sendCommandToServer(-1, -2, 1)
            }
            command.isEqualTo(-1, -3, 1) -> {
                sendCommandToServer(-1, -4, 1)
            }
            command.isEqualTo(-1, -5, 3) -> {
                sendCommandToServer(-1, -2, 3)
            }
            command.isEqualTo(-1, -3, 39) -> {
                sendCommandToServer(-1, -4, 39)
            }
            command.isEqualTo(-1, -3, 31) -> {
                sendCommandToServer(-1, -4, 31)
            }
            command.isEqualTo(-1, -3, 0) -> {
                sendCommandToServer(-1, -4, 0)
            }
            command.isEqualTo(-1, -5, 0) -> {
                sendCommandToServer(-1, -2, 0)
            }
            command.isEqualTo(-1, -3, 24) -> {
                sendCommandToServer(-1, -5, 24)
            }
            command.isEqualTo(-1, -6, 24) -> {
                TelnetClient.getConnector().readData(0)
                TelnetClient.getConnector().readData(0)
                TelnetClient.getConnector().readData(0)
                TelnetOutputBuilder.create()
                    .pushData((-1).toByte())
                    .pushData((-6).toByte())
                    .pushData(TelnetCommand.TERMINAL_TYPE.toByte())
                    .pushData(0.toByte())
                    .pushData(65.toByte())
                    .pushData(78.toByte())
                    .pushData(83.toByte())
                    .pushData(73.toByte())
                    .pushData((-1).toByte())
                    .pushData(TelnetCommand.SE.toByte())
                    .sendToServer()
            }
            else -> {
                println("[MuTelnetCommandHandler]Unimplement command : ${command}")
            }
        }
    }

    internal fun sendCommandToServer(header: Int, action: Int, option: Int) {
        TelnetOutputBuilder.create()
            .pushData(header.toByte())
            .pushData(action.toByte())
            .pushData(option.toByte())
            .sendToServer()
    }
}
