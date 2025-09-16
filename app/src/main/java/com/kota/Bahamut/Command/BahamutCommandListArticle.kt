package com.kota.Bahamut.Command

import com.kota.Bahamut.ListPage.TelnetListPage
import com.kota.Bahamut.ListPage.TelnetListPageBlock
import com.kota.Telnet.TelnetClient

class BahamutCommandListArticle(var _article_index: Int) : TelnetCommand() {
    init {
        this.Action = BahamutCommandDefs.Companion.ListArticle
    }

    override fun execute(aListPage: TelnetListPage?) {
        if (this._article_index > 0) {
            TelnetClient.getClient()
                .sendDataToServer((this._article_index.toString() + "\nS").toByteArray())
        }
    }

    override fun executeFinished(aListPage: TelnetListPage?, aPageData: TelnetListPageBlock?) {
        setDone(true)
    }

    override fun toString(): String {
        return "[ListArticle][articleIndex=" + this._article_index + "]"
    }
}
