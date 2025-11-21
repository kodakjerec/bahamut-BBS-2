package com.kota.Bahamut.command

import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnet.TelnetClient

class BahamutCommandLoadArticleEnd {
    fun execute() {
        print(toString() + " ")
        TelnetClient.myInstance?.sendKeyboardInputToServer(TelnetKeyboard.LEFT_ARROW)
    }

    override fun toString(): String {
        return "[LoadArticleEnd]"
    }
}
