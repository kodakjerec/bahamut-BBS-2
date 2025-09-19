package com.kota.Bahamut.command

import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.telnet.TelnetClient

class BahamutCommandListArticle(var articleIndex: Int) : TelnetCommand() {
    init {
        this.action = BahamutCommandDef.Companion.LIST_ARTICLE
    }

    override fun execute(telnetListPage: TelnetListPage) {
        if (this.articleIndex > 0) {
            TelnetClient.client!!
                .sendDataToServer((this.articleIndex.toString() + "\nS").toByteArray())
        }
    }

    override fun executeFinished(telnetListPage: TelnetListPage, telnetListPageBlock: TelnetListPageBlock?) {
        isDone = true
    }

    override fun toString(): String {
        return "[ListArticle][articleIndex=" + this.articleIndex + "]"
    }
}
