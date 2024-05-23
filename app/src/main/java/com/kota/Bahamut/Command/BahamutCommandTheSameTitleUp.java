package com.kota.Bahamut.Command;

import androidx.annotation.NonNull;

import com.kota.ASFramework.Thread.ASRunner;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Telnet.Reference.TelnetKeyboard;
import com.kota.Telnet.TelnetOutputBuilder;

public class BahamutCommandTheSameTitleUp extends TelnetCommand {
    int _article_index;

    public BahamutCommandTheSameTitleUp(int articleIndex) {
        this.Action = LoadForwardSameTitleItem;
        this._article_index = articleIndex;
    }

    public void execute(final TelnetListPage aListPage) {
        if (aListPage.getListType() == 1 || aListPage.getListType() == 2) {
            if (this._article_index == 1) {
                new ASRunner() {
                    public void run() {
                        ASToast.showShortToast("無上一篇同主題文章");
                        aListPage.onLoadItemFinished();
                    }
                }.runInMainThread();
                setDone(true);
            } else {
                TelnetOutputBuilder.create()
                        .pushString(this._article_index + "\n")
                        .pushKey(TelnetKeyboard.UP_ARROW).sendToServer();
            }
        } else if (this._article_index > 0) {
            TelnetOutputBuilder.create()
                    .pushString(this._article_index + "\n[")
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
                        ASToast.showShortToast("無上一篇同主題文章");
                        aListPage.onLoadItemFinished();
                    }
                }.runInMainThread();
                setDone(true);
            } else {
                this._article_index = aPageData.selectedItemNumber;
                setDone(false);
            }
        } else if (aListPage.isItemLoadingByNumber(aPageData.selectedItemNumber)) {
            new ASRunner() {
                public void run() {
                    ASToast.showShortToast("無上一篇同主題文章");
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
        return "[TheSameTitleUp][articleIndex=" + this._article_index + "]";
    }
}
