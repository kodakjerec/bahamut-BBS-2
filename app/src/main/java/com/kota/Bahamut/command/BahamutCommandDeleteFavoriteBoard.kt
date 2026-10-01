package com.kota.Bahamut.command

import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.telnet.TelnetClient

class BahamutCommandDeleteFavoriteBoard(var itemIndex: Int) : TelnetCommand() {
    init {
        action = BahamutCommandDef.DELETE_ARTICLE
    }

    override fun execute(telnetListPage: TelnetListPage) {
        if (itemIndex > 0) {
            TelnetClient.myInstance!!.sendStringToServer("$itemIndex\nd")
        }
    }

    override fun executeFinished(telnetListPage: TelnetListPage, telnetListPageBlock: TelnetListPageBlock?) {
        isDone = true
        telnetListPage.cleanAllItem()
        telnetListPage.loadLastBlock()
    }

    override fun toString(): String {
        return "[DeleteFavoriteBoard][itemIndex=$itemIndex]"
    }
}
