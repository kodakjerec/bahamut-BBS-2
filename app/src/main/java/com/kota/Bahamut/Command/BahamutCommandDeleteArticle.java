package com.kota.Bahamut.Command;

import androidx.annotation.NonNull;

import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Telnet.TelnetClient;

public class BahamutCommandDeleteArticle extends TelnetCommand {
    int _article_index;

    public BahamutCommandDeleteArticle(int articleIndex) {
        _article_index = articleIndex;
        Action = DeleteArticle;
    }

    public void execute(TelnetListPage aListPage) {
        if (_article_index > 0) {
            TelnetClient.getClient().sendStringToServer(_article_index + "\ndy");
        }
    }

    public void executeFinished(TelnetListPage aListPage, TelnetListPageBlock aPageData) {
        setDone(true);
    }

    @NonNull
    public String toString() {
        return "[DeleteArticle][articleIndex=" + _article_index + "]";
    }
}
