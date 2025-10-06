package com.kota.telnet

import com.kota.telnet.model.TelnetRow
import java.util.Vector

abstract class TelnetStateHandler {
    var currentPage: Int = 0

    abstract fun handleState()

    fun cleanRow(row: Int) {
        if (TelnetClient.client?.model != null) {
            TelnetClient.client?.model?.cleanRow(row)
        }
    }

    fun cleanFrame() {
        if (TelnetClient.client?.model != null) {
            TelnetClient.client?.model?.cleanFrame()
        }
    }

    fun getRowString(row: Int): String {
        if (TelnetClient.client!!.model != null) {
            return TelnetClient.client!!.model!!.getRowString(row)
        }
        return ""
    }

    val rows: Vector<TelnetRow>
        get() = TelnetClient.client!!.model!!.rows

    open fun clear() {
    }
}
