package com.kota.telnet

import com.kota.telnet.model.TelnetFrame
import com.kota.telnet.model.TelnetModel
import com.kota.telnet.model.TelnetRow
import java.util.Vector

class TelnetArticleItem {
    var author: String? = ""
    var content: String = ""
        private set
    private var _frame: TelnetFrame? = null
    var nickname: String? = ""
    var quoteLevel: Int = 0
    private val _rows = Vector<TelnetRow>()
    var type: Int = 0

    fun addRow(row: TelnetRow?) {
        this._rows.add(row)
    }

    fun clear() {
        this.author = ""
        this.nickname = ""
        this.content = ""
        this.quoteLevel = 0
    }

    fun build() {
        val buffer = StringBuilder()
        for (row in this._rows) {
            if (buffer.length > 0) {
                buffer.append("\n")
            }
            buffer.append(row.toContentString())
        }
        this.content = buffer.toString()
    }

    val isEmpty: Boolean
        get() = this.content.length == 0

    val model: TelnetModel
        get() = TelnetModel(this._rows.size)

    override fun toString(): String {
        return "QuoteLevel:" + this.quoteLevel + "\n" +
                "Author:" + this.author + "\n" +
                "Nickname:" + this.nickname + "\n" +
                "Content:" + this.content + "\n"
    }

    fun buildFrame() {
        this._frame = TelnetFrame(this._rows.size)
        for (i in this._rows.indices) {
            this._frame!!.setRow(i, this._rows.get(i))
        }
    }

    val frame: TelnetFrame?
        get() {
            if (this._frame == null) {
                buildFrame()
            }
            return this._frame
        }
}
