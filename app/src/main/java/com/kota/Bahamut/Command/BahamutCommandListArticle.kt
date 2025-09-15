package com.kota.Bahamut.Command

import com.kota.Bahamut.ListPage.TelnetListPage
import com.kota.Bahamut.ListPage.TelnetListPageBlock
import com.kota.Telnet.TelnetClient

class BahamutCommandListArticle(private val _article_index: Int) : TelnetCommand() {
    
    init {
        Action = BahamutCommandDefs.ListArticle
    }

    override fun execute(aListPage: TelnetListPage) {
        if (_article_index > 0) {
            TelnetClient.getClient().sendDataToServer("$_article_index\nS".toByteArray())
        }
    }

    override fun executeFinished(aListPage: TelnetListPage, aPageData: TelnetListPageBlock) {
        setDone(true)
    }

    override fun toString(): String {
        return "[ListArticle][articleIndex=$_article_index]"
    }
}
