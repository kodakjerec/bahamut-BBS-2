package com.kota.Bahamut.Command;

import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Telnet.Reference.TelnetKeyboard;
import com.kota.Telnet.TelnetOutputBuilder;

public class BahamutCommandLoadLastBlock extends TelnetCommand {

    public enum OperationMode {
        End,
        Left_Right_End,
        Home_End,
        Left_S_End,
        NotAvailable
    }

    public BahamutCommandLoadLastBlock() {
        this.Action = LoadLastBlock;
    }

    private OperationMode getLoadLastBlockMode(TelnetListPage aListPage) {
        OperationMode operationMode = OperationMode.End;
        switch (aListPage.getListType()) {
            case 1:
                return OperationMode.Left_S_End;
            case 2:
                if (aListPage.getSelectedIndex() != aListPage.getItemSize()) {
                    return OperationMode.End;
                }
                if (aListPage.getSelectedIndex() > 1) {
                    return OperationMode.Home_End;
                }
                return OperationMode.NotAvailable;
            default:
                if (aListPage == null || aListPage.getSelectedIndex() != aListPage.getItemSize()) {
                    return OperationMode.End;
                }
                if (aListPage.getItemSize() == 1) {
                    return OperationMode.Left_Right_End;
                }
                return OperationMode.Home_End;
        }
    }

    public void execute(TelnetListPage aListPage) {
        if (aListPage == null) {
            setDone(true);
            return;
        }
        switch (getLoadLastBlockMode(aListPage)) {
            case Left_Right_End:
                TelnetOutputBuilder.create()
                        .pushKey(TelnetKeyboard.LEFT_ARROW)
                        .pushKey(TelnetKeyboard.RIGHT_ARROW)
                        .pushKey(TelnetKeyboard.END).sendToServer();
                return;
            case Home_End:
                TelnetOutputBuilder.create()
                        .pushKey(TelnetKeyboard.HOME)
                        .pushKey(TelnetKeyboard.END).sendToServer();
                return;
            case Left_S_End:
                TelnetOutputBuilder.create()
                        .pushKey(TelnetKeyboard.LEFT_ARROW)
                        .pushKey(TelnetKeyboard.BACK_ONE_CHAR)
                        .pushKey(TelnetKeyboard.END).sendToServer();
                return;
            case End:
                TelnetOutputBuilder.create()
                        .pushKey(TelnetKeyboard.END).sendToServer();
                return;
            default:
                setDone(true);
        }
    }

    public void executeFinished(TelnetListPage aListPage, TelnetListPageBlock aPageData) {
        if (aListPage.getItemSize() > aPageData.maximumItemNumber) {
            aListPage.setItemSize(0);
            aListPage.cleanAllItem();
        }
        setDone(true);
    }

    public String toString() {
        return "[LoadLastBlock]";
    }
}
