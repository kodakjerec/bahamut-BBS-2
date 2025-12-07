package com.kota.Bahamut.command

import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.asFramework.thread.ASCoroutine
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.telnet.TelnetOutputBuilder.Companion.create
import com.kota.telnet.reference.TelnetKeyboard

class BahamutCommandTheSameTitleDown(fromArticleIndex: Int) : TelnetCommand() {
    var articleIndex: Int = 0

    init {
        action = BahamutCommandDef.Companion.THE_SAME_TITLE_DOWN
        articleIndex = fromArticleIndex
    }

    override fun execute(telnetListPage: TelnetListPage) {
        if (telnetListPage.listType > 0) {
            if (articleIndex == telnetListPage.listCount) {
                ASCoroutine.ensureMainThread {
                    showShortToast("無下一篇同主題文章")
                    telnetListPage.onLoadItemFinished()
                }
                isDone = true
            } else {
                create()
                    .pushString(articleIndex.toString() + "\n")
                    .pushKey(TelnetKeyboard.DOWN_ARROW).sendToServer()
            }
        } else if (articleIndex > 0) {
            create()
                .pushString("$articleIndex\n]")
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
                ASCoroutine.ensureMainThread {
                    showShortToast("無下一篇同主題文章")
                    telnetListPage.onLoadItemFinished()
                }
                isDone = true
            } else {
                articleIndex = telnetListPageBlock?.selectedItemNumber!!
                isDone = false
            }
        } else if (telnetListPage.isItemLoadingByNumber(telnetListPageBlock?.selectedItemNumber!! )) {
            ASCoroutine.ensureMainThread {
                showShortToast("無下一篇同主題文章")
                telnetListPage.onLoadItemFinished()
            }
            isDone = true
        } else if (!telnetListPage.isEnabled(telnetListPageBlock.selectedItemNumber - 1)) {
            showShortToast("下一篇不可使用")
            telnetListPage.onLoadItemFinished()
            isDone = true
        } else {
            telnetListPage.loadItemAtNumber(telnetListPageBlock.selectedItemNumber)
            isDone = true
        }
    }

    override fun toString(): String {
        return "[TheSameTitleDown][articleIndex=$articleIndex]"
    }
}
