package com.kota.Bahamut.pages.model

import com.kota.Bahamut.pages.boardPage.BoardPageAction
import com.kota.telnet.model.TelnetModel
import com.kota.telnet.model.TelnetModel.getRowString
import com.kota.telnet.model.TelnetRow
import com.kota.telnet.model.TelnetRow.getSpaceString
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetClient.model
import com.kota.telnet.TelnetUtils

class BoardPageHandler private constructor() {
    fun load(): BoardPageBlock {
        var row: TelnetRow?
        val board_package: BoardPageBlock = BoardPageBlock.Companion.create()
        val first_row_string: String = TelnetClient.model.getRowString(0)
        val row_char = first_row_string.toCharArray()
        var board_manager_start = 0
        var board_manager_end = 0
        var i = 0
        while (true) {
            if (i >= row_char.size) {
                break
            } else if (row_char[i].code == 12304) {
                board_manager_start = i + 1
                break
            } else {
                i++
            }
        }
        var i2 = board_manager_start
        while (true) {
            if (i2 >= row_char.size) {
                break
            } else if (row_char[i2].code == 12305) {
                board_manager_end = i2
                break
            } else {
                i2++
            }
        }
        if (board_manager_end > board_manager_start) {
            var board_mananer = first_row_string.substring(board_manager_start, board_manager_end)
                .trim { it <= ' ' }
            if (board_mananer.length > 3 && board_mananer.startsWith("板主：")) {
                board_mananer = board_mananer.substring(3)
            }
            board_package.BoardManager = board_mananer
        }
        var board_name_start = row_char.size - 1
        var board_name_end = row_char.size - 1
        var i3 = row_char.size - 1
        while (true) {
            if (i3 < 0) {
                break
            } else if (row_char[i3].code == 12299) {
                board_name_end = i3
                break
            } else {
                i3--
            }
        }
        var i4 = board_name_end
        while (true) {
            if (i4 < 0) {
                break
            } else if (row_char[i4].code == 12298) {
                board_name_start = i4 + 1
                break
            } else {
                i4--
            }
        }
        if (board_name_end > board_name_start) {
            board_package.BoardName =
                first_row_string.substring(board_name_start, board_name_end).trim { it <= ' ' }
        }
        val board_title_start = board_manager_end + 1
        var board_title_end = row_char.size - 1
        var i5 = board_name_start
        while (true) {
            if (i5 <= board_title_start) {
                break
            } else if (row_char[i5].code == 30475) {
                board_title_end = i5
                break
            } else {
                i5--
            }
        }
        if (board_title_end > board_title_start) {
            board_package.BoardTitle =
                first_row_string.substring(board_title_start, board_title_end).trim { it <= ' ' }
        }
        if (board_package.BoardManager == null || board_package.BoardManager != "主題串列") {
            board_package.Type = BoardPageAction.LIST
        } else {
            board_package.Type = BoardPageAction.SEARCH
        }
        val end_index = 3 + 20
        var i6 = 3

        val rowModel: TelnetModel? = TelnetClient.model
        var isMoreThen10w = ""
        for (getRow in rowModel!!.rows) {
            val checkChar = getRow.getSpaceString(0, 0).trim { it <= ' ' }
            if (checkChar.length > 0 && checkChar.get(0) >= '1' && checkChar.get(0) <= '9') {
                isMoreThen10w = checkChar
            }
        }



        while (i6 < end_index && (rowModel.getRow(i6).also { row = it }) != null && row.toString()
                .trim { it <= ' ' }.length != 0
        ) {
            row!!.reloadSpace()
            val article_selected = row.getSpaceString(0, 0).trim { it <= ' ' }
            var article_number_str = row.getSpaceString(1, 5).trim { it <= ' ' }
            // 應對十萬篇
            if (isMoreThen10w != "") article_number_str = isMoreThen10w + article_number_str
            val article_number = article_number_str.toInt()
            var is_selected = false
            if (article_selected.length > 0 && article_selected == ">") {
                board_package.selectedItemNumber = article_number
                is_selected = true
            }

            if (article_number != 0) {
                val info = row.data[7]
                val gy = TelnetUtils.getIntegerFromData(row, 8, 9)
                val date = row.getSpaceString(10, 14).trim { it <= ' ' }
                val author = row.getSpaceString(16, 27).trim { it <= ' ' }
                val origin_mark = row.getSpaceString(29, 30).trim { it <= ' ' }
                val title = row.getSpaceString(31, 79).trim { it <= ' ' }
                val item: BoardPageItem = BoardPageItem.Companion.create()
                if (i6 == 3) {
                    board_package.minimumItemNumber = article_number
                }
                board_package.maximumItemNumber = article_number
                item.Number = article_number
                item.Date = date
                item.Author = author
                item.isRead = info.toInt() != 43 && info.toInt() != 77
                item.isDeleted = info.toInt() == 100
                item.isMarked = info.toInt() == 109 || info.toInt() == 77
                item.GY = gy
                item.Title = title
                item.isReply = origin_mark != "◇" && origin_mark != "◆"
                board_package.setItem(i6 - 3, item)
                if (is_selected) {
                    board_package.selectedItem = item
                }
            }
            i6++
        }
        return board_package
    }

    companion object {
        private var _instance: BoardPageHandler? = null

        val instance: BoardPageHandler
            get() {
                if (_instance == null) {
                    _instance = BoardPageHandler()
                }
                return _instance!!
            }
    }
}
