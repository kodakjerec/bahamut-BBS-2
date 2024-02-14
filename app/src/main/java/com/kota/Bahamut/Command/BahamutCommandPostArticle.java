package com.kota.Bahamut.Command;

import com.kota.Bahamut.ListPage.TelnetListPage;
import com.kota.Bahamut.ListPage.TelnetListPageBlock;
import com.kota.Telnet.Reference.TelnetKeyboard;
import com.kota.Telnet.TelnetOutputBuilder;

/* loaded from: classes.dex */
public class BahamutCommandPostArticle extends TelnetCommand {
    String _article_number;
    String _content;
    TelnetListPage _list_page;
    String _sign;
    String _target;
    String _title;

    Boolean isRecoverPost = false;

    public BahamutCommandPostArticle(TelnetListPage aListPage, String title, String content, String aTarget, String aArticleNumber, String aSign, Boolean isRecoverPost) {
        this._title = "";
        this._content = "";
        this._target = "F";
        this._article_number = null;
        this._sign = "";
        this._title = title;
        this._content = content;
        this.Action = 8;
        this._target = aTarget;
        this._article_number = aArticleNumber;
        this._list_page = aListPage;
        this._sign = aSign;
        if (this._sign == null) {
            this._sign = "";
        }
        this.isRecoverPost = isRecoverPost;
    }

    @Override // com.kota.Bahamut.Command.TelnetCommand
    public void execute(TelnetListPage aListPage) {
        TelnetOutputBuilder buffer = TelnetOutputBuilder.create();
        // Reply
        if (this._article_number != null && this._target != null) {
            if (isRecoverPost) {
                // 引言過長
                if (this._title != null) {
                    buffer.pushKey(TelnetKeyboard.CTRL_T); // ctrl + T
                    buffer.pushString(this._title + "\n");
                }

                // 砍掉所有內文重新貼上
                Integer pageUpCounts = (int)Math.ceil((double)this._content.split("\n").length/22);
                for (int i = 0;i< pageUpCounts; i++) {
                    buffer.pushKey(TelnetKeyboard.PAGE_UP); // ctrl+B, pageUp
                }
                buffer.sendToServer();

                buffer.pushKey(TelnetKeyboard.CTRL_G); // ctrl+G, 此行以下
                buffer.pushKey(TelnetKeyboard.CTRL_Y); // ctrl+Y, 此行
                buffer.sendToServer();
                // 內文全部刪除

                // 重新貼文
                buffer.pushString(this._content);
                buffer.pushKey(TelnetKeyboard.CTRL_X); // ctrl+x 存檔
                buffer.pushString("s\n"); // S-存檔
                if (this._target.equals("M")) {
                    buffer.pushString("Y\n\n"); // 是否自存底稿(Y/N)？[N]
                }
                buffer.sendToServer();
            } else {
                buffer.pushString(this._article_number + "\n"); // 指定文章編號, 按下Enter
                buffer.pushString("y"); // y-回應
                buffer.pushString(this._target + "\n"); // 回應至 (F)看板 (M)作者信箱 (B)二者皆是 (Q)取消？[F]
                if (this._target != null && (this._target.equals("F") || this._target.equals("B"))) {
                    buffer.pushString("\n");
                }
                if (this._title != null) {
                    buffer.pushKey(TelnetKeyboard.CTRL_Y); // ctrl + Y
                    buffer.pushString(this._title + "\n");
                    buffer.pushString("N\n"); // 請問要引用原文嗎(Y/N/All/Repost/1-9)？[Y]
                    buffer.pushString(this._sign + "\n"); // 選擇簽名檔 (1 ~ 9, 0=不加)[0]：
                } else {
                    buffer.pushString("\n");
                    buffer.pushString("N\n"); // 請問要引用原文嗎(Y/N/All/Repost/1-9)？[Y]
                    buffer.pushString(this._sign + "\n"); // 選擇簽名檔 (1 ~ 9, 0=不加)[0]：
                }
                buffer.pushString(this._content);
                buffer.pushKey(TelnetKeyboard.CTRL_X); // ctrl+x 存檔
                buffer.pushString("s\n"); // S-存檔
                if (this._target.equals("M")) {
                    buffer.pushString("Y\n\n"); // 是否自存底稿(Y/N)？[N]
                }
                buffer.sendToServer();
            }
        } else {
            // New
            buffer.pushKey(16); // Ctrl
            buffer.pushData((byte) 13); // P
            buffer.pushString(this._title);
            buffer.pushString("\n" + this._sign + "\n");
            buffer.pushString(this._content);
            buffer.pushKey(24); // ctrl+x 存檔
            buffer.pushString("s\n");
            buffer.sendToServer();
        }
    }

    @Override // com.kota.Bahamut.Command.TelnetCommand
    public void executeFinished(TelnetListPage aListPage, TelnetListPageBlock aPageData) {
        aListPage.pushPreloadCommand(0);
        setDone(true);
    }

    public String toString() {
        return "[PostArticle][title=" + this._title + " content" + this._content + "]";
    }
}
