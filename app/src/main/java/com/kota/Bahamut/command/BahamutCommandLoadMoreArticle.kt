package com.kota.Bahamut.command

import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnet.TelnetClient

class BahamutCommandLoadMoreArticle {
    fun execute() {
        print(toString() + " ")
        TelnetClient.client?.sendKeyboardInputToServerInBackground(TelnetKeyboard.PAGE_DOWN, 1)
    }

    override fun toString(): String {
        return "[LoadMoreArticle]"
    }
}
