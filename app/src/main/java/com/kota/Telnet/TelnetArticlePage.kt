package com.kota.Telnet

import com.kota.Telnet.Model.TelnetRow
import java.util.*

class TelnetArticlePage {
    private val rows = Vector<TelnetRow>()

    fun addRow(row: TelnetRow?) {
        rows.add(row.clone())
    }

    fun getRowCount(): Int = rows.size

    fun getRow(index: Int): TelnetRow = rows[index]

    fun clear() {
        rows.clear()
    }
}
