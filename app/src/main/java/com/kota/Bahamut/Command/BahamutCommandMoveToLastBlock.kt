package com.kota.Bahamut.Command

import com.kota.Bahamut.ListPage.TelnetListPage
import com.kota.Bahamut.ListPage.TelnetListPageBlock

class BahamutCommandMoveToLastBlock : BahamutCommandLoadLastBlock() {
    
    init {
        Action = BahamutCommandDefs.MoveToLastBlock
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
