package com.kota.Bahamut.Command

import com.kota.Telnet.Reference.TelnetKeyboard
import com.kota.Telnet.TelnetClient

class BahamutCommandLoadArticleEndForSearch {
    fun execute() {
        print(toString() + " ")
        TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.END)
    }

    override fun toString(): String {
        return "[LoadArticleEndForSearch]"
    }
}
