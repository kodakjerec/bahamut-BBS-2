package com.kota.Bahamut.Command

import com.kota.ASFramework.Thread.ASRunner
import com.kota.ASFramework.UI.ASToast.showShortToast
import com.kota.Bahamut.ListPage.TelnetListPage
import com.kota.Bahamut.ListPage.TelnetListPageBlock
import com.kota.Telnet.Reference.TelnetKeyboard
import com.kota.Telnet.TelnetOutputBuilder.Companion.create

class BahamutCommandTheSameTitleUp(articleIndex: Int) : TelnetCommand() {
    var _article_index: Int

    init {
        this.Action = BahamutCommandDefs.Companion.LoadForwardSameTitleItem
        this._article_index = articleIndex
    }

    override fun execute(aListPage: TelnetListPage) {
        if (aListPage.getListType() > 0) {
            if (this._article_index == 1) {
                object : ASRunner() {
                    public override fun run() {
                        showShortToast("無上一篇同主題文章")
                        aListPage.onLoadItemFinished()
                    }
                }.runInMainThread()
                setDone(true)
            } else {
                create()
                    .pushString(this._article_index.toString() + "\n")
                    .pushKey(TelnetKeyboard.UP_ARROW).sendToServer()
            }
        } else if (this._article_index > 0) {
            create()
                .pushString(this._article_index.toString() + "\n[")
                .sendToServer()
        } else {
            setDone(true)
        }
    }

    override fun executeFinished(aListPage: TelnetListPage, aPageData: TelnetListPageBlock) {
        if (aPageData.selectedItem.isDeleted || aListPage.isItemBlocked(aPageData.selectedItem)) {
            if (_article_index == aPageData.selectedItemNumber) {
                object : ASRunner() {
                    public override fun run() {
                        showShortToast("無上一篇同主題文章")
                        aListPage.onLoadItemFinished()
                    }
                }.runInMainThread()
                setDone(true)
            } else {
                this._article_index = aPageData.selectedItemNumber
                setDone(false)
            }
        } else if (aListPage.isItemLoadingByNumber(aPageData.selectedItemNumber)) {
            object : ASRunner() {
                public override fun run() {
                    showShortToast("無上一篇同主題文章")
                    aListPage.onLoadItemFinished()
                }
            }.runInMainThread()
            setDone(true)
        } else if (!aListPage.isEnabled(aPageData.selectedItemNumber - 1)) {
            showShortToast("上一篇不可使用")
            aListPage.onLoadItemFinished()
            setDone(true)
        } else {
            aListPage.loadItemAtNumber(aPageData.selectedItemNumber)
            setDone(true)
        }
    }

    override fun toString(): String {
        return "[TheSameTitleUp][articleIndex=" + this._article_index + "]"
    }
}
