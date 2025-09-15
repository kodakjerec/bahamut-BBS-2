package com.kota.Bahamut.Command

import com.kota.Bahamut.ListPage.TelnetListPage
import com.kota.Bahamut.ListPage.TelnetListPageBlock

abstract class TelnetCommand : BahamutCommandDefs {
    var Action = BahamutCommandDefs.LoadBlock
    private var _is_done = false
    var recordTime = true

    abstract fun execute(telnetListPage: TelnetListPage)

    abstract fun executeFinished(telnetListPage: TelnetListPage, telnetListPageBlock: TelnetListPageBlock)

    fun isDone(): Boolean {
        return this._is_done
    }

    fun setDone(done: Boolean) {
        this._is_done = done
    }

    open fun isOperationCommand(): Boolean {
        return true
    }
}
