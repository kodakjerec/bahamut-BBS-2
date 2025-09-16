package com.kota.Bahamut.Command

import com.kota.Bahamut.ListPage.TelnetListPage
import com.kota.Bahamut.ListPage.TelnetListPageBlock

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
