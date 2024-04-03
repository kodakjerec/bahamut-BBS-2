package com.kota.Bahamut.Command;

import com.kota.Telnet.Reference.TelnetKeyboard;
import com.kota.Telnet.TelnetClient;

public class BahamutCommandLoadMoreArticle {
    public void execute() {
        System.out.print(toString() + " ");
        TelnetClient.getClient().sendKeyboardInputToServerInBackground(TelnetKeyboard.PAGE_DOWN, 1);
    }

    public String toString() {
        return "[LoadMoreArticle]";
    }
}
