package com.kota.Bahamut.pages.essencePage

import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.Bahamut.pages.boardPage.BoardPageAction
import com.kota.Bahamut.pages.model.BoardEssencePageItem
import com.kota.Bahamut.pages.model.BoardPageBlock
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetUtils
import com.kota.telnet.model.TelnetRow

class BoardEssencePageHandler private constructor() {
    fun load(): TelnetListPageBlock {
        val boardPageBlock = BoardPageBlock.create()
        val firstRowString = TelnetClient.model.getRowString(0)

        // 沒有版主, 沒有看板標題
        boardPageBlock.boardManager = ""
        boardPageBlock.boardName = ""

        val regexBoardName: Regex = """《(?<boardName>.*?)》""".trimIndent().toRegex()
        val boardName = regexBoardName.find(firstRowString)

        if (boardName!=null) {
            boardPageBlock.boardTitle = boardName.groups[1]?.value.toString()
        }

        if (boardPageBlock.boardManager != "主題串列") {
            boardPageBlock.boardType = BoardPageAction.LIST
        } else {
            boardPageBlock.boardType = BoardPageAction.SEARCH
        }
        val endIndex = 3 + 20
        var i6 = 3
        val model = TelnetClient.model
        while (i6 < endIndex) {
            val r = model.getRow(i6) ?: break
            val rowStr = r.toString()
            if (rowStr.trim().isEmpty()) {
                i6++
                continue
            }
            r.reloadSpace()
            val articleSelected = r.getSpaceString(0, 0).trim()
            val articleNumber = TelnetUtils.getIntegerFromData(r, 1, 5)
            if (articleNumber != 0) {
                var isSelected = false
                if (articleSelected.isNotEmpty() && articleSelected[0] == '>') {
                    boardPageBlock.selectedItemNumber = articleNumber
                    isSelected = true
                }
                val info = r.getSpaceString(8, 8).trim()
                val originMark = r.getSpaceString(6, 7).trim()
                val title = r.getSpaceString(10, 55).trim()
                val author = r.getSpaceString(56, 68).trim()
                val date = r.getSpaceString(69, 77).trim()
                val item = BoardEssencePageItem.create()
                if (i6 == 3) {
                    boardPageBlock.minimumItemNumber = articleNumber
                }
                boardPageBlock.maximumItemNumber = articleNumber
                item.itemNumber = articleNumber
                item.date = date
                item.author = author
                item.isDeleted = false
                item.title = title
                item.isBBSClickable = originMark != ")"
                item.isDirectory = info == "◆"
                boardPageBlock.setItem(i6 - 3, item)
                if (isSelected) {
                    boardPageBlock.selectedItem = item
                }
            }
            i6++
        }
        return boardPageBlock
    }

    companion object {
        private var _instance: BoardEssencePageHandler? = null
        @JvmStatic
        val instance: BoardEssencePageHandler?
            get() {
                if (_instance == null) {
                    _instance = BoardEssencePageHandler()
                }
                return _instance
            }
    }
}
