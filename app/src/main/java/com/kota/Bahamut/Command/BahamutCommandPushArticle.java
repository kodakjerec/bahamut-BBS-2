package com.kota.Bahamut.Command;

import androidx.annotation.NonNull;

import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Telnet.TelnetClient;

public class BahamutCommandPushArticle extends TelnetCommand {
    int _article_index;

    public BahamutCommandPushArticle(int articleIndex) {
        this._article_index = articleIndex;
        this.Action = PushArticle;
    }

    public void execute(TelnetListPage aListPage) {
        if (this._article_index > 0) {
            TelnetClient.getClient().sendStringToServer(this._article_index + "\ngx\n");
        }
    }

    public void executeFinished(TelnetListPage aListPage, TelnetListPageBlock aPageData) {
        setDone(true);
    }

    @NonNull
    public String toString() {
        return "[PushArticle][articleIndex=" + this._article_index + "]";
    }
}
