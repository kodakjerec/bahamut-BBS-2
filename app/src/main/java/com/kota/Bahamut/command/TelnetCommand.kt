package com.kota.Bahamut.command

import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock

abstract class TelnetCommand : BahamutCommandDef {
    @JvmField
    var action: Int = BahamutCommandDef.Companion.LOAD_BLOCK
    var isDone: Boolean = false
    @JvmField
    var recordTime: Boolean = true

    abstract fun execute(telnetListPage: TelnetListPage)

    abstract fun executeFinished(
        telnetListPage: TelnetListPage,
        telnetListPageBlock: TelnetListPageBlock
    )

    open val isOperationCommand: Boolean
        get() = true
}
