package com.kota.Bahamut.command

import com.kota.asFramework.thread.ASRunner
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnet.TelnetOutputBuilder.Companion.create

class BahamutCommandTheSameTitleBottom(fromArticleIndex: Int) : TelnetCommand() {
    var articleIndex: Int = 0

    init {
        action = BahamutCommandDef.Companion.LOAD_FORWARD_SAME_ARTICLE
        articleIndex = fromArticleIndex
    }

    override fun execute(telnetListPage: TelnetListPage) {
        if (telnetListPage.listType > 0) {
            // 找出沒被block的最大index
            var maximumAvailableIndex = 1
            val itemSize = telnetListPage.itemSize
            for (i in itemSize - 1 downTo 0) {
                val item = telnetListPage.getItem(i)
                if (item != null && !item.isDeleted && !telnetListPage.isItemBlocked(item)) {
                    maximumAvailableIndex = (i + 1)
                    break
                }
            }

            if (articleIndex == maximumAvailableIndex) {
                object : ASRunner() {
                    override fun run() {
                        showShortToast("找沒有了耶...:(")
                        telnetListPage.onLoadItemFinished()
                    }
                }.runInMainThread()
                isDone = true
            } else {
                create()
                    .pushString(maximumAvailableIndex.toString() + "\n")
                    .sendToServer()
            }
        } else if (articleIndex > 0) {
            create()
                .pushKey(TelnetKeyboard.END)
                .pushString("[")
                .sendToServer()
        } else {
            isDone = true
        }
    }

    override fun executeFinished(
        telnetListPage: TelnetListPage,
        telnetListPageBlock: TelnetListPageBlock?
    ) {
        if (telnetListPageBlock.selectedItem?.isDeleted || telnetListPage.isItemBlocked(telnetListPageBlock.selectedItem)) {
            if (articleIndex == telnetListPageBlock.selectedItemNumber) {
                object : ASRunner() {
                    override fun run() {
                        telnetListPage.onLoadItemFinished()
                    }
                }.runInMainThread()
                isDone = true
            } else {
                articleIndex = telnetListPageBlock.selectedItemNumber
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
            showShortToast("下一篇不可使用")
            telnetListPage.onLoadItemFinished()
            isDone = true
        } else {
            telnetListPage.loadItemAtNumber(telnetListPageBlock.selectedItemNumber)
            isDone = true
        }
    }

    override fun toString(): String {
        return "[BahamutCommandTheSameTitleBottom][articleIndex=$articleIndex]"
    }
}
