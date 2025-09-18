package com.kota.Bahamut.command

import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock

class BahamutCommandMoveToLastBlock : BahamutCommandLoadLastBlock() {
    init {
        this.action = BahamutCommandDef.Companion.MOVE_TO_LAST_BLOCK
    }

    override fun executeFinished(aListPage: TelnetListPage, aPageData: TelnetListPageBlock) {
        super.executeFinished(aListPage, aPageData)
        aListPage.pushRefreshCommand(1)
        isDone = true
    }

    override fun toString(): String {
        return "[MoveToLastBlock]"
    }
}
