package com.kota.Bahamut.pages.model

import com.kota.Bahamut.pages.boardPage.BoardPageAction
import com.kota.telnet.model.TelnetModel
import com.kota.telnet.model.TelnetRow
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetUtils

class BoardPageHandler private constructor() {
    fun load(): BoardPageBlock {
        var row: TelnetRow? = null
        val boardPackage: BoardPageBlock = BoardPageBlock.Companion.create()
        val firstRowString: String? = TelnetClient.client?.model?.getRowString(0)
        val rowChar = firstRowString?.toCharArray()
        var boardManagerStart = 0
        var boardManagerEnd = 0
        var i = 0
        while (true) {
            if (i >= rowChar.size) {
                break
            } else if (rowChar[i].code == 12304) {
                boardManagerStart = i + 1
                break
            } else {
                i++
            }
        }
        var i2 = boardManagerStart
        while (true) {
            if (i2 >= rowChar.size) {
                break
            } else if (rowChar[i2].code == 12305) {
                boardManagerEnd = i2
                break
            } else {
                i2++
            }
        }
        if (boardManagerEnd > boardManagerStart) {
            var boardManager = firstRowString.substring(boardManagerStart, boardManagerEnd)
                .trim { it <= ' ' }
            if (boardManager.length > 3 && boardManager.startsWith("板主：")) {
                boardManager = boardManager.substring(3)
            }
            boardPackage.boardManager = boardManager
        }
        var boardNameStart = rowChar.size - 1
        var boardNameEnd = rowChar.size - 1
        var i3 = rowChar.size - 1
        while (true) {
            if (i3 < 0) {
                break
            } else if (rowChar[i3].code == 12299) {
                boardNameEnd = i3
                break
            } else {
                i3--
            }
        }
        var i4 = boardNameEnd
        while (true) {
            if (i4 < 0) {
                break
            } else if (rowChar[i4].code == 12298) {
                boardNameStart = i4 + 1
                break
            } else {
                i4--
            }
        }
        if (boardNameEnd > boardNameStart) {
            boardPackage.boardName =
                firstRowString.substring(boardNameStart, boardNameEnd).trim { it <= ' ' }
        }
        val boardTitleStart = boardManagerEnd + 1
        var boardTitleEnd = rowChar.size - 1
        var i5 = boardNameStart
        while (true) {
            if (i5 <= boardTitleStart) {
                break
            } else if (rowChar[i5].code == 30475) {
                boardTitleEnd = i5
                break
            } else {
                i5--
            }
        }
        if (boardTitleEnd > boardTitleStart) {
            boardPackage.boardTitle =
                firstRowString.substring(boardTitleStart, boardTitleEnd).trim { it <= ' ' }
        }
        if (boardPackage.boardManager == null || boardPackage.boardManager != "主題串列") {
            boardPackage.boardType = BoardPageAction.LIST
        } else {
            boardPackage.boardType = BoardPageAction.SEARCH
        }
        val endIndex = 3 + 20
        var i6 = 3

        val rowModel: TelnetModel? = TelnetClient.client?.model
        var isMoreThen10w = ""
        for (getRow in rowModel?.rows) {
            val checkChar = getRow.getSpaceString(0, 0).trim { it <= ' ' }
            if (checkChar.isNotEmpty() && checkChar[0] >= '1' && checkChar[0] <= '9') {
                isMoreThen10w = checkChar
            }
        }



        while (i6 < endIndex && (rowModel.getRow(i6).also { row = it }) != null && row.toString()
                .trim { it <= ' ' }.isNotEmpty()
        ) {
            row?.reloadSpace()
            val articleSelected = row.getSpaceString(0, 0).trim { it <= ' ' }
            var articleNumberStr = row.getSpaceString(1, 5).trim { it <= ' ' }
            // 應對十萬篇
            if (isMoreThen10w != "") articleNumberStr = isMoreThen10w + articleNumberStr
            val articleNumber = articleNumberStr.toInt()
            var isSelected = false
            if (articleSelected.isNotEmpty() && articleSelected == ">") {
                boardPackage.selectedItemNumber = articleNumber
                isSelected = true
            }

            if (articleNumber != 0) {
                val info = row.data[7]
                val gy = TelnetUtils.getIntegerFromData(row, 8, 9)
                val date = row.getSpaceString(10, 14).trim { it <= ' ' }
                val author = row.getSpaceString(16, 27).trim { it <= ' ' }
                val originMark = row.getSpaceString(29, 30).trim { it <= ' ' }
                val title = row.getSpaceString(31, 79).trim { it <= ' ' }
                val item: BoardPageItem = BoardPageItem.Companion.create()
                if (i6 == 3) {
                    boardPackage.minimumItemNumber = articleNumber
                }
                boardPackage.maximumItemNumber = articleNumber
                item.itemNumber = articleNumber
                item.date = date
                item.author = author
                item.isRead = info.toInt() != 43 && info.toInt() != 77
                item.isDeleted = info.toInt() == 100
                item.isMarked = info.toInt() == 109 || info.toInt() == 77
                item.gy = gy
                item.title = title
                item.isReply = originMark != "◇" && originMark != "◆"
                boardPackage.setItem(i6 - 3, item)
                if (isSelected) {
                    boardPackage.selectedItem = item
                }
            }
            i6++
        }
        return boardPackage
    }

    companion object {
        private var boardPageHandler: BoardPageHandler? = null

        val instance: BoardPageHandler
            get() {
                if (boardPageHandler == null) {
                    boardPageHandler = BoardPageHandler()
                }
                return boardPageHandler!!
            }
    }
}
