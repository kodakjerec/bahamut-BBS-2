package com.kota.Bahamut.Command

import com.kota.Bahamut.ListPage.TelnetListPage
import com.kota.Bahamut.ListPage.TelnetListPageBlock
import com.kota.Telnet.Logic.ItemUtils
import com.kota.Telnet.TelnetClient

class BahamutCommandLoadBlock(private val _block: Int) : TelnetCommand() {
    
    init {
        Action = BahamutCommandDefs.LoadBlock
    }

    fun containsArticle(articleIndex: Int): Boolean {
        return _block == ItemUtils.getBlock(articleIndex)
    }

    override fun isOperationCommand(): Boolean {
        return false
    }

    override fun execute(aListPage: TelnetListPage) {
        if (_block >= 0) {
            TelnetClient.getClient().sendStringToServer(((_block * 20) + 1).toString())
        }
    }

    override fun executeFinished(aListPage: TelnetListPage, aPageData: TelnetListPageBlock) {
        setDone(true)
    }

    override fun toString(): String {
        return "[LoadBlock][block=$_block targetIndex=${(_block * 20) + 1}]"
    }
}
