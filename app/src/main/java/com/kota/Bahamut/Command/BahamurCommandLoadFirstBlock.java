package com.kota.Bahamut.Command;

import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;

public class BahamurCommandLoadFirstBlock extends TelnetCommand {
    public BahamurCommandLoadFirstBlock() {
        this.Action = 1;
    }

    public void execute(TelnetListPage aListPage) {
    }

    public void executeFinished(TelnetListPage aListPage, TelnetListPageBlock aPageData) {
        setDone(true);
    }

    public String toString() {
        return "[LoadFirstBlock]";
    }
}
