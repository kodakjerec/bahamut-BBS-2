package com.kota.telnet

import com.kota.telnet.model.TelnetFrame
import com.kota.telnet.model.TelnetModel
import com.kota.telnet.model.TelnetRow
import java.util.Vector

class TelnetArticleItem {
    var author: String? = ""
    var content: String = ""
        private set
    var telnetFrame: TelnetFrame? = null
    var nickname: String? = ""
    var quoteLevel: Int = 0
    val rows = Vector<TelnetRow>()
    var type: Int = 0

    fun addRow(row: TelnetRow?) {
        this.rows.add(row)
    }

    fun clear() {
        this.author = ""
        this.nickname = ""
        this.content = ""
        this.quoteLevel = 0
    }

    fun build() {
        val buffer = StringBuilder()
        for (row in this.rows) {
            if (buffer.isNotEmpty()) {
                buffer.append("\n")
            }
            buffer.append(row.toContentString())
        }
        this.content = buffer.toString()
    }

    val isEmpty: Boolean
        get() = this.content.isEmpty()

    val model: TelnetModel
        get() = TelnetModel(this.rows.size)

    override fun toString(): String {
        return "QuoteLevel:" + this.quoteLevel + "\n" +
                "Author:" + this.author + "\n" +
                "Nickname:" + this.nickname + "\n" +
                "Content:" + this.content + "\n"
    }

    fun buildFrame() {
        this.telnetFrame = TelnetFrame(this.rows.size)
        for (i in this.rows.indices) {
            this.telnetFrame!!.setRow(i, this.rows[i])
        }
    }

    val frame: TelnetFrame?
        get() {
            if (this.telnetFrame == null) {
                buildFrame()
            }
            return this.telnetFrame
        }
}
