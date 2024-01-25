package com.kota.Bahamut.Command;

import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Telnet.TelnetClient;

public class BahamutCommandDeleteArticle extends TelnetCommand {
    int _article_index = 0;

    public BahamutCommandDeleteArticle(int articleIndex) {
        this._article_index = articleIndex;
        this.Action = 7;
    }

    public void execute(TelnetListPage aListPage) {
        if (this._article_index > 0) {
            TelnetClient.getClient().sendStringToServer(this._article_index + "\ndy");
        }
    }

    public void executeFinished(TelnetListPage aListPage, TelnetListPageBlock aPageData) {
        setDone(true);
    }

    public String toString() {
        return "[DeleteArticle][articleIndex=" + this._article_index + "]";
    }
}
