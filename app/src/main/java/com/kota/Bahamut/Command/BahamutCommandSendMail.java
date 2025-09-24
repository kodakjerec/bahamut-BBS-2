package com.kota.Bahamut.Command;
import static com.kota.Bahamut.Command.BahamutCommandPostArticle.convertContentToStringList;

import androidx.annotation.NonNull;

import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Telnet.Reference.TelnetKeyboard;
import com.kota.Telnet.TelnetClient;
import com.kota.Telnet.TelnetOutputBuilder;
import com.kota.Telnet.TelnetCommand;

import java.util.List;

public class BahamutCommandSendMail extends com.kota.Bahamut.Command.TelnetCommand {
    String _content;
    String _receiver;
    String _title;

    public BahamutCommandSendMail(String receiver, String title, String content) {
        _receiver = receiver;
        _title = title;
        _content = content;
        Action = SendMail;
    }

    public void execute(TelnetListPage aListPage) {
        if (_receiver.length() > 0 && _title.length() > 0 && _content.length() > 0) {
            TelnetOutputBuilder buffer = TelnetOutputBuilder.create();
            buffer.pushKey(TelnetKeyboard.SHIFT_M);
            buffer.pushString(_receiver + "\n");
            buffer.pushString(_title + "\n");
            buffer.pushString("0" + "\n"); // 選擇簽名檔 (1 ~ 9, 0=不加)[0]：

            // 重新貼文
            // 將內文依照 *[ 切換成多段
            List<String> outputs = convertContentToStringList(_content);
            // 貼入內文
            for(String output: outputs) {
                if (output.equals("*")) // 跳脫字元
                    buffer.pushKey(TelnetKeyboard.CTRL_U);
                else
                    buffer.pushString(output);
            }

            buffer.pushData(TelnetCommand.TERMINAL_TYPE);
            buffer.pushString("s\n"); // S-存檔
            buffer.pushString("N\n\n"); // 是否自存底稿(Y/N)？[N]
            buffer.sendToServer();
        }
    }

    public void executeFinished(TelnetListPage aListPage, TelnetListPageBlock aPageData) {
        setDone(true);
    }

    @NonNull
    public String toString() {
        return "[SendMail][title=" + _title + " receiver=" + _receiver + " content=" + _content + "]";
    }
}
