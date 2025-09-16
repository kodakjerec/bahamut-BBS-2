package com.kota.telnet

import com.kota.telnet.model.TelnetRow
import java.util.Vector

class TelnetArticlePage {
    private val rows = Vector<TelnetRow?>()

    fun addRow(row: TelnetRow) {
        this.rows.add(row.clone())
    }

    val rowCount: Int
        get() = this.rows.size

    fun getRow(index: Int): TelnetRow? {
        return this.rows.get(index)
    }

    fun clear() {
        this.rows.clear()
    }
}
