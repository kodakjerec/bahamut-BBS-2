package com.kota.Bahamut.command

import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.asFramework.thread.ASCoroutine
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.telnet.TelnetOutputBuilder.Companion.create
import com.kota.telnet.reference.TelnetKeyboard

class BahamutCommandTheSameTitleTop(fromArticleIndex: Int) : TelnetCommand() {
    var articleIndex: Int

    init {
        action = BahamutCommandDef.Companion.LOAD_FORWARD_SAME_ARTICLE
        articleIndex = fromArticleIndex
    }

    override fun execute(telnetListPage: TelnetListPage) {
        if (telnetListPage.listType > 0) {
            // 找出沒被block的最小index
            val miniumAvailableIndex = 1
            if (articleIndex == miniumAvailableIndex) {
                ASCoroutine.runOnMain {
                    showShortToast("找沒有了耶...:(")
                    telnetListPage.onLoadItemFinished()
                }
                isDone = true
            } else {
                create()
                    .pushString(miniumAvailableIndex.toString() + "\n")
                    .sendToServer()
            }
        } else if (articleIndex > 0) {
            create()
                .pushString(articleIndex.toString() + "\n\\")
                .sendToServer()
        } else {
            isDone = true
        }
    }

    override fun executeFinished(telnetListPage: TelnetListPage, telnetListPageBlock: TelnetListPageBlock?) {
        if (telnetListPageBlock?.selectedItem?.isDeleted == true || telnetListPage.isItemBlocked(
                telnetListPageBlock?.selectedItem
            )) {
            if (articleIndex == telnetListPageBlock?.selectedItemNumber) {
                ASCoroutine.runOnMain {
                    telnetListPage.onLoadItemFinished()
                }
                isDone = true
            } else {
//                articleIndex = aPageData.selectedItemNumber;

                create()
                    .pushKey(TelnetKeyboard.DOWN_ARROW)
                    .sendToServer()
                isDone = false
            }
        } else if (telnetListPage.isItemLoadingByNumber(telnetListPageBlock?.selectedItemNumber!!)) {
            ASCoroutine.runOnMain {
                showShortToast("找沒有了耶...:(")
                telnetListPage.onLoadItemFinished()
            }
            isDone = true
        } else if (!telnetListPage.isEnabled(telnetListPageBlock.selectedItemNumber - 1)) {
            showShortToast("上一篇不可使用")
            telnetListPage.onLoadItemFinished()
            isDone = true
        } else {
            telnetListPage.loadItemAtNumber(telnetListPageBlock.selectedItemNumber)
            isDone = true
        }
    }

    override fun toString(): String {
        return "[BahamutCommandTheSameTitleTop][articleIndex=$articleIndex]"
    }
}
