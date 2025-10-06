package com.kota.Bahamut.pages.model

import com.kota.telnet.model.TelnetRow
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetUtils

class MailBoxPageHandler private constructor() {
    fun load(): MailBoxPageBlock {
        val boardData = MailBoxPageBlock.create()
        val endIndex = 3 + 20
        for (i in 3..<endIndex) {
            val row: TelnetRow = TelnetClient.model.getRow(i)!!
            val articleSelected = row.getSpaceString(0, 0).trim { it <= ' ' }
            val articleIndex = TelnetUtils.getIntegerFromData(row, 1, 5)
            if (articleIndex != 0) {
                if (articleSelected.isNotEmpty() && articleSelected[0] == '>') {
                    boardData.selectedItemNumber = articleIndex
                }
                val info = row.data[6]
                val date = row.getSpaceString(8, 12).trim { it <= ' ' }
                val author = row.getSpaceString(14, 25).trim { it <= ' ' }
                val originMark = row.getSpaceString(27, 28).trim { it <= ' ' }
                val title = row.getSpaceString(30, 79).trim { it <= ' ' }
                val item: MailBoxPageItem = MailBoxPageItem.Companion.create()
                if (i == 3) {
                    boardData.minimumItemNumber = articleIndex
                }
                boardData.maximumItemNumber = articleIndex
                item.itemNumber = articleIndex
                item.date = date
                item.author = author
                item.isRead = info.toInt() != 43 && info.toInt() != 77
                item.isReply = info.toInt() == 114 || info.toInt() == 82
                item.isMarked = info.toInt() == 109 || info.toInt() == 82 || info.toInt() == 77
                item.title = title
                item.isOrigin = originMark == "◇" || originMark == "◆"
                boardData.setItem(i - 3, item)
            }
        }
        return boardData
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
