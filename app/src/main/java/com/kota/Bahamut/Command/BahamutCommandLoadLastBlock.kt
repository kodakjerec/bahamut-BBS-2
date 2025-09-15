package com.kota.Bahamut.Command

import com.kota.Bahamut.ListPage.TelnetListPage
import com.kota.Bahamut.ListPage.TelnetListPageBlock
import com.kota.Bahamut.Pages.BoardPage.BoardPageAction
import com.kota.Telnet.Reference.TelnetKeyboard
import com.kota.Telnet.TelnetOutputBuilder

class BahamutCommandLoadLastBlock : TelnetCommand() {

    enum class OperationMode {
        End,
        Left_Right_End,
        Home_End,
        Left_S_End,
        NotAvailable
    }

    init {
        Action = BahamutCommandDefs.LoadLastBlock
    }

    private fun getLoadLastBlockMode(aListPage: TelnetListPage): OperationMode {
        return when (aListPage.listType) {
            BoardPageAction.LINK_TITLE -> // BoardLinkPage
                OperationMode.Left_S_End
            BoardPageAction.SEARCH, // BoardSearchPage
            BoardPageAction.ESSENCE -> {
                if (aListPage.selectedIndex != aListPage.itemSize) {
                    OperationMode.End
                } else if (aListPage.selectedIndex > 1) {
                    OperationMode.Home_End
                } else {
                    OperationMode.NotAvailable
                }
            }
            else -> { // TelnetListPage
                // 目前的文章index != 所有文章
                if (aListPage.selectedIndex != aListPage.itemSize) {
                    OperationMode.End
                } else if (aListPage.itemSize == 1) {
                    // 只有一篇 看板/文章
                    OperationMode.Left_Right_End
                } else {
                    OperationMode.Home_End
                }
            }
        }
    }

    override fun execute(aListPage: TelnetListPage) {
        if (aListPage == null) {
            setDone(true)
            return
        }
        
        when (getLoadLastBlockMode(aListPage)) {
            OperationMode.Left_Right_End -> {
                TelnetOutputBuilder.create()
                    .pushKey(TelnetKeyboard.LEFT_ARROW)
                    .pushKey(TelnetKeyboard.RIGHT_ARROW)
                    .pushKey(TelnetKeyboard.END).sendToServer()
                return
            }
            OperationMode.Home_End -> {
                TelnetOutputBuilder.create()
                    .pushKey(TelnetKeyboard.HOME)
                    .pushKey(TelnetKeyboard.END).sendToServer()
                return
            }
            OperationMode.Left_S_End -> {
                TelnetOutputBuilder.create()
                    .pushKey(TelnetKeyboard.LEFT_ARROW)
                    .pushKey(TelnetKeyboard.BACK_ONE_CHAR)
                    .pushKey(TelnetKeyboard.END).sendToServer()
                return
            }
            OperationMode.End -> {
                TelnetOutputBuilder.create()
                    .pushKey(TelnetKeyboard.END).sendToServer()
                return
            }
            else -> {
                setDone(true)
            }
        }
    }

    override fun executeFinished(aListPage: TelnetListPage, aPageData: TelnetListPageBlock) {
        if (aListPage.itemSize > aPageData.maximumItemNumber) {
            aListPage.itemSize = 0
            aListPage.cleanAllItem()
        }
        setDone(true)
    }

    override fun toString(): String {
        return "[LoadLastBlock]"
    }
}
