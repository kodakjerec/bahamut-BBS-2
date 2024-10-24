package com.kota.Bahamut.Command;

import static com.kota.Bahamut.Command.BahamutCommandPostArticle.convertContentToStringList;
import static com.kota.Bahamut.Service.CommonFunctions.judgeDoubleWord;
import static com.kota.Telnet.Model.TelnetFrame.DEFAULT_COLUMN;

import androidx.annotation.NonNull;

import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Telnet.Reference.TelnetKeyboard;
import com.kota.Telnet.TelnetClient;
import com.kota.Telnet.TelnetCommand;
import com.kota.Telnet.TelnetOutputBuilder;

import java.util.List;

public class BahamutCommandEditArticle extends com.kota.Bahamut.Command.TelnetCommand {
    String _article_number;
    String _content;
    String _title;

    public BahamutCommandEditArticle(String aArticleNumber, String title, String content) {
        _article_number = aArticleNumber;
        // 標題: 第80個字元如果是雙字元則截斷
        _title = judgeDoubleWord(title, DEFAULT_COLUMN-9).split("\n")[0];
        // 內文: 一行超過80個字元預先截斷, 第80個字元如果是雙字元則先截斷, 這個雙字元歸類到下一行
        _content = judgeDoubleWord(content, DEFAULT_COLUMN-2);
        Action = PostArticle;
    }

    public void execute(TelnetListPage aListPage) {
        if (_article_number != null && _content != null && _content.length() > 0) {
            // 將內文依照 *[ 切換成多段
            List<String> outputs = convertContentToStringList(_content);

            TelnetOutputBuilder builder = TelnetOutputBuilder.create()
                    .pushString(_article_number + "\nE")
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
            if (_title != null) {
                builder.pushString("T").pushData((byte) 25).pushString(_title + "\nY\n");
            }
            TelnetClient.getClient().sendDataToServer(builder.build());
        }
    }

    public void executeFinished(TelnetListPage aListPage, TelnetListPageBlock aPageData) {
        setDone(true);
    }

    @NonNull
    public String toString() {
        return "[EditArticle][articleIndex=" + _article_number + " title=" + _title + " content=" + _content + "]";
    }
}
