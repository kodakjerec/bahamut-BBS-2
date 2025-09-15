package com.kota.Bahamut.Command

import com.kota.Bahamut.ListPage.TelnetListPage
import com.kota.Bahamut.ListPage.TelnetListPageBlock
import com.kota.Telnet.TelnetClient

class BahamutCommandSearchArticle(
    private val _keyword: String,
    private val _author: String,
    private val _mark: String,
    private val _gy: String
) : TelnetCommand() {
    
    init {
        Action = BahamutCommandDefs.SearchArticle
    }

    override fun execute(aListPage: TelnetListPage) {
        TelnetClient.getClient().sendStringToServerInBackground("~$_keyword\n$_author\n$_mark\n$_gy")
    }

    override fun executeFinished(aListPage: TelnetListPage, aPageData: TelnetListPageBlock) {
        setDone(true)
    }

    override fun toString(): String {
        return "[SearchArticle][keyword=$_keyword author=$_author mark=$_mark gy=$_gy]"
    }
}
