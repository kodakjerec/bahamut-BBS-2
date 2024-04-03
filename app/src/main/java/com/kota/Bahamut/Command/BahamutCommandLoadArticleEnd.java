package com.kota.Bahamut.Command;

import com.kota.Telnet.Reference.TelnetKeyboard;
import com.kota.Telnet.TelnetClient;

public class BahamutCommandLoadArticleEnd {
    public void execute() {
        System.out.print(toString() + " ");
        TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.LEFT_ARROW);
    }

    public String toString() {
        return "[LoadArticleEnd]";
    }
}
