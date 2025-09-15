package com.kota.Telnet

import com.kota.Telnet.Model.TelnetFrame
import com.kota.Telnet.Model.TelnetModel
import com.kota.Telnet.Model.TelnetRow
import java.util.*

class TelnetArticleItem {
    private var _author = ""
    private var _content = ""
    private var _frame: TelnetFrame? = null
    private var _nickname = ""
    private var _quote_level = 0
    private val _rows = Vector<TelnetRow>()
    private var _type = 0

    fun getAuthor(): String = _author

    fun setAuthor(author: String) {
        _author = author
    }

    fun getNickname(): String = _nickname

    fun setNickname(nickname: String) {
        _nickname = nickname
    }

    fun getContent(): String = _content

    fun getQuoteLevel(): Int = _quote_level

    fun setQuoteLevel(quoteLevel: Int) {
        _quote_level = quoteLevel
    }

    fun getType(): Int = _type

    fun setType(type: Int) {
        _type = type
    }

    fun addRow(row: TelnetRow) {
        _rows.add(row)
    }

    fun clear() {
        _author = ""
        _nickname = ""
        _content = ""
        _quote_level = 0
    }

    fun build() {
        val buffer = StringBuilder()
        for (row in _rows) {
            if (buffer.isNotEmpty()) {
                buffer.append("\n")
            }
            buffer.append(row.toContentString())
        }
        _content = buffer.toString()
    }

    fun isEmpty(): Boolean = _content.isEmpty()

    fun getModel(): TelnetModel = TelnetModel(_rows.size)

    override fun toString(): String {
        return "QuoteLevel:$_quote_level\n" +
                "Author:$_author\n" +
                "Nickname:$_nickname\n" +
                "Content:$_content\n"
    }

    fun buildFrame() {
        _frame = TelnetFrame(_rows.size)
        for (i in _rows.indices) {
            _frame?.setRow(i, _rows[i])
        }
    }

    fun getFrame(): TelnetFrame {
        if (_frame == null) {
            buildFrame()
        }
        return _frame!!
    }
}
