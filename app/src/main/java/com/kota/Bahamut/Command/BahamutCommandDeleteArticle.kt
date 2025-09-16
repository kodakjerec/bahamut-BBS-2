package com.kota.Bahamut.Command

import com.kota.Bahamut.ListPage.TelnetListPage
import com.kota.Bahamut.ListPage.TelnetListPageBlock
import com.kota.Telnet.TelnetClient

class BahamutCommandDeleteArticle(var _article_index: Int) : TelnetCommand() {
    init {
        Action = BahamutCommandDefs.Companion.DeleteArticle
    }

    override fun execute(aListPage: TelnetListPage?) {
        if (_article_index > 0) {
            TelnetClient.getClient().sendStringToServer(_article_index.toString() + "\ndy")
        }
    }

    override fun executeFinished(aListPage: TelnetListPage?, aPageData: TelnetListPageBlock?) {
        setDone(true)
    }

    override fun toString(): String {
        return "[DeleteArticle][articleIndex=" + _article_index + "]"
    }
}
