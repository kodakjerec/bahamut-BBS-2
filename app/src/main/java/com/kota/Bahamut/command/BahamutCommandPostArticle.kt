package com.kota.Bahamut.command

import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.Bahamut.service.CommonFunctions.judgeDoubleWord
import com.kota.telnet.model.TelnetFrame
import com.kota.telnet.reference.TelnetKeyboard
import com.kota.telnet.TelnetOutputBuilder.Companion.create
import kotlin.math.ceil

class BahamutCommandPostArticle(
    telnetListPage: TelnetListPage?,
    title: String,
    content: String,
    aTarget: String?,
    aArticleNumber: String?,
    aSign: String?,
    isRecoverPost: Boolean
) : TelnetCommand() {
    var articleNumber: String?
    var myContent: String
    var listPage: TelnetListPage?
    var sign: String?
    var target: String?
    // 標題: 第80個字元如果是雙字元則截斷
    var title: String? = judgeDoubleWord(title, TelnetFrame.Companion.DEFAULT_COLUMN - 9).split("\n".toRegex())
        .dropLastWhile { it.isEmpty() }.toTypedArray()[0]

    var isRecoverPost: Boolean

    init {
        // 內文: 一行超過80個字元預先截斷, 第80個字元如果是雙字元則先截斷, 這個雙字元歸類到下一行
        myContent = judgeDoubleWord(content, TelnetFrame.Companion.DEFAULT_COLUMN - 2)
        action = BahamutCommandDef.Companion.POST_ARTICLE
        target = aTarget
        articleNumber = aArticleNumber
        listPage = telnetListPage
        sign = aSign
        if (sign == null) {
            sign = ""
        }
        this@BahamutCommandPostArticle.isRecoverPost = isRecoverPost
    }

    // com.kota.Bahamut.Command.TelnetCommand
    override fun execute(telnetListPage: TelnetListPage) {
        var buffer = create()
        // Reply
        if (articleNumber != null && target != null) {
            if (isRecoverPost) {
                // 砍掉所有內文重新貼上
                val pageUpCounts =
                    ceil(myContent.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray().size.toDouble() / 23).toInt() + 1
                for (i in 0..<pageUpCounts) {
                    buffer.pushKey(TelnetKeyboard.PAGE_UP) // ctrl+B, pageUp
                }
                buffer.pushKey(TelnetKeyboard.CTRL_G) // ^G 刪除目前這行之後至檔案結尾
                buffer.pushKey(TelnetKeyboard.CTRL_Y) // ^Y 刪除目前這行
                buffer.sendToServer()

                // 內文全部刪除

                // 重新貼文
                buffer = create()
                // 將內文依照 *[ 切換成多段
                val outputs: MutableList<String> = convertContentToStringList(myContent)
                // 貼入內文
                for (output in outputs) {
                    if (output == "*")  // 跳脫字元
                        buffer.pushKey(TelnetKeyboard.CTRL_U)
                    else buffer.pushString(output)
                }
            } else {
                buffer.pushString(articleNumber + "\n") // 指定文章編號, 按下Enter
                buffer.pushString("y") // y-回應
                buffer.pushString(target + "\n") // 回應至 (F)看板 (M)作者信箱 (B)二者皆是 (Q)取消？[F]
                if (target != null && (target == "F" || target == "B")) {
                    buffer.pushString("\n") // 類別: a)問題 b)情報 c)心得 d)討論 e)攻略 f)秘技 g)閒聊 h)其它 :
                }
                if (title != null) {
                    buffer.pushKey(TelnetKeyboard.CTRL_Y) // ctrl + Y
                    buffer.pushString(title + "\n")
                    buffer.pushString("N\n") // 請問要引用原文嗎(Y/N/All/Repost/1-9)？[Y]
                    buffer.pushString(sign + "\n") // 選擇簽名檔 (1 ~ 9, 0=不加)[0]：
                } else {
                    buffer.pushString("\n")
                    buffer.pushString("N\n") // 請問要引用原文嗎(Y/N/All/Repost/1-9)？[Y]
                    buffer.pushString(sign + "\n") // 選擇簽名檔 (1 ~ 9, 0=不加)[0]：
                }

                // 重新貼文
                // 將內文依照 *[ 切換成多段
                val outputs: MutableList<String> = convertContentToStringList(myContent)
                // 貼入內文
                for (output in outputs) {
                    if (output == "*")  // 跳脫字元
                        buffer.pushKey(TelnetKeyboard.CTRL_U)
                    else buffer.pushString(output)
                }
            }
            buffer.pushKey(TelnetKeyboard.CTRL_X) // ctrl+x 存檔
            buffer.pushString("s\n") // S-存檔
            if (target == "M") {
                buffer.pushString("Y\n\n") // 是否自存底稿(Y/N)？[N]
            }
            buffer.sendToServer()
        } else {
            // New
            buffer.pushKey(TelnetKeyboard.CTRL_P) // Ctrl+P
            buffer.pushString("\n")
            buffer.pushString(title!!)
            buffer.pushString("\n" + sign + "\n")

            // 貼文
            // 將內文依照 *[ 切換成多段
            val outputs: MutableList<String> = convertContentToStringList(myContent)
            // 貼入內文
            for (output in outputs) {
                if (output == "*")  // 跳脫字元
                    buffer.pushKey(21)
                else buffer.pushString(output)
            }

            buffer.pushKey(TelnetKeyboard.CTRL_X) // ctrl+x 存檔
            buffer.pushString("s\n")
            buffer.sendToServer()
        }
    }

    // com.kota.Bahamut.Command.TelnetCommand
    override fun executeFinished(
        telnetListPage: TelnetListPage,
        telnetListPageBlock: TelnetListPageBlock?
    ) {
        telnetListPage.pushPreloadCommand(0)
        isDone = true
    }

    override fun toString(): String {
        return "[PostArticle][title=$title content$myContent]"
    }

    companion object {
        fun convertContentToStringList(content: String): MutableList<String> {
            // 將內文依照 *[ 切換成多段
            var myContent = content
            val outputs: MutableList<String> = ArrayList()
            var endIndex: Int
            while (true) {
                endIndex = myContent.indexOf("*[")
                if (endIndex > -1) {
                    outputs.add(myContent.substring(0, endIndex))
                    outputs.add("*") // 標記起來之後用來替換字元
                    myContent = myContent.substring(endIndex + 1)
                } else {
                    outputs.add(myContent)
                    break
                }
            }

            return outputs
        }
    }
}
