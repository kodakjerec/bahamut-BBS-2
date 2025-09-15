package com.kota.Telnet

import com.kota.Telnet.Model.TelnetRow
import java.util.*

abstract class TelnetStateHandler {
    private var currentPage = 0

    abstract fun handleState()

    fun cleanRow(row: Int) {
        TelnetClient.getModel().cleanRow(row)
    }

    fun cleanFrame() {
        TelnetClient.getModel().cleanFrame()
    }

    fun getRowString(row: Int): String {
        return TelnetClient.getModel().getRowString(row)
    }

    fun getRows(): Vector<TelnetRow> {
        return TelnetClient.getModel().getRows()
    }

    fun setCurrentPage(pageID: Int) {
        currentPage = pageID
    }

    fun getCurrentPage(): Int {
        return currentPage
    }

    open fun clear() {
        // Default implementation - do nothing
    }
}
