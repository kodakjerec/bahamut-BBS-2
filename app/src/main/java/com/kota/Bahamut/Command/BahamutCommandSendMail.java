package com.kota.Bahamut.Command;
import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Telnet.TelnetClient;
import com.kota.Telnet.TelnetOutputBuilder;
import com.kota.Telnet.TelnetCommand;

public class BahamutCommandSendMail extends com.kota.Bahamut.Command.TelnetCommand {
    String _content = "";
    String _receiver = "";
    String _title = "";

    public BahamutCommandSendMail(String receiver, String title, String content) {
        this._receiver = receiver;
        this._title = title;
        this._content = content;
        this.Action = 12;
    }

    public void execute(TelnetListPage aListPage) {
        if (this._receiver.length() > 0 && this._title.length() > 0 && this._content.length() > 0) {
            TelnetClient.getClient().sendDataToServer(TelnetOutputBuilder.create().pushKey(115).pushString(this._receiver + "\n").pushString(this._title + "\n").pushString("0\n").pushString(this._content).pushData((byte) TelnetCommand.TERMINAL_TYPE).pushString("s\nn\n\n").build());
        }
    }

    public void executeFinished(TelnetListPage aListPage, TelnetListPageBlock aPageData) {
        setDone(true);
    }

    public String toString() {
        return "[SendMail][title=" + this._title + " receiver=" + this._receiver + " content=" + this._content + "]";
    }
}
