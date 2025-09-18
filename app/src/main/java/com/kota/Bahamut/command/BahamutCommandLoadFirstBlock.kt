package com.kota.Bahamut.command

import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock

class BahamutCommandLoadFirstBlock : TelnetCommand() {
    init {
        this.action = BahamutCommandDef.Companion.LOAD_FIRST_BLOCK
    }

    override fun execute(telnetListPage: TelnetListPage) {
    }

    override fun executeFinished(telnetListPage: TelnetListPage, telnetListPageBlock: TelnetListPageBlock) {
        isDone = true
    }

    override fun toString(): String {
        return "[LoadFirstBlock]"
    }
}
