package com.kota.Bahamut.Command

import com.kota.ASFramework.Thread.ASRunner
import com.kota.ASFramework.UI.ASToast.showShortToast
import com.kota.Bahamut.ListPage.TelnetListPage
import com.kota.Bahamut.ListPage.TelnetListPageBlock
import com.kota.Telnet.Reference.TelnetKeyboard
import com.kota.Telnet.TelnetOutputBuilder.Companion.create

class BahamutCommandTheSameTitleBottom(articleIndex: Int) : TelnetCommand() {
    var _article_index: Int = 0

    init {
        Action = BahamutCommandDefs.Companion.LoadForwardSameTitleItem
        _article_index = articleIndex
    }

    override fun execute(aListPage: TelnetListPage) {
        if (aListPage.getListType() > 0) {
            // 找出沒被block的最大index
            var maximumAvailableIndex = 1
            val itemSize = aListPage.getItemSize()
            for (i in itemSize - 1 downTo 0) {
                val item = aListPage.getItem(i)
                if (item != null && !item.isDeleted && !aListPage.isItemBlocked(item)) {
                    maximumAvailableIndex = (i + 1)
                    break
                }
            }

            if (_article_index == maximumAvailableIndex) {
                object : ASRunner() {
                    public override fun run() {
                        showShortToast("找沒有了耶...:(")
                        aListPage.onLoadItemFinished()
                    }
                }.runInMainThread()
                setDone(true)
            } else {
                create()
                    .pushString(maximumAvailableIndex.toString() + "\n")
                    .sendToServer()
            }
        } else if (_article_index > 0) {
            create()
                .pushKey(TelnetKeyboard.END)
                .pushString("[")
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
                _article_index = aPageData.selectedItemNumber
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
            showShortToast("下一篇不可使用")
            aListPage.onLoadItemFinished()
            setDone(true)
        } else {
            aListPage.loadItemAtNumber(aPageData.selectedItemNumber)
            setDone(true)
        }
    }

    override fun toString(): String {
        return "[BahamutCommandTheSameTitleBottom][articleIndex=" + _article_index + "]"
    }
}
