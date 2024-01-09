package com.kota.Bahamut.Command;

import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Telnet.TelnetClient;

public class BahamutCommandListArticle extends TelnetCommand {
    int _article_index = 0;

    public BahamutCommandListArticle(int articleIndex) {
        this._article_index = articleIndex;
        this.Action = 6;
    }

    public void execute(TelnetListPage aListPage) {
        if (this._article_index > 0) {
            TelnetClient.getClient().sendDataToServer((String.valueOf(this._article_index) + "\nS").getBytes());
        }
    }

    public void executeFinished(TelnetListPage aListPage, TelnetListPageBlock aPageData) {
        setDone(true);
    }

    public String toString() {
        return "[ListArticle][articleIndex=" + this._article_index + "]";
    }
}
