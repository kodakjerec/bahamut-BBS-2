package com.kota.Bahamut.Command;

import static com.kota.Bahamut.BahamutStateHandler.*;

import com.kota.Bahamut.BahamutStateHandler;
import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Telnet.TelnetClient;

public class BahamutCommandLoadArticle extends TelnetCommand {
    int _article_number = 0;

    public BahamutCommandLoadArticle(int articleNumber) {
        this._article_number = articleNumber;
        this.Action = 5;
    }

    public int getArticleIndex() {
        return this._article_number;
    }

    public void execute(TelnetListPage aListPage) {
        if (this._article_number > 0) {
            String article_number = String.valueOf(this._article_number);
            BahamutStateHandler.getInstance().setArticleNumber(article_number);
            TelnetClient.getClient().sendStringToServerInBackground(article_number + "\n");
        }
    }

    public void executeFinished(TelnetListPage aListPage, TelnetListPageBlock aPageData) {
        setDone(true);
    }

    public String toString() {
        return "[LoadArticle][articleIndex=" + this._article_number + "]";
    }
}
