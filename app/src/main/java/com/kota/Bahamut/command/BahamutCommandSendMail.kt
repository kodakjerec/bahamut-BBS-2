package com.kota.Bahamut.command

import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnet.TelnetCommand
import com.kota.telnet.TelnetOutputBuilder.Companion.create

class BahamutCommandSendMail(var receiver: String, var title: String, var content: String) :
    com.kota.Bahamut.command.TelnetCommand() {
    init {
        action = BahamutCommandDef.Companion.SEND_MAIL
    }

    override fun execute(telnetListPage: TelnetListPage) {
        if (receiver.isNotEmpty() && title.isNotEmpty() && content.isNotEmpty()) {
            val buffer = create()
            buffer.pushKey(TelnetKeyboard.SHIFT_M)
            buffer.pushString(receiver + "\n")
            buffer.pushString(title + "\n")
            buffer.pushString("0" + "\n") // 選擇簽名檔 (1 ~ 9, 0=不加)[0]：

            // 重新貼文
            // 將內文依照 *[ 切換成多段
            val outputs: MutableList<String> =
                BahamutCommandPostArticle.Companion.convertContentToStringList(content)
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

    override fun executeFinished(telnetListPage: TelnetListPage, telnetListPageBlock: TelnetListPageBlock) {
        isDone = true
    }

    override fun toString(): String {
        return "[SendMail][title=$title receiver=$receiver content=$content]"
    }
}
