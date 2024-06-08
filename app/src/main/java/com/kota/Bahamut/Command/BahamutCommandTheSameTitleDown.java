package com.kota.Bahamut.Command;

import androidx.annotation.NonNull;

import com.kota.ASFramework.Thread.ASRunner;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Telnet.Reference.TelnetKeyboard;
import com.kota.Telnet.TelnetOutputBuilder;

public class BahamutCommandTheSameTitleDown extends TelnetCommand {
    int _article_index = 0;

    public BahamutCommandTheSameTitleDown(int articleIndex) {
        Action = TheSameTitleDown;
        _article_index = articleIndex;
    }

    public void execute(final TelnetListPage aListPage) {
        if (aListPage.getListType()>0) {
            if (_article_index == aListPage.getItemSize()) {
                new ASRunner() {
                    public void run() {
                        ASToast.showShortToast("無下一篇同主題文章");
                        aListPage.onLoadItemFinished();
                    }
                }.runInMainThread();
                setDone(true);
            } else {
                TelnetOutputBuilder.create()
                        .pushString(_article_index + "\n")
                        .pushKey(TelnetKeyboard.DOWN_ARROW).sendToServer();
            }
        } else if (_article_index > 0) {
            TelnetOutputBuilder.create()
                    .pushString(_article_index + "\n]")
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
                        ASToast.showShortToast("無下一篇同主題文章");
                        aListPage.onLoadItemFinished();
                    }
                }.runInMainThread();
                setDone(true);
            } else {
                _article_index = aPageData.selectedItemNumber;
                setDone(false);
            }
        } else if (aListPage.isItemLoadingByNumber(aPageData.selectedItemNumber)) {
            new ASRunner() {
                public void run() {
                    ASToast.showShortToast("無下一篇同主題文章");
                    aListPage.onLoadItemFinished();
                }
            }.runInMainThread();
            setDone(true);
        } else if (!aListPage.isEnabled(aPageData.selectedItemNumber - 1)) {
            ASToast.showShortToast("下一篇不可使用");
            aListPage.onLoadItemFinished();
            setDone(true);
        } else {
            aListPage.loadItemAtNumber(aPageData.selectedItemNumber);
            setDone(true);
        }
    }

    @NonNull
    public String toString() {
        return "[TheSameTitleDown][articleIndex=" + _article_index + "]";
    }
}
