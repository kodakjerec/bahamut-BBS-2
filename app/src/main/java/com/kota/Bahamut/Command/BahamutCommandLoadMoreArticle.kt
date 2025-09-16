package com.kota.Bahamut.Command

import com.kota.Telnet.Reference.TelnetKeyboard
import com.kota.Telnet.TelnetClient

class BahamutCommandLoadMoreArticle {
    fun execute() {
        print(toString() + " ")
        TelnetClient.getClient().sendKeyboardInputToServerInBackground(TelnetKeyboard.PAGE_DOWN, 1)
    }

    override fun toString(): String {
        return "[LoadMoreArticle]"
    }
}
