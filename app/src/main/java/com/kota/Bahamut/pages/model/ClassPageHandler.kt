package com.kota.Bahamut.pages.model

import com.kota.telnet.model.TelnetModel.getRow
import com.kota.telnet.model.TelnetModel.getRowString
import com.kota.telnet.model.TelnetRow
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetClient.model
import com.kota.telnet.TelnetUtils

class ClassPageHandler private constructor() {
    fun load(): ClassPageBlock {
        val class_package: ClassPageBlock = ClassPageBlock.Companion.create()
        if (TelnetClient.model.getRowString(2).trim { it <= ' ' }.startsWith("編號")) {
            class_package.mode = 0
        } else {
            class_package.mode = 1
        }
        val end_index = 3 + 20
        var i = 3
        while (i < end_index && TelnetClient.model.getRowString(i).length != 0) {
            val row: TelnetRow? = TelnetClient.model.getRow(i)
            val board_index = TelnetUtils.getIntegerFromData(row!!, 1, 5)
            if (i == 3) {
                class_package.minimumItemNumber = board_index
            }
            val board_selected = row.getSpaceString(0, 0).trim { it <= ' ' }
            if (board_selected.length > 0 && board_selected.get(0) == '>') {
                class_package.selectedItemNumber = board_index
            }
            var board_name_end = 9
            var j = 0
            while (j < 12 && row.data[j + 9].toInt() != 32) {
                board_name_end = j + 9
                j++
            }
            var board_manager_start = 65
            // 看板英文名稱
            var board_name = row.getSpaceString(9, board_name_end).trim { it <= ' ' }
            // 看板中文名稱
            // 先取得除英文名稱外全字串, 扣除版主群後, 剩下的就是看板中文名稱
            var board_title =
                row.getSpaceString(board_name_end + 1, row.data.size - 1).trim { it <= ' ' }
            for (bb in board_title.length - 1 downTo 0) {
                if (board_title.get(bb).code == 32) {
                    board_manager_start = bb
                    break
                }
            }
            // 版主群
            val board_manager = board_title.substring(board_manager_start + 1, board_title.length)

            // 得到看板中文名稱
            board_title = board_title.replace(board_manager, "")
            val item: ClassPageItem = ClassPageItem.Companion.create()
            if (board_name.endsWith("/")) {
                item.isDirectory = true
                board_name = board_name.substring(0, board_name.length - 1)
            } else {
                item.isDirectory = false
                if (board_title.length > 2) {
                    board_title = board_title.substring(2)
                }
            }
            item.Name = board_name
            item.Title = board_title
            item.Manager = board_manager
            class_package.maximumItemNumber = board_index
            item.Number = board_index
            class_package.setItem(i - 3, item)
            i++
        }
        return class_package
    }

    companion object {
        private var _instance: ClassPageHandler? = null

        @JvmStatic
        val instance: ClassPageHandler
            get() {
                if (_instance == null) {
                    _instance = ClassPageHandler()
                }
                return _instance!!
            }
    }
}
