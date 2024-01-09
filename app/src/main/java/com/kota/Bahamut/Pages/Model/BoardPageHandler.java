package com.kota.Bahamut.Pages.Model;

import com.kota.Telnet.Model.TelnetRow;
import com.kota.Telnet.TelnetClient;
import com.kota.Telnet.TelnetUtils;

public class BoardPageHandler {
    private static BoardPageHandler _instance = null;

    private BoardPageHandler() {
    }

    public static BoardPageHandler getInstance() {
        if (_instance == null) {
            _instance = new BoardPageHandler();
        }
        return _instance;
    }

    public BoardPageBlock load() {
        TelnetRow row;
        BoardPageBlock board_package = BoardPageBlock.create();
        String first_row_string = TelnetClient.getModel().getRowString(0);
        char[] row_char = first_row_string.toCharArray();
        int board_manager_start = 0;
        int board_manager_end = 0;
        int i = 0;
        while (true) {
            if (i >= row_char.length) {
                break;
            } else if (row_char[i] == 12304) {
                board_manager_start = i + 1;
                break;
            } else {
                i++;
            }
        }
        int i2 = board_manager_start;
        while (true) {
            if (i2 >= row_char.length) {
                break;
            } else if (row_char[i2] == 12305) {
                board_manager_end = i2;
                break;
            } else {
                i2++;
            }
        }
        if (board_manager_end > board_manager_start) {
            String board_mananer = first_row_string.substring(board_manager_start, board_manager_end).trim();
            if (board_mananer.length() > 3 && board_mananer.startsWith("板主：")) {
                board_mananer = board_mananer.substring(3);
            }
            board_package.BoardManager = board_mananer;
        }
        int board_name_start = row_char.length - 1;
        int board_name_end = row_char.length - 1;
        int i3 = row_char.length - 1;
        while (true) {
            if (i3 < 0) {
                break;
            } else if (row_char[i3] == 12299) {
                board_name_end = i3;
                break;
            } else {
                i3--;
            }
        }
        int i4 = board_name_end;
        while (true) {
            if (i4 < 0) {
                break;
            } else if (row_char[i4] == 12298) {
                board_name_start = i4 + 1;
                break;
            } else {
                i4--;
            }
        }
        if (board_name_end > board_name_start) {
            board_package.BoardName = first_row_string.substring(board_name_start, board_name_end).trim();
        }
        int board_title_start = board_manager_end + 1;
        int board_title_end = row_char.length - 1;
        int i5 = board_name_start;
        while (true) {
            if (i5 <= board_title_start) {
                break;
            } else if (row_char[i5] == 30475) {
                board_title_end = i5;
                break;
            } else {
                i5--;
            }
        }
        if (board_title_end > board_title_start) {
            board_package.BoardTitle = first_row_string.substring(board_title_start, board_title_end).trim();
        }
        if (board_package.BoardManager == null || !board_package.BoardManager.equals("主題串列")) {
            board_package.Type = 0;
        } else {
            board_package.Type = 1;
        }
        int end_index = 3 + 20;
        int i6 = 3;
        while (i6 < end_index && (row = TelnetClient.getModel().getRow(i6)) != null && row.toString().trim().length() != 0) {
            String article_selected = row.getSpaceString(0, 0).trim();
            int article_number = TelnetUtils.getIntegerFromData(row, 1, 5);
            if (article_number != 0) {
                boolean is_selected = false;
                if (article_selected.length() > 0 && article_selected.charAt(0) == '>') {
                    board_package.selectedItemNumber = article_number;
                    is_selected = true;
                }
                byte info = row.data[7];
                int gy = TelnetUtils.getIntegerFromData(row, 8, 9);
                String date = row.getSpaceString(10, 14).trim();
                String author = row.getSpaceString(16, 27).trim();
                String origin_mark = row.getSpaceString(29, 30).trim();
                String title = row.getSpaceString(31, 79).trim();
                BoardPageItem item = BoardPageItem.create();
                if (i6 == 3) {
                    board_package.minimumItemNumber = article_number;
                }
                board_package.maximumItemNumber = article_number;
                item.Number = article_number;
                item.Date = date;
                item.Author = author;
                item.isRead = (info == 43 || info == 77) ? false : true;
                item.isDeleted = info == 100;
                item.isMarked = info == 109 || info == 77;
                item.GY = gy;
                item.Title = title;
                item.isReply = !origin_mark.equals("◇") && !origin_mark.equals("◆");
                board_package.setItem(i6 - 3, item);
                if (is_selected) {
                    board_package.selectedItem = item;
                }
            }
            i6++;
        }
        return board_package;
    }
}
