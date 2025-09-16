package com.kota.Bahamut.command

import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.telnet.TelnetClient

class BahamutCommandSearchArticle(
    var _keyword: String,
    var _author: String?,
    var _mark: String?,
    var _gy: String?
) : TelnetCommand() {
    init {
        Action = BahamutCommandDefs.Companion.SearchArticle
    }

    override fun execute(aListPage: TelnetListPage?) {
        TelnetClient.getClient()
            .sendStringToServerInBackground("~" + _keyword + "\n" + _author + "\n" + _mark + "\n" + _gy)
    }

    override fun executeFinished(aListPage: TelnetListPage?, aPageData: TelnetListPageBlock?) {
        setDone(true)
    }

    override fun toString(): String {
        return "[SearchArticle][keyword=" + _keyword + " author=" + _author + " mark=" + _mark + " gy=" + _gy + "]"
    }
}
