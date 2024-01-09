package com.kota.Bahamut.Command;

import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Telnet.Logic.ItemUtils;
import com.kota.Telnet.TelnetClient;

public class BahamutCommandLoadBlock extends TelnetCommand {
    private int _block = -1;

    public BahamutCommandLoadBlock(int aBlock) {
        this.Action = 0;
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

    public String toString() {
        return "[LoadBlock][block=" + this._block + " targetIndex=" + ((this._block * 20) + 1) + "]";
    }
}
