package com.kota.Bahamut.Command;

import androidx.annotation.NonNull;

import com.kota.ASFramework.Thread.ASRunner;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Telnet.Reference.TelnetKeyboard;
import com.kota.Telnet.TelnetOutputBuilder;

public class BahamutCommandTheSameTitleTop extends TelnetCommand {
    int _article_index;

    public BahamutCommandTheSameTitleTop(int articleIndex) {
        Action = LoadForwardSameTitleItem;
        _article_index = articleIndex;
    }

    public void execute(final TelnetListPage aListPage) {
        if (aListPage.getListType()>0) {
            // 找出沒被block的最小index
            int miniumAvailableIndex = 1;
            if (_article_index == miniumAvailableIndex) {
                new ASRunner() {
                    public void run() {
                        ASToast.showShortToast("找沒有了耶...:(");
                        aListPage.onLoadItemFinished();
                    }
                }.runInMainThread();
                setDone(true);
            } else {
                TelnetOutputBuilder.create()
                        .pushString(miniumAvailableIndex + "\n")
                        .sendToServer();
            }
        } else if (_article_index > 0) {
            TelnetOutputBuilder.create()
                    .pushString(_article_index + "\n\\")
                    .sendToServer();
        } else {
            setDone(true);
        }
    }

    public void executeFinished(final TelnetListPage aListPage, TelnetListPageBlock aPageData) {
        if (aPageData.selectedItem.isDeleted || aListPage.isItemBlocked(aPageData.selectedItem)) {
            if (_article_index == aPageData.selectedItemNumber) {
                new ASRunner() {
                    public void run() {
                        aListPage.onLoadItemFinished();
                    }
                }.runInMainThread();
                setDone(true);
            } else {
//                _article_index = aPageData.selectedItemNumber;

                TelnetOutputBuilder.create()
                        .pushKey(TelnetKeyboard.DOWN_ARROW)
                        .sendToServer();
                setDone(false);
            }
        } else if (aListPage.isItemLoadingByNumber(aPageData.selectedItemNumber)) {
            new ASRunner() {
                public void run() {
                    ASToast.showShortToast("找沒有了耶...:(");
                    aListPage.onLoadItemFinished();
                }
            }.runInMainThread();
            setDone(true);
        } else if (!aListPage.isEnabled(aPageData.selectedItemNumber - 1)) {
            ASToast.showShortToast("上一篇不可使用");
            aListPage.onLoadItemFinished();
            setDone(true);
        } else {
            aListPage.loadItemAtNumber(aPageData.selectedItemNumber);
            setDone(true);
        }
    }

    @NonNull
    public String toString() {
        return "[BahamutCommandTheSameTitleTop][articleIndex=" + _article_index + "]";
    }
}
