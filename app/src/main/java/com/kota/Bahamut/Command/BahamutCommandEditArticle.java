package com.kota.Bahamut.Command;

import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Telnet.TelnetClient;
import com.kota.Telnet.TelnetOutputBuilder;
import com.kota.Telnet.TelnetCommand;

public class BahamutCommandEditArticle extends com.kota.Bahamut.Command.TelnetCommand {
    String _article_number;
    String _content = "";
    String _title = "";

    public BahamutCommandEditArticle(String aArticleNumber, String title, String content) {
        this._article_number = aArticleNumber;
        this._title = title;
        this._content = content;
        this.Action = 8;
    }

    public void execute(TelnetListPage aListPage) {
        if (this._article_number != null && this._content != null && this._content.length() > 0) {
            TelnetOutputBuilder builder = TelnetOutputBuilder.create().pushString(this._article_number + "\nE").pushData((byte) 7).pushData((byte) 25).pushString(this._content).pushData((byte) TelnetCommand.TERMINAL_TYPE).pushString("S\n");
            if (this._title != null) {
                builder.pushString("T").pushData((byte) 25).pushString(this._title + "\nY\n");
            }
            TelnetClient.getClient().sendDataToServer(builder.build());
        }
    }

    public void executeFinished(TelnetListPage aListPage, TelnetListPageBlock aPageData) {
        setDone(true);
    }

    public String toString() {
        return "[EditArticle][articleIndex=" + this._article_number + " title=" + this._title + " content=" + this._content + "]";
    }
}
