package com.kota.Bahamut.command

import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.Bahamut.service.CommonFunctions.judgeDoubleWord
import com.kota.telnet.model.TelnetFrame
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnet.TelnetClient
import com.kota.telnet.TelnetCommand
import com.kota.telnet.TelnetOutputBuilder.Companion.create

class BahamutCommandEditArticle(var articleNumber: String?, title: String, content: String) :
    com.kota.Bahamut.command.TelnetCommand() {
    // 內文: 一行超過80個字元預先截斷, 第80個字元如果是雙字元則先截斷, 這個雙字元歸類到下一行
    var myContent: String? = judgeDoubleWord(content, TelnetFrame.Companion.DEFAULT_COLUMN - 2)

    // 標題: 第80個字元如果是雙字元則截斷
    var myTitle: String? = judgeDoubleWord(title, TelnetFrame.Companion.DEFAULT_COLUMN - 9).split("\n".toRegex())
        .dropLastWhile { it.isEmpty() }.toTypedArray()[0]

    init {
        action = BahamutCommandDef.Companion.POST_ARTICLE
    }

    override fun execute(telnetListPage: TelnetListPage) {
        if (articleNumber != null && myContent != null && myContent?.isNotEmpty()) {
            // 將內文依照 *[ 切換成多段
            val outputs: MutableList<String> =
                BahamutCommandPostArticle.Companion.convertContentToStringList(myContent!!)

            val builder = create()
                .pushString("$articleNumber\nE")
                .pushData(TelnetKeyboard.CTRL_G.toByte()) // ^G 刪除目前這行之後至檔案結尾
                .pushData(TelnetKeyboard.CTRL_Y.toByte()) // ^Y 刪除目前這行
            // 貼入內文
            for (output in outputs) {
                if (output == "*")  // 跳脫字元
                    builder.pushData(21.toByte())
                else builder.pushString(output)
            }
            // 結束
            builder.pushData(TelnetCommand.TERMINAL_TYPE)
                .pushString("S\n")
            if (myTitle != null) {
                builder.pushString("T").pushData(25.toByte()).pushString("$myTitle\nY\n")
            }
            TelnetClient.client?.sendDataToServer(builder.build())
        }
    }

    override fun executeFinished(telnetListPage: TelnetListPage, telnetListPageBlock: TelnetListPageBlock?) {
        isDone = true
    }

    override fun toString(): String {
        return "[EditArticle][articleIndex=$articleNumber title=$myTitle content=$myContent]"
    }
}
