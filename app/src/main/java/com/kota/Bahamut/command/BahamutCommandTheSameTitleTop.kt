package com.kota.Bahamut.command

import com.kota.asFramework.thread.ASRunner
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnet.TelnetOutputBuilder.Companion.create

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
                object : ASRunner() {
                    override fun run() {
                        showShortToast("找沒有了耶...:(")
                        telnetListPage.onLoadItemFinished()
                    }
                }.runInMainThread()
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

    override fun executeFinished(telnetListPage: TelnetListPage, telnetListPageBlock: TelnetListPageBlock) {
        if (telnetListPageBlock.selectedItem!!.isDeleted || telnetListPage.isItemBlocked(telnetListPageBlock.selectedItem)) {
            if (articleIndex == telnetListPageBlock.selectedItemNumber) {
                object : ASRunner() {
                    override fun run() {
                        telnetListPage.onLoadItemFinished()
                    }
                }.runInMainThread()
                isDone = true
            } else {
//                articleIndex = aPageData.selectedItemNumber;

                create()
                    .pushKey(TelnetKeyboard.DOWN_ARROW)
                    .sendToServer()
                isDone = false
            }
        } else if (telnetListPage.isItemLoadingByNumber(telnetListPageBlock.selectedItemNumber)) {
            object : ASRunner() {
                override fun run() {
                    showShortToast("找沒有了耶...:(")
                    telnetListPage.onLoadItemFinished()
                }
            }.runInMainThread()
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
