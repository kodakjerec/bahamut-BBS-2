package com.kota.Bahamut.Command;

import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;

public abstract class TelnetCommand implements BahamutCommandDefs {
    public int Action = 0;
    private boolean _is_done = false;
    public boolean recordTime = true;

    public abstract void execute(TelnetListPage telnetListPage);

    public abstract void executeFinished(TelnetListPage telnetListPage, TelnetListPageBlock telnetListPageBlock);

    public boolean isDone() {
        return this._is_done;
    }

    public void setDone(boolean done) {
        this._is_done = done;
    }

    public boolean isOperationCommand() {
        return true;
    }
}
