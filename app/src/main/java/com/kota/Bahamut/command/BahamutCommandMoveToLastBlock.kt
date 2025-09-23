package com.kota.Bahamut.command

import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock

class BahamutCommandMoveToLastBlock : BahamutCommandLoadLastBlock() {
    init {
        this.action = BahamutCommandDef.Companion.MOVE_TO_LAST_BLOCK
    }

    override fun executeFinished(telnetListPage: TelnetListPage, telnetListPageBlock: TelnetListPageBlock?) {
        super.executeFinished(telnetListPage, telnetListPageBlock)
        telnetListPage.pushRefreshCommand(1)
        isDone = true
    }

    override fun toString(): String {
        return "[MoveToLastBlock]"
    }
}
