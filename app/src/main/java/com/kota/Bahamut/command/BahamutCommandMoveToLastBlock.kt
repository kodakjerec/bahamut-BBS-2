package com.kota.Bahamut.command

import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock

class BahamutCommandMoveToLastBlock : BahamutCommandLoadLastBlock() {
    init {
        this.Action = BahamutCommandDefs.Companion.MoveToLastBlock
    }

    override fun executeFinished(aListPage: TelnetListPage, aPageData: TelnetListPageBlock) {
        super.executeFinished(aListPage, aPageData)
        aListPage.pushRefreshCommand(1)
        setDone(true)
    }

    override fun toString(): String {
        return "[MoveToLastBlock]"
    }
}
