package com.kota.Bahamut.Command;

import androidx.annotation.NonNull;

import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Telnet.TelnetClient;

public class BahamutCommandSearchArticle extends TelnetCommand {
    String _author;
    String _gy;
    String _keyword;
    String _mark;

    public BahamutCommandSearchArticle(String keyword, String author, String mark, String gy) {
        _keyword = keyword;
        _author = author;
        _mark = mark;
        _gy = gy;
        Action = SearchArticle;
    }

    public void execute(TelnetListPage aListPage) {
        TelnetClient.getClient().sendStringToServerInBackground("~" + _keyword + "\n" + _author + "\n" + _mark + "\n" + _gy);
    }

    public void executeFinished(TelnetListPage aListPage, TelnetListPageBlock aPageData) {
        setDone(true);
    }

    @NonNull
    public String toString() {
        return "[SearchArticle][keyword=" + _keyword + " author=" + _author + " mark=" + _mark + " gy=" + _gy + "]";
    }
}
