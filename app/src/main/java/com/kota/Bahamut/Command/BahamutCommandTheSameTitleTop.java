package com.kota.Bahamut.Command;

import androidx.annotation.NonNull;

import com.kota.ASFramework.Thread.ASRunner;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Telnet.TelnetOutputBuilder;

public class BahamutCommandTheSameTitleTop extends TelnetCommand {
    int _article_index;

    public BahamutCommandTheSameTitleTop(int articleIndex) {
        Action = LoadForwardSameTitleItem;
        _article_index = articleIndex;
    }

    public void execute(final TelnetListPage aListPage) {
        if (aListPage.getListType() == 1 || aListPage.getListType() == 2) {
            if (_article_index == 1) {
                new ASRunner() {
                    public void run() {
                        ASToast.showShortToast("找沒有了耶...:(");
                        aListPage.onLoadItemFinished();
                    }
                }.runInMainThread();
                setDone(true);
                return;
            }
            TelnetOutputBuilder.create()
                    .pushString("1\n").sendToServer();
        } else if (_article_index > 0) {
            TelnetOutputBuilder.create()
                    .pushString(_article_index + "\n\\").sendToServer();
        } else {
            setDone(true);
        }
    }

    public void executeFinished(final TelnetListPage aListPage, TelnetListPageBlock aPageData) {
        if (aPageData.selectedItem.isDeleted || aListPage.isItemBlocked(aPageData.selectedItem)) {
            _article_index = aPageData.selectedItemNumber;
            setDone(false);
        } else if (aListPage.isItemLoadingByNumber(aPageData.selectedItemNumber)) {
            new ASRunner() {
                public void run() {
                    ASToast.showShortToast("找沒有了耶...:(");
                    aListPage.onLoadItemFinished();
                }
            }.runInMainThread();
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
