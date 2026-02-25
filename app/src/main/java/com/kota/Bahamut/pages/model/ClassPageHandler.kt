package com.kota.Bahamut.pages.model

import com.kota.telnet.model.TelnetRow
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetUtils
import com.kota.telnet.logic.ClassMode

class ClassPageHandler private constructor() {
    fun load(): ClassPageBlock {
        val classPackage: ClassPageBlock = ClassPageBlock.Companion.create()
        // TODO: 原本設計是 編號/總數 共用, 但是總數目前無法逐步新增
        if (TelnetClient.model.getRowString(2).trim().startsWith("編號")) {
            classPackage.mode = ClassMode.INDEX
        } else {
            classPackage.mode = ClassMode.COUNT
        }
        val endIndex = 3 + 20
        var i = 3
        while (i < endIndex && TelnetClient.model.getRowString(i).isNotEmpty()) {
            val row: TelnetRow? = TelnetClient.model.getRow(i)
            // 看板編號
            val boardIndex = TelnetUtils.getIntegerFromData(row!!, 1, 5)
            if (i == 3) {
                classPackage.minimumItemNumber = boardIndex
            }
            val boardSelected = row.getSpaceString(0, 0).trim()
            if (boardSelected.isNotEmpty() && boardSelected[0] == '>') {
                classPackage.selectedItemNumber = boardIndex
            }
            var boardNameEnd = 9
            var j = 0
            while (j < 12 && row.data[j + 9].toInt() != 32) {
                boardNameEnd = j + 9
                j++
            }
            var boardManagerStart = 65
            // 看板英文名稱
            var boardName = row.getSpaceString(9, boardNameEnd).trim()
            // 看板中文名稱
            // 先取得除英文名稱外全字串, 扣除版主群後, 剩下的就是看板中文名稱
            var boardTitle =
                row.getSpaceString(boardNameEnd + 1, row.data.size - 1).trim()
            for (bb in boardTitle.length - 1 downTo 0) {
                if (boardTitle[bb].code == 32) {
                    boardManagerStart = bb
                    break
                }
            }
            // 版主群
            val boardManager = boardTitle.substring(boardManagerStart + 1, boardTitle.length)

            // 得到看板中文名稱
            boardTitle = boardTitle.replace(boardManager, "")
            val item: ClassPageItem = ClassPageItem.Companion.create()
            if (boardName.endsWith("/")) {
                item.isDirectory = true
                boardName = boardName.substring(0, boardName.length - 1)
            } else {
                item.isDirectory = false
                if (boardTitle.length > 2) {
                    boardTitle = boardTitle.substring(2)
                }
            }
            item.name = boardName
            item.title = boardTitle
            item.manager = boardManager
            classPackage.maximumItemNumber = boardIndex
            item.itemNumber = boardIndex
            classPackage.setItem(i - 3, item)
            i++
        }
        return classPackage
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
