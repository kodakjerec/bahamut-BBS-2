package com.kota.Bahamut.Command

import com.kota.ASFramework.Thread.ASRunner
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.ListPage.TelnetListPage
import com.kota.Bahamut.ListPage.TelnetListPageBlock
import com.kota.Telnet.Reference.TelnetKeyboard
import com.kota.Telnet.TelnetOutputBuilder

class BahamutCommandTheSameTitleTop(private val _article_index: Int) : TelnetCommand() {
    
    init {
        Action = BahamutCommandDefs.LoadForwardSameTitleItem
    }

    override fun execute(aListPage: TelnetListPage) {
        if (aListPage.listType > 0) {
            // 找出沒被block的最小index
            val miniumAvailableIndex = 1
            if (_article_index == miniumAvailableIndex) {
                ASRunner {
                    ASToast.showShortToast("找沒有了耶...:(")
                    aListPage.onLoadItemFinished()
                }.runInMainThread()
                setDone(true)
            } else {
                TelnetOutputBuilder.create()
                    .pushString("$miniumAvailableIndex\n")
                    .sendToServer()
            }
        } else if (_article_index > 0) {
            TelnetOutputBuilder.create()
                .pushString("$_article_index\n\\")
                .sendToServer()
        } else {
            setDone(true)
        }
    }

    override fun executeFinished(aListPage: TelnetListPage, aPageData: TelnetListPageBlock) {
        if (aPageData.selectedItem.isDeleted || aListPage.isItemBlocked(aPageData.selectedItem)) {
            if (_article_index == aPageData.selectedItemNumber) {
                ASRunner {
                    aListPage.onLoadItemFinished()
                }.runInMainThread()
                setDone(true)
            } else {
                // _article_index = aPageData.selectedItemNumber;
                TelnetOutputBuilder.create()
                    .pushKey(TelnetKeyboard.DOWN_ARROW)
                    .sendToServer()
                setDone(false)
            }
        } else if (aListPage.isItemLoadingByNumber(aPageData.selectedItemNumber)) {
            ASRunner {
                ASToast.showShortToast("找沒有了耶...:(")
                aListPage.onLoadItemFinished()
            }.runInMainThread()
            setDone(true)
        } else if (!aListPage.isEnabled(aPageData.selectedItemNumber - 1)) {
            ASToast.showShortToast("上一篇不可使用")
            aListPage.onLoadItemFinished()
            setDone(true)
        } else {
            aListPage.loadItemAtNumber(aPageData.selectedItemNumber)
            setDone(true)
        }
    }

    override fun toString(): String {
        return "[BahamutCommandTheSameTitleTop][articleIndex=$_article_index]"
    }
}
