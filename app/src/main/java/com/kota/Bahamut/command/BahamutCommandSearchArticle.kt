package com.kota.Bahamut.command

import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.telnet.TelnetClient

class BahamutCommandSearchArticle(
    var keyword: String,
    var author: String?,
    var mark: String?,
    var gy: String?
) : TelnetCommand() {
    init {
        action = BahamutCommandDef.Companion.SEARCH_ARTICLE
    }

    override fun execute(telnetListPage: TelnetListPage) {
        TelnetClient.client!!
            .sendStringToServerInBackground("~$keyword\n$author\n$mark\n$gy")
    }

    override fun executeFinished(telnetListPage: TelnetListPage, telnetListPageBlock: TelnetListPageBlock) {
        isDone = true
    }

    override fun toString(): String {
        return "[SearchArticle][keyword=$keyword author=$author mark=$mark gy=$gy]"
    }
}
