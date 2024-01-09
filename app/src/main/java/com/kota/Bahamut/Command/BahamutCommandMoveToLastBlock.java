package com.kota.Bahamut.Command;

import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;

public class BahamutCommandMoveToLastBlock extends BahamutCommandLoadLastBlock {
    public BahamutCommandMoveToLastBlock() {
        this.Action = 3;
    }

    public void executeFinished(TelnetListPage aListPage, TelnetListPageBlock aPageData) {
        super.executeFinished(aListPage, aPageData);
        aListPage.pushRefreshCommand(1);
        setDone(true);
    }

    public String toString() {
        return "[MoveToLastBlock]";
    }
}
