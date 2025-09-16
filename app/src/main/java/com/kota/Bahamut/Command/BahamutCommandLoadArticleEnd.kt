package com.kota.Bahamut.Command

import com.kota.Telnet.Reference.TelnetKeyboard
import com.kota.Telnet.TelnetClient

class BahamutCommandLoadArticleEnd {
    fun execute() {
        print(toString() + " ")
        TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.LEFT_ARROW)
    }

    override fun toString(): String {
        return "[LoadArticleEnd]"
    }
}
