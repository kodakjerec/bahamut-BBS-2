package com.kota.Bahamut.Command;

import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Telnet.TelnetClient;

public class BahamutCommandGoodArticle extends TelnetCommand {
    int _article_index = 0;

    public BahamutCommandGoodArticle(int articleIndex) {
        this._article_index = articleIndex;
        this.Action = 9;
    }

    public void execute(TelnetListPage aListPage) {
        if (this._article_index > 0) {
            TelnetClient.getClient().sendStringToServer(this._article_index + "\ngy");
        }
    }

    public void executeFinished(TelnetListPage aListPage, TelnetListPageBlock aPageData) {
        setDone(true);
    }

    public String toString() {
        return "[GoodArticle][articleIndex=" + this._article_index + "]";
    }
}
