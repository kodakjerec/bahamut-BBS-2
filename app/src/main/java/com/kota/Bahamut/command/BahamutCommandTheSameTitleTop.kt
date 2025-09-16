package com.kota.Bahamut.command

import com.kota.asFramework.thread.ASRunner
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnet.TelnetOutputBuilder.Companion.create

class BahamutCommandTheSameTitleTop(articleIndex: Int) : TelnetCommand() {
    var _article_index: Int

    init {
        Action = BahamutCommandDefs.Companion.LoadForwardSameTitleItem
        _article_index = articleIndex
    }

    override fun execute(aListPage: TelnetListPage) {
        if (aListPage.getListType() > 0) {
            // 找出沒被block的最小index
            val miniumAvailableIndex = 1
            if (_article_index == miniumAvailableIndex) {
                object : ASRunner() {
                    public override fun run() {
                        showShortToast("找沒有了耶...:(")
                        aListPage.onLoadItemFinished()
                    }
                }.runInMainThread()
                setDone(true)
            } else {
                create()
                    .pushString(miniumAvailableIndex.toString() + "\n")
                    .sendToServer()
            }
        } else if (_article_index > 0) {
            create()
                .pushString(_article_index.toString() + "\n\\")
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
                        aListPage.onLoadItemFinished()
                    }
                }.runInMainThread()
                setDone(true)
            } else {
//                _article_index = aPageData.selectedItemNumber;

                create()
                    .pushKey(TelnetKeyboard.DOWN_ARROW)
                    .sendToServer()
                setDone(false)
            }
        } else if (aListPage.isItemLoadingByNumber(aPageData.selectedItemNumber)) {
            object : ASRunner() {
                public override fun run() {
                    showShortToast("找沒有了耶...:(")
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
        return "[BahamutCommandTheSameTitleTop][articleIndex=" + _article_index + "]"
    }
}
