package com.kota.Bahamut.Command;

import androidx.annotation.NonNull;

import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Telnet.Reference.TelnetKeyboard;
import com.kota.Telnet.TelnetOutputBuilder;

import java.util.ArrayList;
import java.util.List;

public class BahamutCommandPostArticle extends TelnetCommand {
    String _article_number;
    String _content;
    TelnetListPage _list_page;
    String _sign;
    String _target;
    String _title;

    Boolean _isRecoverPost;

    public BahamutCommandPostArticle(TelnetListPage aListPage, String title, String content, String aTarget, String aArticleNumber, String aSign, Boolean isRecoverPost) {
        _title = title;
        _content = content;
        Action = PostArticle;
        _target = aTarget;
        _article_number = aArticleNumber;
        _list_page = aListPage;
        _sign = aSign;
        if (_sign == null) {
            _sign = "";
        }
        _isRecoverPost = isRecoverPost;
    }

    @Override // com.kota.Bahamut.Command.TelnetCommand
    public void execute(TelnetListPage aListPage) {
        TelnetOutputBuilder buffer = TelnetOutputBuilder.create();
        // Reply
        if (_article_number != null && _target != null) {
            if (_isRecoverPost) {
                // 引言過長
                if (_title != null) {
                    buffer.pushKey(TelnetKeyboard.CTRL_T); // ctrl + T
                    buffer.pushString(_title + "\n");
                }

                // 砍掉所有內文重新貼上
                int pageUpCounts = (int)Math.ceil((double)_content.split("\n").length/22);
                for (int i = 0;i< pageUpCounts; i++) {
                    buffer.pushKey(TelnetKeyboard.PAGE_UP); // ctrl+B, pageUp
                }
                buffer.sendToServer();

                buffer.pushKey(TelnetKeyboard.CTRL_G); // ^G 刪除目前這行之後至檔案結尾
                buffer.pushKey(TelnetKeyboard.CTRL_Y); // ^Y 刪除目前這行
                buffer.sendToServer();
                // 內文全部刪除

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


                buffer.pushKey(TelnetKeyboard.CTRL_X); // ctrl+x 存檔
                buffer.pushString("s\n"); // S-存檔
                if (_target.equals("M")) {
                    buffer.pushString("Y\n\n"); // 是否自存底稿(Y/N)？[N]
                }
                buffer.sendToServer();
            } else {
                buffer.pushString(_article_number + "\n"); // 指定文章編號, 按下Enter
                buffer.pushString("y"); // y-回應
                buffer.pushString(_target + "\n"); // 回應至 (F)看板 (M)作者信箱 (B)二者皆是 (Q)取消？[F]
                if (_target != null && (_target.equals("F") || _target.equals("B"))) {
                    buffer.pushString("\n");
                }
                if (_title != null) {
                    buffer.pushKey(TelnetKeyboard.CTRL_Y); // ctrl + Y
                    buffer.pushString(_title + "\n");
                    buffer.pushString("N\n"); // 請問要引用原文嗎(Y/N/All/Repost/1-9)？[Y]
                    buffer.pushString(_sign + "\n"); // 選擇簽名檔 (1 ~ 9, 0=不加)[0]：
                } else {
                    buffer.pushString("\n");
                    buffer.pushString("N\n"); // 請問要引用原文嗎(Y/N/All/Repost/1-9)？[Y]
                    buffer.pushString(_sign + "\n"); // 選擇簽名檔 (1 ~ 9, 0=不加)[0]：
                }

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

                buffer.pushKey(TelnetKeyboard.CTRL_X); // ctrl+x 存檔
                buffer.pushString("s\n"); // S-存檔
                if (_target.equals("M")) {
                    buffer.pushString("Y\n\n"); // 是否自存底稿(Y/N)？[N]
                }
                buffer.sendToServer();
            }
        } else {
            // New
            buffer.pushKey(TelnetKeyboard.CTRL_P); // Ctrl+P
            buffer.pushString("\n");
            buffer.pushString(_title);
            buffer.pushString("\n" + _sign + "\n");

            // 貼文
            // 將內文依照 *[ 切換成多段
            List<String> outputs = convertContentToStringList(_content);
            // 貼入內文
            for(String output: outputs) {
                if (output.equals("*")) // 跳脫字元
                    buffer.pushKey(21);
                else
                    buffer.pushString(output);
            }

            buffer.pushKey(TelnetKeyboard.CTRL_X); // ctrl+x 存檔
            buffer.pushString("s\n");
            buffer.sendToServer();
        }
    }

    @Override // com.kota.Bahamut.Command.TelnetCommand
    public void executeFinished(TelnetListPage aListPage, TelnetListPageBlock aPageData) {
        aListPage.pushPreloadCommand(0);
        setDone(true);
    }

    public static List<String> convertContentToStringList(String _content) {
        // 將內文依照 *[ 切換成多段
        List<String> outputs = new ArrayList<>();
        int endIndex;
        while(true) {
            endIndex = _content.indexOf("*[");
            if (endIndex>-1) {
                outputs.add(_content.substring(0, endIndex));
                outputs.add("*"); // 標記起來之後用來替換字元
                _content = _content.substring(endIndex+1);
            } else {
                outputs.add(_content);
                break;
            }
        }

        return outputs;
    }

    @NonNull
    public String toString() {
        return "[PostArticle][title=" + _title + " content" + _content + "]";
    }
}
