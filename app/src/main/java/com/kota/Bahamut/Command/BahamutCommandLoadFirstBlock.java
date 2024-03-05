package com.kota.Bahamut.Command;

import androidx.annotation.NonNull;

import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;

public class BahamutCommandLoadFirstBlock extends TelnetCommand {
    public BahamutCommandLoadFirstBlock() {
        this.Action = LoadFirstBlock;
    }

    public void execute(TelnetListPage aListPage) {
    }

    public void executeFinished(TelnetListPage aListPage, TelnetListPageBlock aPageData) {
        setDone(true);
    }

    @NonNull
    public String toString() {
        return "[LoadFirstBlock]";
    }
}
