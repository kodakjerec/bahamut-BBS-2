package com.kota.Bahamut.command

import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.Bahamut.pages.boardPage.BoardPageAction
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnet.TelnetOutputBuilder.Companion.create

open class BahamutCommandLoadLastBlock : TelnetCommand() {
    enum class OperationMode {
        End,
        LeftRightEnd,
        HomeEnd,
        LeftSEnd,
        NotAvailable
    }

    init {
        this.action = BahamutCommandDef.Companion.LOAD_LAST_BLOCK
    }

    private fun getLoadLastBlockMode(aListPage: TelnetListPage): OperationMode {
        when (aListPage.listType) {
            BoardPageAction.LINK_TITLE -> return OperationMode.LeftSEnd
            BoardPageAction.SEARCH, BoardPageAction.ESSENCE -> {
                if (aListPage.selectedIndex != aListPage.listCount) {
                    return OperationMode.End
                }
                if (aListPage.selectedIndex > 1) {
                    return OperationMode.HomeEnd
                }
                return OperationMode.NotAvailable
            }

            else -> {
                // 目前的文章index != 所有文章
                if (aListPage.selectedIndex != aListPage.listCount) {
                    return OperationMode.End
                }
                // 只有一篇 看板/文章
                if (aListPage.listCount == 1) {
                    return OperationMode.LeftRightEnd
                }
                return OperationMode.HomeEnd
            }
        }
    }

    override fun execute(telnetListPage: TelnetListPage) {
        when (getLoadLastBlockMode(telnetListPage)) {
            OperationMode.LeftRightEnd -> {
                create()
                    .pushKey(TelnetKeyboard.LEFT_ARROW)
                    .pushKey(TelnetKeyboard.RIGHT_ARROW)
                    .pushKey(TelnetKeyboard.END).sendToServer()
                return
            }

            OperationMode.HomeEnd -> {
                create()
                    .pushKey(TelnetKeyboard.HOME)
                    .pushKey(TelnetKeyboard.END).sendToServer()
                return
            }

            OperationMode.LeftSEnd -> {
                create()
                    .pushKey(TelnetKeyboard.LEFT_ARROW)
                    .pushKey(TelnetKeyboard.BACK_ONE_CHAR)
                    .pushKey(TelnetKeyboard.END).sendToServer()
                return
            }

            OperationMode.End -> {
                create()
                    .pushKey(TelnetKeyboard.END).sendToServer()
                return
            }

            else -> isDone = true
        }
    }

    override fun executeFinished(telnetListPage: TelnetListPage, telnetListPageBlock: TelnetListPageBlock?) {
        if (telnetListPage.listCount > telnetListPageBlock!!.maximumItemNumber) {
            telnetListPage.listCount = 0
            telnetListPage.cleanAllItem()
        }
        isDone = true
    }

    override fun toString(): String {
        return "[LoadLastBlock]"
    }
}
