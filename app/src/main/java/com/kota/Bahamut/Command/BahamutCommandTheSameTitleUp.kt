package com.kota.Bahamut.Command

import com.kota.ASFramework.Thread.ASRunner
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.ListPage.TelnetListPage
import com.kota.Bahamut.ListPage.TelnetListPageBlock
import com.kota.Telnet.Reference.TelnetKeyboard
import com.kota.Telnet.TelnetOutputBuilder

class BahamutCommandTheSameTitleUp(private var _article_index: Int) : TelnetCommand() {
    
    init {
        Action = BahamutCommandDefs.LoadForwardSameTitleItem
    }

    override fun execute(aListPage: TelnetListPage) {
        if (aListPage.listType > 0) {
            if (_article_index == 1) {
                ASRunner {
                    ASToast.showShortToast("無上一篇同主題文章")
                    aListPage.onLoadItemFinished()
                }.runInMainThread()
                setDone(true)
            } else {
                TelnetOutputBuilder.create()
                    .pushString("$_article_index\n")
                    .pushKey(TelnetKeyboard.UP_ARROW).sendToServer()
            }
        } else if (_article_index > 0) {
            TelnetOutputBuilder.create()
                .pushString("$_article_index\n[")
                .sendToServer()
        } else {
            setDone(true)
        }
    }

    override fun executeFinished(aListPage: TelnetListPage, aPageData: TelnetListPageBlock) {
        if (aPageData.selectedItem.isDeleted || aListPage.isItemBlocked(aPageData.selectedItem)) {
            if (_article_index == aPageData.selectedItemNumber) {
                ASRunner {
                    ASToast.showShortToast("無上一篇同主題文章")
                    aListPage.onLoadItemFinished()
                }.runInMainThread()
                setDone(true)
            } else {
                _article_index = aPageData.selectedItemNumber
                setDone(false)
            }
        } else if (aListPage.isItemLoadingByNumber(aPageData.selectedItemNumber)) {
            ASRunner {
                ASToast.showShortToast("無上一篇同主題文章")
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
        return "[TheSameTitleUp][articleIndex=$_article_index]"
    }
}
