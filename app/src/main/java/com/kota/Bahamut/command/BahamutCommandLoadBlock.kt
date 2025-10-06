package com.kota.Bahamut.command

import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.telnet.logic.ItemUtils.getBlock
import com.kota.telnet.TelnetClient

class BahamutCommandLoadBlock(aBlock: Int) : TelnetCommand() {
    var block: Int

    init {
        this.action = BahamutCommandDef.Companion.LOAD_BLOCK
        this.block = aBlock
    }

    fun containsArticle(articleIndex: Int): Boolean {
        return this.block == getBlock(articleIndex)
    }

    override val isOperationCommand: Boolean
        get() = false

    override fun execute(telnetListPage: TelnetListPage) {
        if (this.block >= 0) {
            TelnetClient.myInstance?.sendStringToServer(((this.block * 20) + 1).toString())
        }
    }

    override fun executeFinished(telnetListPage: TelnetListPage, telnetListPageBlock: TelnetListPageBlock?) {
        isDone = true
    }

    override fun toString(): String {
        return "[LoadBlock][block=" + this.block + " targetIndex=" + ((this.block * 20) + 1) + "]"
    }
}
