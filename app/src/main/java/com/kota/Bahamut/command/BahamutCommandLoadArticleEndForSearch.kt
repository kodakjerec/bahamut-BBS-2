package com.kota.Bahamut.command

import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnet.TelnetClient

class BahamutCommandLoadArticleEndForSearch {
    fun execute() {
        print(toString() + " ")
        TelnetClient.client?.sendKeyboardInputToServer(TelnetKeyboard.END)
    }

    override fun toString(): String {
        return "[LoadArticleEndForSearch]"
    }
}
