package com.kota.Bahamut.command

import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.telnet.logic.ItemUtils.getBlock
import com.kota.telnet.TelnetClient

class BahamutCommandLoadBlock(aBlock: Int) : TelnetCommand() {
    var _block: Int

    init {
        this.Action = BahamutCommandDefs.Companion.LoadBlock
        this._block = aBlock
    }

    fun containsArticle(articleIndex: Int): Boolean {
        return this._block == getBlock(articleIndex)
    }

    override fun isOperationCommand(): Boolean {
        return false
    }

    override fun execute(aListPage: TelnetListPage?) {
        if (this._block >= 0) {
            TelnetClient.getClient().sendStringToServer(((this._block * 20) + 1).toString())
        }
    }

    override fun executeFinished(aListPage: TelnetListPage?, aPageData: TelnetListPageBlock?) {
        setDone(true)
    }

    override fun toString(): String {
        return "[LoadBlock][block=" + this._block + " targetIndex=" + ((this._block * 20) + 1) + "]"
    }
}
