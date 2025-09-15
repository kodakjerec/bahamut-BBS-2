package com.kota.Bahamut.Command

import com.kota.ASFramework.Thread.ASRunner
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.ListPage.TelnetListPage
import com.kota.Bahamut.ListPage.TelnetListPageBlock
import com.kota.Bahamut.ListPage.TelnetListPageItem
import com.kota.Telnet.Reference.TelnetKeyboard
import com.kota.Telnet.TelnetOutputBuilder

class BahamutCommandTheSameTitleBottom(private var _article_index: Int) : TelnetCommand() {
    
    init {
        Action = BahamutCommandDefs.LoadForwardSameTitleItem
    }

    override fun execute(aListPage: TelnetListPage) {
        if (aListPage.listType > 0) {
            // 找出沒被block的最大index
            var maximumAvailableIndex = 1
            val itemSize = aListPage.itemSize
            for (i in itemSize - 1 downTo 0) {
                val item: TelnetListPageItem? = aListPage.getItem(i)
                if (item != null && !item.isDeleted && !aListPage.isItemBlocked(item)) {
                    maximumAvailableIndex = i + 1
                    break
                }
            }

            if (_article_index == maximumAvailableIndex) {
                ASRunner {
                    ASToast.showShortToast("找沒有了耶...:(")
                    aListPage.onLoadItemFinished()
                }.runInMainThread()
                setDone(true)
            } else {
                TelnetOutputBuilder.create()
                    .pushString("$maximumAvailableIndex\n")
                    .sendToServer()
            }
        } else if (_article_index > 0) {
            TelnetOutputBuilder.create()
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
                ASRunner {
                    aListPage.onLoadItemFinished()
                }.runInMainThread()
                setDone(true)
            } else {
                _article_index = aPageData.selectedItemNumber
                setDone(false)
            }
        } else if (aListPage.isItemLoadingByNumber(aPageData.selectedItemNumber)) {
            ASRunner {
                ASToast.showShortToast("找沒有了耶...:(")
                aListPage.onLoadItemFinished()
            }.runInMainThread()
            setDone(true)
        } else if (!aListPage.isEnabled(aPageData.selectedItemNumber - 1)) {
            ASToast.showShortToast("下一篇不可使用")
            aListPage.onLoadItemFinished()
            setDone(true)
        } else {
            aListPage.loadItemAtNumber(aPageData.selectedItemNumber)
            setDone(true)
        }
    }

    override fun toString(): String {
        return "[BahamutCommandTheSameTitleBottom][articleIndex=$_article_index]"
    }
}
