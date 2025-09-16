package com.kota.Bahamut.command

import com.kota.Bahamut.BahamutStateHandler
import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.telnet.TelnetClient

class BahamutCommandLoadArticle(var articleIndex: Int) : TelnetCommand() {
    init {
        this.Action = BahamutCommandDefs.Companion.LoadArticle
    }

    override fun execute(aListPage: TelnetListPage?) {
        if (this.articleIndex > 0) {
            val article_number = this.articleIndex.toString()
            BahamutStateHandler.getInstance().setArticleNumber(article_number)
            TelnetClient.getClient().sendStringToServerInBackground(article_number + "\n")
        }
    }

    override fun executeFinished(aListPage: TelnetListPage?, aPageData: TelnetListPageBlock?) {
        setDone(true)
    }

    override fun toString(): String {
        return "[LoadArticle][articleIndex=" + this.articleIndex + "]"
    }
}
