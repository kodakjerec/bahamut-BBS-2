package com.kota.Bahamut.Command;

import androidx.annotation.NonNull;

import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;

public class BahamutCommandMoveToLastBlock extends BahamutCommandLoadLastBlock {
    public BahamutCommandMoveToLastBlock() {
        this.Action = MoveToLastBlock;
    }

    public void executeFinished(TelnetListPage aListPage, TelnetListPageBlock aPageData) {
        super.executeFinished(aListPage, aPageData);
        aListPage.pushRefreshCommand(1);
        setDone(true);
    }

    @NonNull
    public String toString() {
        return "[MoveToLastBlock]";
    }
}
