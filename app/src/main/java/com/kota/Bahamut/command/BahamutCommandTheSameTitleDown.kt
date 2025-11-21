package com.kota.Bahamut.command

import com.kota.asFramework.thread.ASRunner
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnet.TelnetOutputBuilder.Companion.create

class BahamutCommandTheSameTitleDown(fromArticleIndex: Int) : TelnetCommand() {
    var articleIndex: Int = 0

    init {
        action = BahamutCommandDef.Companion.THE_SAME_TITLE_DOWN
        articleIndex = fromArticleIndex
    }

    override fun execute(telnetListPage: TelnetListPage) {
        if (telnetListPage.listType > 0) {
            if (articleIndex == telnetListPage.listCount) {
                object : ASRunner() {
                    override fun run() {
                        showShortToast("無下一篇同主題文章")
                        telnetListPage.onLoadItemFinished()
                    }
                }.runInMainThread()
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
                object : ASRunner() {
                    override fun run() {
                        showShortToast("無下一篇同主題文章")
                        telnetListPage.onLoadItemFinished()
                    }
                }.runInMainThread()
                isDone = true
            } else {
                articleIndex = telnetListPageBlock?.selectedItemNumber!!
                isDone = false
            }
        } else if (telnetListPage.isItemLoadingByNumber(telnetListPageBlock?.selectedItemNumber!! )) {
            object : ASRunner() {
                override fun run() {
                    showShortToast("無下一篇同主題文章")
                    telnetListPage.onLoadItemFinished()
                }
            }.runInMainThread()
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
