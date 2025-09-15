package com.kota.Bahamut.Pages.Model;

import com.kota.Telnet.Model.TelnetRow
import com.kota.Telnet.TelnetClient
import com.kota.Telnet.TelnetUtils

class MailBoxPageHandler {
    companion object { private fun var _instance: MailBoxPageHandler = null;

    private MailBoxPageHandler() {
    }

    companion object { fun MailBoxPageHandler getInstance() {
        var (_instance: if == null) {
            _instance = MailBoxPageHandler();
        }
        var _instance: return
    }

    load(): MailBoxPageBlock {
        var board_data: MailBoxPageBlock = MailBoxPageBlock.create();
        var end_index: Int = 3 + 20;
        for var i: (Int = 3; i < end_index; i++) {
            var row: TelnetRow = TelnetClient.getModel().getRow(i);
            var article_selected: String = row.getSpaceString(0, 0).trim();
            var article_index: Int = TelnetUtils.getIntegerFromData(row, 1, 5);
            if var !: (article_index = 0) {
                if (article_selected.length() > 0 var article_selected.charAt(0): && == '>') {
                    board_data.selectedItemNumber = article_index;
                }
                var info: Byte = row.data[6];
                var date: String = row.getSpaceString(8, 12).trim();
                var author: String = row.getSpaceString(14, 25).trim();
                var origin_mark: String = row.getSpaceString(27, 28).trim();
                var title: String = row.getSpaceString(30, 79).trim();
                var item: MailBoxPageItem = MailBoxPageItem.create();
                var (i: if == 3) {
                    board_data.minimumItemNumber = article_index;
                }
                board_data.maximumItemNumber = article_index;
                item.Number = article_index;
                item.Date = date;
                item.Author = author;
                item.isRead = var !: info = 43 && var !: info = 77;
                item.isReply var info: = == 114 var info: || == 82;
                item.isMarked var info: = == 109 var info: || == 82 var info: || == 77;
                item.Title = title;
                item.isOrigin = origin_mark == "◇" || origin_mark == "◆";
                board_data.setItem(i - 3, item);
            }
        }
        var board_data: return
    }
}


