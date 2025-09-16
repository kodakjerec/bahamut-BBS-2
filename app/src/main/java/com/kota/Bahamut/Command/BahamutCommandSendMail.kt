package com.kota.Bahamut.Command

import com.kota.Bahamut.ListPage.TelnetListPage
import com.kota.Bahamut.ListPage.TelnetListPageBlock
import com.kota.Telnet.Reference.TelnetKeyboard
import com.kota.Telnet.TelnetCommand
import com.kota.Telnet.TelnetOutputBuilder.Companion.create

class BahamutCommandSendMail(var _receiver: String, var _title: String, var _content: String) :
    com.kota.Bahamut.Command.TelnetCommand() {
    init {
        Action = BahamutCommandDefs.Companion.SendMail
    }

    override fun execute(aListPage: TelnetListPage?) {
        if (_receiver.length > 0 && _title.length > 0 && _content.length > 0) {
            val buffer = create()
            buffer.pushKey(TelnetKeyboard.SHIFT_M)
            buffer.pushString(_receiver + "\n")
            buffer.pushString(_title + "\n")
            buffer.pushString("0" + "\n") // 選擇簽名檔 (1 ~ 9, 0=不加)[0]：

            // 重新貼文
            // 將內文依照 *[ 切換成多段
            val outputs: MutableList<String> =
                BahamutCommandPostArticle.Companion.convertContentToStringList(_content)
            // 貼入內文
            for (output in outputs) {
                if (output == "*")  // 跳脫字元
                    buffer.pushKey(TelnetKeyboard.CTRL_U)
                else buffer.pushString(output)
            }

            buffer.pushData(TelnetCommand.TERMINAL_TYPE)
            buffer.pushString("s\n") // S-存檔
            buffer.pushString("N\n\n") // 是否自存底稿(Y/N)？[N]
            buffer.sendToServer()
        }
    }

    override fun executeFinished(aListPage: TelnetListPage?, aPageData: TelnetListPageBlock?) {
        setDone(true)
    }

    override fun toString(): String {
        return "[SendMail][title=" + _title + " receiver=" + _receiver + " content=" + _content + "]"
    }
}
