package com.kota.Telnet.Logic;

import com.kota.Telnet.TelnetClient;
import java.util.Vector;

public class SearchBoard_Handler {
    private static SearchBoard_Handler _instance = null;
    private Vector<String> _boards = new Vector<>();

    private SearchBoard_Handler() {
    }

    public static SearchBoard_Handler getInstance() {
        if (_instance == null) {
            _instance = new SearchBoard_Handler();
        }
        return _instance;
    }

    public void read() {
        int i = 3;
        while (i < 23) {
            String content = TelnetClient.getModel().getRowString(i).trim();
            if (content.length() != 0) {
                for (String board : content.split(" +")) {
                    if (board.length() > 0) {
                        this._boards.add(board);
                    }
                }
                i++;
            } else {
                return;
            }
        }
    }

    public void clear() {
        this._boards.clear();
    }

    public int getBoardsSize() {
        return this._boards.size();
    }

    public String getBoard(int index) {
        return this._boards.get(index);
    }

    public String[] getBoards() {
        return (String[]) this._boards.toArray(new String[this._boards.size()]);
    }
}
