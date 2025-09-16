package com.kota.Bahamut.pages.model

import com.kota.telnet.model.TelnetModel.getRow
import com.kota.telnet.model.TelnetRow
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetClient.model
import com.kota.telnet.TelnetUtils

class MailBoxPageHandler private constructor() {
    fun load(): MailBoxPageBlock {
        val board_data = MailBoxPageBlock.create()
        val end_index = 3 + 20
        for (i in 3..<end_index) {
            val row: TelnetRow? = TelnetClient.model.getRow(i)
            val article_selected = row!!.getSpaceString(0, 0).trim { it <= ' ' }
            val article_index = TelnetUtils.getIntegerFromData(row, 1, 5)
            if (article_index != 0) {
                if (article_selected.length > 0 && article_selected.get(0) == '>') {
                    board_data.selectedItemNumber = article_index
                }
                val info = row.data[6]
                val date = row.getSpaceString(8, 12).trim { it <= ' ' }
                val author = row.getSpaceString(14, 25).trim { it <= ' ' }
                val origin_mark = row.getSpaceString(27, 28).trim { it <= ' ' }
                val title = row.getSpaceString(30, 79).trim { it <= ' ' }
                val item: MailBoxPageItem = MailBoxPageItem.Companion.create()
                if (i == 3) {
                    board_data.minimumItemNumber = article_index
                }
                board_data.maximumItemNumber = article_index
                item.Number = article_index
                item.Date = date
                item.Author = author
                item.isRead = info.toInt() != 43 && info.toInt() != 77
                item.isReply = info.toInt() == 114 || info.toInt() == 82
                item.isMarked = info.toInt() == 109 || info.toInt() == 82 || info.toInt() == 77
                item.Title = title
                item.isOrigin = origin_mark == "◇" || origin_mark == "◆"
                board_data.setItem(i - 3, item)
            }
        }
        return board_data
    }

    companion object {
        private var _instance: MailBoxPageHandler? = null

        @JvmStatic
        val instance: MailBoxPageHandler
            get() {
                if (_instance == null) {
                    _instance = MailBoxPageHandler()
                }
                return _instance!!
            }
    }
}
