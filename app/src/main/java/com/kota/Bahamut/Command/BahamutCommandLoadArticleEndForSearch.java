package com.kota.Bahamut.Command;

import com.kota.Telnet.Reference.TelnetKeyboard;
import com.kota.Telnet.TelnetClient;

public class BahamutCommandLoadArticleEndForSearch {
    public void execute() {
        System.out.print(toString() + " ");
        TelnetClient.getClient().sendKeyboardInputToServer(TelnetKeyboard.END);
    }

    public String toString() {
        return "[LoadArticleEndForSearch]";
    }
}
