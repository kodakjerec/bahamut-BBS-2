package com.kota.Bahamut.Command;

import static com.kota.Bahamut.Command.BahamutCommandPostArticle.convertContentToStringList;

import androidx.annotation.NonNull;

import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Telnet.Reference.TelnetKeyboard;
import com.kota.Telnet.TelnetClient;
import com.kota.Telnet.TelnetCommand;
import com.kota.Telnet.TelnetOutputBuilder;

import java.util.ArrayList;
import java.util.List;

public class BahamutCommandEditArticle extends com.kota.Bahamut.Command.TelnetCommand {
    String _article_number;
    String _content;
    String _title;

    public BahamutCommandEditArticle(String aArticleNumber, String title, String content) {
        this._article_number = aArticleNumber;
        this._title = title;
        this._content = content;
        this.Action = PostArticle;
    }

    public void execute(TelnetListPage aListPage) {
        if (this._article_number != null && this._content != null && this._content.length() > 0) {
            // 將內文依照 *[ 切換成多段
            List<String> outputs = convertContentToStringList(_content);

            TelnetOutputBuilder builder = TelnetOutputBuilder.create()
                    .pushString(this._article_number + "\nE")
                    .pushData((byte)TelnetKeyboard.CTRL_G) // ^G 刪除目前這行之後至檔案結尾
                    .pushData((byte)TelnetKeyboard.CTRL_Y); // ^Y 刪除目前這行
            // 貼入內文
            for(String output: outputs) {
                if (output.equals("*")) // 跳脫字元
                    builder.pushData((byte)21);
                else
                    builder.pushString(output);
            }
            // 結束
            builder.pushData(TelnetCommand.TERMINAL_TYPE)
                    .pushString("S\n");
            if (this._title != null) {
                builder.pushString("T").pushData((byte) 25).pushString(this._title + "\nY\n");
            }
            TelnetClient.getClient().sendDataToServer(builder.build());
        }
    }

    public void executeFinished(TelnetListPage aListPage, TelnetListPageBlock aPageData) {
        setDone(true);
    }

    @NonNull
    public String toString() {
        return "[EditArticle][articleIndex=" + this._article_number + " title=" + this._title + " content=" + this._content + "]";
    }
}
