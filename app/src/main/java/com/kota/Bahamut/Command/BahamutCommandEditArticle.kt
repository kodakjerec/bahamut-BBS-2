package com.kota.Bahamut.Command

import com.kota.Bahamut.Command.BahamutCommandPostArticle.convertContentToStringList
import com.kota.Bahamut.ListPage.TelnetListPage
import com.kota.Bahamut.ListPage.TelnetListPageBlock
import com.kota.Bahamut.Service.CommonFunctions.judgeDoubleWord
import com.kota.Telnet.Model.TelnetFrame.DEFAULT_COLUMN
import com.kota.Telnet.Reference.TelnetKeyboard
import com.kota.Telnet.TelnetClient
import com.kota.Telnet.TelnetCommand
import com.kota.Telnet.TelnetOutputBuilder

class BahamutCommandEditArticle(
    private val _article_number: String,
    title: String,
    content: String
) : TelnetCommand() {
    
    private val _title: String
    private val _content: String

    init {
        // 標題: 第80個字元如果是雙字元則截斷
        _title = judgeDoubleWord(title, DEFAULT_COLUMN - 9).split("\n")[0]
        // 內文: 一行超過80個字元預先截斷, 第80個字元如果是雙字元則先截斷, 這個雙字元歸類到下一行
        _content = judgeDoubleWord(content, DEFAULT_COLUMN - 2)
        Action = BahamutCommandDefs.PostArticle
    }

    override fun execute(aListPage: TelnetListPage) {
        if (_content.isNotEmpty()) {
            // 將內文依照 *[ 切換成多段
            val outputs = convertContentToStringList(_content)

            val builder = TelnetOutputBuilder.create()
                .pushString("$_article_number\nE")
                .pushData(TelnetKeyboard.CTRL_G.toByte()) // ^G 刪除目前這行之後至檔案結尾
                .pushData(TelnetKeyboard.CTRL_Y.toByte()) // ^Y 刪除目前這行
            
            // 貼入內文
            for (output in outputs) {
                if (output == "*") { // 跳脫字元
                    builder.pushData(21.toByte())
                } else {
                    builder.pushString(output)
                }
            }
            
            // 結束
            builder.pushData(TelnetCommand.TERMINAL_TYPE)
                .pushString("S\n")
            
            if (_title.isNotEmpty()) {
                builder.pushString("T").pushData(25.toByte()).pushString("$_title\nY\n")
            }
            
            TelnetClient.getClient().sendDataToServer(builder.build())
        }
    }

    override fun executeFinished(aListPage: TelnetListPage, aPageData: TelnetListPageBlock) {
        setDone(true)
    }

    override fun toString(): String {
        return "[EditArticle][articleIndex=$_article_number title=$_title content=$_content]"
    }
}
