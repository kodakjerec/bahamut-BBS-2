package com.kota.Bahamut.Command;

import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Telnet.TelnetClient;

public class BahamutCommandSearchArticle extends TelnetCommand {
    private String _author = "";
    private String _gy = "";
    private String _keyword = "";
    private String _mark = "";

    public BahamutCommandSearchArticle(String keyword, String author, String mark, String gy) {
        this._keyword = keyword;
        this._author = author;
        this._mark = mark;
        this._gy = gy;
        this.Action = 4;
    }

    public void execute(TelnetListPage aListPage) {
        TelnetClient.getClient().sendStringToServerInBackground("~" + this._keyword + "\n" + this._author + "\n" + this._mark + "\n" + this._gy);
    }

    public void executeFinished(TelnetListPage aListPage, TelnetListPageBlock aPageData) {
        setDone(true);
    }

    public String toString() {
        return "[SearchArticle][keyword=" + this._keyword + " author=" + this._author + " mark=" + this._mark + " gy=" + this._gy + "]";
    }
}
