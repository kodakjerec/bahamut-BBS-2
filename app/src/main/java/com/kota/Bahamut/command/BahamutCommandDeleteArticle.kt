package com.kota.Bahamut.command

import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.telnet.TelnetClient

class BahamutCommandDeleteArticle(var articleIndex: Int) : TelnetCommand() {
    init {
        action = BahamutCommandDef.Companion.DELETE_ARTICLE
    }

    override fun execute(telnetListPage: TelnetListPage) {
        if (articleIndex > 0) {
            TelnetClient.client?.sendStringToServer("$articleIndex\ndy")
        }
    }

    override fun executeFinished(telnetListPage: TelnetListPage, telnetListPageBlock: TelnetListPageBlock?) {
        isDone = true
    }

    override fun toString(): String {
        return "[DeleteArticle][articleIndex=$articleIndex]"
    }
}
