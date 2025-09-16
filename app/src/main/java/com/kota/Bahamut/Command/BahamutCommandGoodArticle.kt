package com.kota.Bahamut.Command

import com.kota.Bahamut.ListPage.TelnetListPage
import com.kota.Bahamut.ListPage.TelnetListPageBlock
import com.kota.Telnet.TelnetClient

class BahamutCommandGoodArticle(var _article_index: Int) : TelnetCommand() {
    init {
        this.Action = BahamutCommandDefs.Companion.GoodArticle
    }

    override fun execute(aListPage: TelnetListPage?) {
        if (this._article_index > 0) {
            TelnetClient.getClient().sendStringToServer(this._article_index.toString() + "\ngy")
        }
    }

    override fun executeFinished(aListPage: TelnetListPage?, aPageData: TelnetListPageBlock?) {
        setDone(true)
    }

    override fun toString(): String {
        return "[GoodArticle][articleIndex=" + this._article_index + "]"
    }
}
