package com.kota.Bahamut.Command;

import androidx.annotation.NonNull;

import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Telnet.TelnetClient;

public class BahamutCommandListArticle extends TelnetCommand {
    int _article_index;

    public BahamutCommandListArticle(int articleIndex) {
        this._article_index = articleIndex;
        this.Action = ListArticle;
    }

    public void execute(TelnetListPage aListPage) {
        if (this._article_index > 0) {
            TelnetClient.getClient().sendDataToServer((this._article_index + "\nS").getBytes());
        }
    }

    public void executeFinished(TelnetListPage aListPage, TelnetListPageBlock aPageData) {
        setDone(true);
    }

    @NonNull
    public String toString() {
        return "[ListArticle][articleIndex=" + this._article_index + "]";
    }
}
