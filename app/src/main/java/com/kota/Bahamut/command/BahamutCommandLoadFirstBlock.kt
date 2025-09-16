package com.kota.Bahamut.command

import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock

class BahamutCommandLoadFirstBlock : TelnetCommand() {
    init {
        this.Action = BahamutCommandDefs.Companion.LoadFirstBlock
    }

    override fun execute(aListPage: TelnetListPage?) {
    }

    override fun executeFinished(aListPage: TelnetListPage?, aPageData: TelnetListPageBlock?) {
        setDone(true)
    }

    override fun toString(): String {
        return "[LoadFirstBlock]"
    }
}
