package com.kota.telnet

import com.kota.telnet.model.TelnetRow
import java.util.Vector

abstract class TelnetStateHandler {
    var currentPage: Int = 0

    abstract fun handleState()

    fun cleanRow(row: Int) {
        TelnetClient.model.cleanRow(row)
    }

    fun cleanFrame() {
        TelnetClient.model.cleanFrame()
    }

    fun getRowString(row: Int): String {
        return TelnetClient.model.getRowString(row)
    }

    val rows: Vector<TelnetRow>
        get() = TelnetClient.model.rows

    open fun clear() {
    }
}
