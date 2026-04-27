package com.kota.Bahamut.command

import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.Bahamut.service.TempSettings
import com.kota.telnet.TelnetArticle
import com.kota.telnet.TelnetClient
import com.kota.telnet.reference.TelnetKeyboard

class BahamutCommandLocateArticle() : TelnetCommand() {
    private var targetArticle: TelnetArticle? = null

    init {
        this.targetArticle = TempSettings.targetArticle
        this.action = BahamutCommandDef.Companion.LOCATE_ARTICLE
    }

    override fun execute(telnetListPage: TelnetListPage) {
        if (this.targetArticle != null) {
            TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.SMALL_T)
        }
    }

    override fun executeFinished(telnetListPage: TelnetListPage, telnetListPageBlock: TelnetListPageBlock?) {
        isDone = true
    }

    override fun toString(): String {
        return "[LocateArticle][author=${targetArticle?.author}, title=${targetArticle?.title}, datetime=${targetArticle?.dateTime}]"
    }
}
