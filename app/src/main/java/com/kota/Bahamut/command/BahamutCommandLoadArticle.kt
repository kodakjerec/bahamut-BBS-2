package com.kota.Bahamut.command

import com.kota.Bahamut.BahamutStateHandler
import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.telnet.TelnetClient

class BahamutCommandLoadArticle(var articleIndex: Int) : TelnetCommand() {
    init {
        this.action = BahamutCommandDef.Companion.LOAD_ARTICLE
    }

    override fun execute(telnetListPage: TelnetListPage) {
        if (this.articleIndex > 0) {
            val articleNumber = this.articleIndex.toString()
            BahamutStateHandler.instance?.setArticleNumber(articleNumber)
            TelnetClient.client?.sendStringToServerInBackground(articleNumber + "\n")
        }
    }

    override fun executeFinished(telnetListPage: TelnetListPage, telnetListPageBlock: TelnetListPageBlock?) {
        isDone = true
    }

    override fun toString(): String {
        return "[LoadArticle][articleIndex=" + this.articleIndex + "]"
    }
}
