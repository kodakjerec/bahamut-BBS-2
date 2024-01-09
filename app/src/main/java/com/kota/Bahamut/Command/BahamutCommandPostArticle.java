package com.kota.Bahamut.Command;

import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Telnet.TelnetOutputBuilder;

public class BahamutCommandPostArticle extends TelnetCommand {
    String _article_number = null;
    String _content = "";
    TelnetListPage _list_page;
    String _sign = "";
    String _target = "F";
    String _title = "";

    public BahamutCommandPostArticle(TelnetListPage aListPage, String title, String content, String aTarget, String aArticleNumber, String aSign) {
        this._title = title;
        this._content = content;
        this.Action = 8;
        this._target = aTarget;
        this._article_number = aArticleNumber;
        this._list_page = aListPage;
        this._sign = aSign;
        if (this._sign == null) {
            this._sign = "";
        }
    }

    public void execute(TelnetListPage aListPage) {
        TelnetOutputBuilder buffer = TelnetOutputBuilder.create();
        if (this._article_number == null || this._target == null) {
            buffer.pushKey(16);
            buffer.pushData((byte) 13);
            buffer.pushString(this._title);
            buffer.pushString("\n" + this._sign + "\n");
            buffer.pushString(this._content);
            buffer.pushKey(24);
            buffer.pushString("s\n");
            buffer.sendToServer();
            return;
        }
        buffer.pushString(this._article_number + "\ny" + this._target + "\n");
        if (this._target != null && (this._target.equals("F") || this._target.equals("B"))) {
            buffer.pushString("\n");
        }
        if (this._title != null) {
            buffer.pushKey(25);
            buffer.pushString(this._title);
            buffer.pushString("\nn\n" + this._sign + "\n");
        } else {
            buffer.pushString("\nn\n" + this._sign + "\n");
        }
        buffer.pushString(this._content);
        buffer.pushKey(24);
        buffer.pushString("s\n");
        if (this._target.equals("M")) {
            buffer.pushString("Y\n\n");
        }
        buffer.sendToServer();
    }

    public void executeFinished(TelnetListPage aListPage, TelnetListPageBlock aPageData) {
        aListPage.pushPreloadCommand(0);
        setDone(true);
    }

    public String toString() {
        return "[PostArticle][title=" + this._title + " content" + this._content + "]";
    }
}
