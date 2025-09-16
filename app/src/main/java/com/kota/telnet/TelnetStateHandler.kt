package com.kota.telnet

import com.kota.telnet.model.TelnetModel.cleanFrame
import com.kota.telnet.model.TelnetModel.cleanRow
import com.kota.telnet.model.TelnetModel.getRowString
import com.kota.telnet.model.TelnetModel.rows
import com.kota.telnet.model.TelnetRow

abstract class TelnetStateHandler {
    var currentPage: Int = 0

    abstract fun handleState()

    fun cleanRow(row: Int) {
        if (TelnetClient.Companion.getModel() != null) {
            TelnetClient.Companion.getModel().cleanRow(row)
        }
    }

    fun cleanFrame() {
        if (TelnetClient.Companion.getModel() != null) {
            TelnetClient.Companion.getModel().cleanFrame()
        }
    }

    fun getRowString(row: Int): String {
        if (TelnetClient.Companion.getModel() != null) {
            return TelnetClient.Companion.getModel().getRowString(row)
        }
        return ""
    }

    val rows: Vector<TelnetRow?>
        get() = TelnetClient.Companion.getModel().rows

    open fun clear() {
    }
}
