package com.kota.Bahamut.Command;

import static com.kota.Bahamut.Service.CommonFunctions.getContextString;

import com.kota.ASFramework.Dialog.ASProcessingDialog;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Bahamut.R;
import com.kota.Telnet.Reference.TelnetKeyboard;
import com.kota.Telnet.TelnetClient;
import com.kota.Telnet.TelnetOutputBuilder;

public class BahamutCommandFSendMail extends TelnetCommand  {
    String _receiver;
    public BahamutCommandFSendMail(String receiver) {
        this._receiver = receiver;
        this.Action = SendMail;
    }

    @Override
    public void execute(TelnetListPage telnetListPage) {
        if (_receiver.length() > 0) {
            TelnetClient.getClient().sendDataToServer(
                    TelnetOutputBuilder.create()
                            .pushString("FA\n")
                            .pushKey(TelnetKeyboard.CTRL_Y)
                            .pushString(_receiver + "\n")
                            .build());
        }
        ASProcessingDialog.showProcessingDialog(getContextString(R.string.board_page_send_mail_ing));
    }

    @Override
    public void executeFinished(TelnetListPage telnetListPage, TelnetListPageBlock telnetListPageBlock) {
        setDone(true);
        ASProcessingDialog.dismissProcessingDialog();
        ASToast.showShortToast(getContextString(R.string.board_page_send_mail_finish));
    }

    public String toString() {
        return "[FSendMail]";
    }
}
