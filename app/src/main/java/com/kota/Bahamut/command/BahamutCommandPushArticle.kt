package com.kota.Bahamut.command

import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.telnet.TelnetClient

class BahamutCommandPushArticle(var _article_index: Int) : TelnetCommand() {
    init {
        this.Action = BahamutCommandDefs.Companion.PushArticle
    }

    override fun execute(aListPage: TelnetListPage?) {
        if (this._article_index > 0) {
            TelnetClient.getClient().sendStringToServer(this._article_index.toString() + "\ngx")
        }
    }

    override fun executeFinished(aListPage: TelnetListPage?, aPageData: TelnetListPageBlock?) {
        setDone(true)
    }

    override fun toString(): String {
        return "[PushArticle][articleIndex=" + this._article_index + "]"
    }
}
