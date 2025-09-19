package com.kota.Bahamut.command

import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.telnet.TelnetClient

class BahamutCommandGoodArticle(var articleIndex: Int) : TelnetCommand() {
    init {
        this.action = BahamutCommandDef.Companion.GOOD_ARTICLE
    }

    override fun execute(telnetListPage: TelnetListPage) {
        if (this.articleIndex > 0) {
            TelnetClient.client?.sendStringToServer(this.articleIndex.toString() + "\ngy")
        }
    }

    override fun executeFinished(telnetListPage: TelnetListPage, telnetListPageBlock: TelnetListPageBlock?) {
        isDone = true
    }

    override fun toString(): String {
        return "[GoodArticle][articleIndex=" + this.articleIndex + "]"
    }
}
