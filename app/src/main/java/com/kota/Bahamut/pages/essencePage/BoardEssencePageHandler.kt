package com.kota.Bahamut.pages.essencePage

import com.kota.Bahamut.pages.boardPage.BoardPageAction
import com.kota.Bahamut.pages.model.BoardEssencePageItem
import com.kota.Bahamut.pages.model.BoardPageBlock
import com.kota.telnet.model.TelnetRow
import com.kota.telnet.TelnetClient.getModel
import com.kota.telnet.TelnetUtils

class BoardEssencePageHandler private constructor() {
    fun load(): BoardPageBlock {
        var row: TelnetRow
        val boardPageBlock = BoardPageBlock.create()
        val firstRowString = getModel().getRowString(0)

        // 沒有版主, 沒有看板標題
        boardPageBlock.BoardManager = ""
        boardPageBlock.BoardName = ""

        val regexBoardName: Regex = """《(?<boardName>.*?)》""".trimIndent().toRegex()
        val boardName = regexBoardName.find(firstRowString)

        if (boardName!=null) {
            boardPageBlock.BoardTitle = boardName.groups[1]!!.value
        }

        if (boardPageBlock.BoardManager == null || boardPageBlock.BoardManager != "主題串列") {
            boardPageBlock.Type = BoardPageAction.LIST
        } else {
            boardPageBlock.Type = BoardPageAction.SEARCH
        }
        val endIndex = 3 + 20
        var i6 = 3
        row = getModel().getRow(i6)
        while (i6 < endIndex && row.toString().isNotEmpty()) {
            row.reloadSpace()
            val articleSelected = row.getSpaceString(0, 0).trim()
            val articleNumber = TelnetUtils.getIntegerFromData(row, 1, 5)
            if (articleNumber != 0) {
                var isSelected = false
                if (articleSelected.isNotEmpty() && articleSelected[0] == '>') {
                    boardPageBlock.selectedItemNumber = articleNumber
                    isSelected = true
                }
                val info = row.getSpaceString(8, 8).trim()
                val originMark = row.getSpaceString(6, 7).trim()
                val title = row.getSpaceString(10, 55).trim()
                val author = row.getSpaceString(56, 68).trim()
                val date = row.getSpaceString(69, 77).trim()
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
            row = getModel().getRow(i6)
        }
        return boardPageBlock
    }

    companion object {
        private var _instance: BoardEssencePageHandler? = null
        @kotlin.jvm.JvmStatic
        val instance: BoardEssencePageHandler?
            get() {
                if (_instance == null) {
                    _instance = BoardEssencePageHandler()
                }
                return _instance
            }
    }
}
