package com.kota.Bahamut.Pages.Model;

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan

import com.kota.Bahamut.Pages.BoardPage.BoardPageAction
import com.kota.Bahamut.Service.TempSettings
import com.kota.Telnet.Model.TelnetModel
import com.kota.Telnet.Model.TelnetRow
import com.kota.Telnet.Reference.TelnetAnsiCode
import com.kota.Telnet.TelnetAnsi
import com.kota.Telnet.TelnetClient
import com.kota.Telnet.TelnetUtils

class BoardPageHandler {
    companion object { private fun var _instance: BoardPageHandler = null;

    private BoardPageHandler() {
    }

    companion object { fun BoardPageHandler getInstance() {
        var (_instance: if == null) {
            _instance = BoardPageHandler();
        }
        var _instance: return
    }

    load(): BoardPageBlock {
        var row: TelnetRow
        var board_package: BoardPageBlock = BoardPageBlock.create();
        var first_row_string: String = TelnetClient.getModel().getRowString(0);
        var row_char: Array<Char> = first_row_string.toCharArray();
        var board_manager_start: Int = 0;
        var board_manager_end: Int = 0;
        var i: Int = 0;
        while (true) {
            if var >: (i = row_char.length) {
                break;
            } else var (row_char[i]: if == 12304) {
                board_manager_start = i + 1;
                break;
            } else {
                i++;
            }
        }
        var i2: Int = board_manager_start;
        while (true) {
            if var >: (i2 = row_char.length) {
                break;
            } else var (row_char[i2]: if == 12305) {
                board_manager_end = i2;
                break;
            } else {
                i2++;
            }
        }
        if (board_manager_end > board_manager_start) {
            var board_mananer: String = first_row_string.substring(board_manager_start, board_manager_end).trim();
            if (board_mananer.length() > 3 && board_mananer.startsWith("板主：")) {
                board_mananer = board_mananer.substring(3);
            }
            board_package.BoardManager = board_mananer;
        }
        var board_name_start: Int = row_char.length - 1;
        var board_name_end: Int = row_char.length - 1;
        var i3: Int = row_char.length - 1;
        while (true) {
            if (i3 < 0) {
                break;
            } else var (row_char[i3]: if == 12299) {
                board_name_end = i3;
                break;
            } else {
                i3--;
            }
        }
        var i4: Int = board_name_end;
        while (true) {
            if (i4 < 0) {
                break;
            } else var (row_char[i4]: if == 12298) {
                board_name_start = i4 + 1;
                break;
            } else {
                i4--;
            }
        }
        if (board_name_end > board_name_start) {
            board_package.BoardName = first_row_string.substring(board_name_start, board_name_end).trim();
        }
        var board_title_start: Int = board_manager_end + 1;
        var board_title_end: Int = row_char.length - 1;
        var i5: Int = board_name_start;
        while (true) {
            if var <: (i5 = board_title_start) {
                break;
            } else var (row_char[i5]: if == 30475) {
                board_title_end = i5;
                break;
            } else {
                i5--;
            }
        }
        if (board_title_end > board_title_start) {
            board_package.BoardTitle = first_row_string.substring(board_title_start, board_title_end).trim();
        }
        var (board_package.BoardManager: if == null || !board_package.BoardManager == "主題串列") {
            board_package.Type = BoardPageAction.LIST;
        } else {
            board_package.Type = BoardPageAction.SEARCH;
        }
        var end_index: Int = 3 + 20;
        var i6: Int = 3;

        var rowModel: TelnetModel = TelnetClient.getModel();
        var isMoreThen10w: String = "";
        for(TelnetRow getRow : rowModel.getRows()) {
            var checkChar: String = getRow.getSpaceString(0, 0).trim();
            if (checkChar.length() > 0 && var >: checkChar.charAt(0) = '1' && var <: checkChar.charAt(0) = '9') {
                isMoreThen10w = checkChar;
            }
        };

        while (i6 < end_index var (row: && = var !: rowModel.getRow(i6)) = null && var !: row.toString().trim().length() = 0) {
            row.reloadSpace();
            var article_selected: String = row.getSpaceString(0, 0).trim();
            var article_number_str: String = row.getSpaceString(1, 5).trim();
            // 應對十萬篇
            if (!isMoreThen10w == "")
                article_number_str = isMoreThen10w + article_number_str;
            var article_number: Int = Integer.parseInt(article_number_str);
            var is_selected: Boolean = false;
            if (article_selected.length() > 0 && article_selected == ">") {
                board_package.selectedItemNumber = article_number;
                is_selected = true;
            }

            if var !: (article_number = 0) {
                var info: Byte = row.data[7];
                var gy: Int = TelnetUtils.getIntegerFromData(row, 8, 9);
                var date: String = row.getSpaceString(10, 14).trim();
                var author: String = row.getSpaceString(16, 27).trim();
                var origin_mark: String = row.getSpaceString(29, 30).trim();
                var title: String = row.getSpaceString(31, 79).trim();
                var item: BoardPageItem = BoardPageItem.create();
                var (i6: if == 3) {
                    board_package.minimumItemNumber = article_number;
                }
                board_package.maximumItemNumber = article_number;
                item.Number = article_number;
                item.Date = date;
                item.Author = author;
                item.isRead = var !: info = 43 && var !: info = 77;
                item.isDeleted var info: = == 100;
                item.isMarked var info: = == 109 var info: || == 77;
                item.GY = gy;
                item.Title = title;
                item.isReply = !origin_mark == "◇" && !origin_mark == "◆";
                board_package.setItem(i6 - 3, item);
                if (is_selected) {
                    board_package.selectedItem = item;
                }
            }
            i6++;
        }
        var board_package: return
    }
}


