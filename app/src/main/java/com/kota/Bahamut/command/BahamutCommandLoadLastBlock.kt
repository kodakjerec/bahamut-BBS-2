package com.kota.Bahamut.command

import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.Bahamut.pages.boardPage.BoardPageAction
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnet.TelnetOutputBuilder.Companion.create

open class BahamutCommandLoadLastBlock : TelnetCommand() {
    enum class OperationMode {
        End,
        Left_Right_End,
        Home_End,
        Left_S_End,
        NotAvailable
    }

    init {
        this.Action = BahamutCommandDefs.Companion.LoadLastBlock
    }

    private fun getLoadLastBlockMode(aListPage: TelnetListPage): OperationMode {
        when (aListPage.getListType()) {
            BoardPageAction.LINK_TITLE -> return OperationMode.Left_S_End
            BoardPageAction.SEARCH, BoardPageAction.ESSENCE -> {
                if (aListPage.getSelectedIndex() != aListPage.getItemSize()) {
                    return OperationMode.End
                }
                if (aListPage.getSelectedIndex() > 1) {
                    return OperationMode.Home_End
                }
                return OperationMode.NotAvailable
            }

            else -> {
                // 目前的文章index != 所有文章
                if (aListPage.getSelectedIndex() != aListPage.getItemSize()) {
                    return OperationMode.End
                }
                // 只有一篇 看板/文章
                if (aListPage.getItemSize() == 1) {
                    return OperationMode.Left_Right_End
                }
                return OperationMode.Home_End
            }
        }
    }

    override fun execute(aListPage: TelnetListPage?) {
        if (aListPage == null) {
            setDone(true)
            return
        }
        when (getLoadLastBlockMode(aListPage)) {
            OperationMode.Left_Right_End -> {
                create()
                    .pushKey(TelnetKeyboard.LEFT_ARROW)
                    .pushKey(TelnetKeyboard.RIGHT_ARROW)
                    .pushKey(TelnetKeyboard.END).sendToServer()
                return
            }

            OperationMode.Home_End -> {
                create()
                    .pushKey(TelnetKeyboard.HOME)
                    .pushKey(TelnetKeyboard.END).sendToServer()
                return
            }

            OperationMode.Left_S_End -> {
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

            else -> setDone(true)
        }
    }

    override fun executeFinished(aListPage: TelnetListPage, aPageData: TelnetListPageBlock) {
        if (aListPage.getItemSize() > aPageData.maximumItemNumber) {
            aListPage.setItemSize(0)
            aListPage.cleanAllItem()
        }
        setDone(true)
    }

    override fun toString(): String {
        return "[LoadLastBlock]"
    }
}
