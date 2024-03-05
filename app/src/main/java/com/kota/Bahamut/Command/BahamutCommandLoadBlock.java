package com.kota.Bahamut.Command;

import androidx.annotation.NonNull;

import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Telnet.Logic.ItemUtils;
import com.kota.Telnet.TelnetClient;

public class BahamutCommandLoadBlock extends TelnetCommand {
    int _block;

    public BahamutCommandLoadBlock(int aBlock) {
        this.Action = LoadBlock;
        this._block = aBlock;
    }

    public boolean containsArticle(int articleIndex) {
        return this._block == ItemUtils.getBlock(articleIndex);
    }

    public boolean isOperationCommand() {
        return false;
    }

    public void execute(TelnetListPage aListPage) {
        if (this._block >= 0) {
            TelnetClient.getClient().sendStringToServer(String.valueOf((this._block * 20) + 1));
        }
    }

    public void executeFinished(TelnetListPage aListPage, TelnetListPageBlock aPageData) {
        setDone(true);
    }

    @NonNull
    public String toString() {
        return "[LoadBlock][block=" + this._block + " targetIndex=" + ((this._block * 20) + 1) + "]";
    }
}
