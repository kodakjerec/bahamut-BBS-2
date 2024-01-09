package com.kota.Bahamut.Pages.Model;

import com.kota.Telnet.Model.TelnetRow;
import com.kota.Telnet.TelnetClient;
import com.kota.Telnet.TelnetUtils;

public class MailBoxPageHandler {
    private static MailBoxPageHandler _instance = null;

    private MailBoxPageHandler() {
    }

    public static MailBoxPageHandler getInstance() {
        if (_instance == null) {
            _instance = new MailBoxPageHandler();
        }
        return _instance;
    }

    public MailBoxPageBlock load() {
        MailBoxPageBlock board_data = MailBoxPageBlock.create();
        int end_index = 3 + 20;
        for (int i = 3; i < end_index; i++) {
            TelnetRow row = TelnetClient.getModel().getRow(i);
            String article_selected = row.getSpaceString(0, 0).trim();
            int article_index = TelnetUtils.getIntegerFromData(row, 1, 5);
            if (article_index != 0) {
                if (article_selected.length() > 0 && article_selected.charAt(0) == '>') {
                    board_data.selectedItemNumber = article_index;
                }
                byte info = row.data[6];
                String date = row.getSpaceString(8, 12).trim();
                String author = row.getSpaceString(14, 25).trim();
                String origin_mark = row.getSpaceString(27, 28).trim();
                String title = row.getSpaceString(30, 79).trim();
                MailBoxPageItem item = MailBoxPageItem.create();
                if (i == 3) {
                    board_data.minimumItemNumber = article_index;
                }
                board_data.maximumItemNumber = article_index;
                item.Number = article_index;
                item.Date = date;
                item.Author = author;
                item.isRead = (info == 43 || info == 77) ? false : true;
                item.isReply = info == 114 || info == 82;
                item.isMarked = info == 109 || info == 82 || info == 77;
                item.Title = title;
                item.isOrigin = origin_mark.equals("◇") || origin_mark.equals("◆");
                board_data.setItem(i - 3, item);
            }
        }
        return board_data;
    }
}
