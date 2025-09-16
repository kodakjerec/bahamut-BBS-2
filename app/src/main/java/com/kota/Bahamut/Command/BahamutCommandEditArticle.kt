package com.kota.Bahamut.Command

import com.kota.Bahamut.ListPage.TelnetListPage
import com.kota.Bahamut.ListPage.TelnetListPageBlock
import com.kota.Bahamut.Service.CommonFunctions.judgeDoubleWord
import com.kota.Telnet.Model.TelnetFrame
import com.kota.Telnet.Reference.TelnetKeyboard
import com.kota.Telnet.TelnetClient
import com.kota.Telnet.TelnetCommand
import com.kota.Telnet.TelnetOutputBuilder.Companion.create

class BahamutCommandEditArticle(var _article_number: String?, title: String, content: String) :
    com.kota.Bahamut.Command.TelnetCommand() {
    var _content: String?
    var _title: String?

    init {
        // 標題: 第80個字元如果是雙字元則截斷
        _title =
            judgeDoubleWord(title, TelnetFrame.Companion.DEFAULT_COLUMN - 9).split("\n".toRegex())
                .dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        // 內文: 一行超過80個字元預先截斷, 第80個字元如果是雙字元則先截斷, 這個雙字元歸類到下一行
        _content = judgeDoubleWord(content, TelnetFrame.Companion.DEFAULT_COLUMN - 2)
        Action = BahamutCommandDefs.Companion.PostArticle
    }

    override fun execute(aListPage: TelnetListPage?) {
        if (_article_number != null && _content != null && _content!!.length > 0) {
            // 將內文依照 *[ 切換成多段
            val outputs: MutableList<String> =
                BahamutCommandPostArticle.Companion.convertContentToStringList(_content)

            val builder = create()
                .pushString(_article_number + "\nE")
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
            if (_title != null) {
                builder.pushString("T").pushData(25.toByte()).pushString(_title + "\nY\n")
            }
            TelnetClient.getClient().sendDataToServer(builder.build())
        }
    }

    override fun executeFinished(aListPage: TelnetListPage?, aPageData: TelnetListPageBlock?) {
        setDone(true)
    }

    override fun toString(): String {
        return "[EditArticle][articleIndex=" + _article_number + " title=" + _title + " content=" + _content + "]"
    }
}
