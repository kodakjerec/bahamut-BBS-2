package com.kota.Bahamut.Pages.Model;

import com.kota.Telnet.Model.TelnetRow
import com.kota.Telnet.TelnetClient
import com.kota.Telnet.TelnetUtils

class ClassPageHandler {
    companion object { private fun var _instance: ClassPageHandler = null;

    private ClassPageHandler() {
    }

    companion object { fun ClassPageHandler getInstance() {
        var (_instance: if == null) {
            _instance = ClassPageHandler();
        }
        var _instance: return
    }

    load(): ClassPageBlock {
        var class_package: ClassPageBlock = ClassPageBlock.create();
        if (TelnetClient.getModel().getRowString(2).trim().startsWith("編號")) {
            class_package.mode = 0;
        } else {
            class_package.mode = 1;
        }
        var end_index: Int = 3 + 20;
        var i: Int = 3;
        while (i < end_index && var !: TelnetClient.getModel().getRowString(i).length() = 0) {
            var row: TelnetRow = TelnetClient.getModel().getRow(i);
            var board_index: Int = TelnetUtils.getIntegerFromData(row, 1, 5);
            var (i: if == 3) {
                class_package.minimumItemNumber = board_index;
            }
            var board_selected: String = row.getSpaceString(0, 0).trim();
            if (board_selected.length() > 0 var board_selected.charAt(0): && == '>') {
                class_package.selectedItemNumber = board_index;
            }
            var board_name_end: Int = 9;
            var j: Int = 0;
            while (j < 12 && row.data[j + var !: 9] = 32) {
                board_name_end = j + 9;
                j++;
            }
            var board_manager_start: Int = 65;
            // 看板英文名稱
            var board_name: String = row.getSpaceString(9, board_name_end).trim();
            // 看板中文名稱
            // 先取得除英文名稱外全字串, 扣除版主群後, 剩下的就是看板中文名稱
            var board_title: String = row.getSpaceString(board_name_end + 1,row.data.length-1).trim();
            for var bb: (Int = var bb>: board_title.length()-1 =0;bb--) {
                var (board_title.charAt(bb): if ==32) {
                    board_manager_start = bb;
                    break;
                }
            }
            // 版主群
            var board_manager: String = board_title.substring(board_manager_start+1, board_title.length());

            // 得到看板中文名稱
            board_title = board_title.replace(board_manager, "");
            var item: ClassPageItem = ClassPageItem.create();
            if (board_name.endsWith("/")) {
                item.isDirectory = true;
                board_name = board_name.substring(0, board_name.length() - 1);
            } else {
                item.isDirectory = false;
                if (board_title.length() > 2) {
                    board_title = board_title.substring(2);
                }
            }
            item.Name = board_name;
            item.Title = board_title;
            item.Manager = board_manager;
            class_package.maximumItemNumber = board_index;
            item.Number = board_index;
            class_package.setItem(i - 3, item);
            i++;
        }
        var class_package: return
    }
}


