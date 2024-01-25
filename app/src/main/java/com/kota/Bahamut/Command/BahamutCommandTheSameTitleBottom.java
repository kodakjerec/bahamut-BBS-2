package com.kota.Bahamut.Command;

import com.kota.ASFramework.Thread.ASRunner;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Telnet.Reference.TelnetKeyboard;
import com.kota.Telnet.TelnetOutputBuilder;

public class BahamutCommandTheSameTitleBottom extends TelnetCommand {
    int _article_index = 0;

    public BahamutCommandTheSameTitleBottom(int articleIndex) {
        this.Action = 10;
        this._article_index = articleIndex;
    }

    public void execute(final TelnetListPage aListPage) {
        if (aListPage.getListType() == 1 || aListPage.getListType() == 2) {
            if (this._article_index == aListPage.getItemSize()) {
                new ASRunner() {
                    public void run() {
                        ASToast.showShortToast("找沒有了耶...:(");
                        aListPage.onLoadItemFinished();
                    }
                }.runInMainThread();
                setDone(true);
                return;
            }
            TelnetOutputBuilder.create().pushString(aListPage.getItemSize() + "\n").sendToServer();
        } else if (this._article_index > 0) {
            TelnetOutputBuilder.create().pushKey(TelnetKeyboard.END).pushString("[").sendToServer();
        } else {
            setDone(true);
        }
    }

    public void executeFinished(final TelnetListPage aListPage, TelnetListPageBlock aPageData) {
        if (aPageData.selectedItem.isDeleted || aListPage.isItemBlocked(aPageData.selectedItem)) {
            this._article_index = aPageData.selectedItemNumber;
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

    public String toString() {
        return "[BahamutCommandTheSameTitleBottom][articleIndex=" + this._article_index + "]";
    }
}
