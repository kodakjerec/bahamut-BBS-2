package com.kota.Bahamut.Command

import com.kota.Bahamut.BahamutStateHandler
import com.kota.Bahamut.ListPage.TelnetListPage
import com.kota.Bahamut.ListPage.TelnetListPageBlock
import com.kota.Telnet.TelnetClient

class BahamutCommandLoadArticle(private val _article_number: Int) : TelnetCommand() {
    
    init {
        Action = BahamutCommandDefs.LoadArticle
    }

    fun getArticleIndex(): Int {
        return _article_number
    }

    override fun execute(aListPage: TelnetListPage) {
        if (_article_number > 0) {
            val article_number = _article_number.toString()
            BahamutStateHandler.getInstance().setArticleNumber(article_number)
            TelnetClient.getClient().sendStringToServerInBackground("$article_number\n")
        }
    }

    override fun executeFinished(aListPage: TelnetListPage, aPageData: TelnetListPageBlock) {
        setDone(true)
    }

    override fun toString(): String {
        return "[LoadArticle][articleIndex=$_article_number]"
    }
}
