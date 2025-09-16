package com.kota.Bahamut.Command

import com.kota.Bahamut.ListPage.TelnetListPage
import com.kota.Bahamut.ListPage.TelnetListPageBlock
import com.kota.Bahamut.Service.CommonFunctions.judgeDoubleWord
import com.kota.Telnet.Model.TelnetFrame
import com.kota.Telnet.Reference.TelnetKeyboard
import com.kota.Telnet.TelnetOutputBuilder.Companion.create
import kotlin.math.ceil

class BahamutCommandPostArticle(
    aListPage: TelnetListPage?,
    title: String,
    content: String,
    aTarget: String?,
    aArticleNumber: String?,
    aSign: String?,
    isRecoverPost: Boolean
) : TelnetCommand() {
    var _article_number: String?
    var _content: String
    var _list_page: TelnetListPage?
    var _sign: String?
    var _target: String?
    var _title: String?

    var _isRecoverPost: Boolean

    init {
        // 標題: 第80個字元如果是雙字元則截斷
        _title =
            judgeDoubleWord(title, TelnetFrame.Companion.DEFAULT_COLUMN - 9).split("\n".toRegex())
                .dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        // 內文: 一行超過80個字元預先截斷, 第80個字元如果是雙字元則先截斷, 這個雙字元歸類到下一行
        _content = judgeDoubleWord(content, TelnetFrame.Companion.DEFAULT_COLUMN - 2)
        Action = BahamutCommandDefs.Companion.PostArticle
        _target = aTarget
        _article_number = aArticleNumber
        _list_page = aListPage
        _sign = aSign
        if (_sign == null) {
            _sign = ""
        }
        _isRecoverPost = isRecoverPost
    }

    // com.kota.Bahamut.Command.TelnetCommand
    override fun execute(aListPage: TelnetListPage?) {
        var buffer = create()
        // Reply
        if (_article_number != null && _target != null) {
            if (_isRecoverPost) {
                // 砍掉所有內文重新貼上
                val pageUpCounts =
                    ceil(_content.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
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
                val outputs: MutableList<String> = convertContentToStringList(_content)
                // 貼入內文
                for (output in outputs) {
                    if (output == "*")  // 跳脫字元
                        buffer.pushKey(TelnetKeyboard.CTRL_U)
                    else buffer.pushString(output)
                }
            } else {
                buffer.pushString(_article_number + "\n") // 指定文章編號, 按下Enter
                buffer.pushString("y") // y-回應
                buffer.pushString(_target + "\n") // 回應至 (F)看板 (M)作者信箱 (B)二者皆是 (Q)取消？[F]
                if (_target != null && (_target == "F" || _target == "B")) {
                    buffer.pushString("\n") // 類別: a)問題 b)情報 c)心得 d)討論 e)攻略 f)秘技 g)閒聊 h)其它 :
                }
                if (_title != null) {
                    buffer.pushKey(TelnetKeyboard.CTRL_Y) // ctrl + Y
                    buffer.pushString(_title + "\n")
                    buffer.pushString("N\n") // 請問要引用原文嗎(Y/N/All/Repost/1-9)？[Y]
                    buffer.pushString(_sign + "\n") // 選擇簽名檔 (1 ~ 9, 0=不加)[0]：
                } else {
                    buffer.pushString("\n")
                    buffer.pushString("N\n") // 請問要引用原文嗎(Y/N/All/Repost/1-9)？[Y]
                    buffer.pushString(_sign + "\n") // 選擇簽名檔 (1 ~ 9, 0=不加)[0]：
                }

                // 重新貼文
                // 將內文依照 *[ 切換成多段
                val outputs: MutableList<String> = convertContentToStringList(_content)
                // 貼入內文
                for (output in outputs) {
                    if (output == "*")  // 跳脫字元
                        buffer.pushKey(TelnetKeyboard.CTRL_U)
                    else buffer.pushString(output)
                }
            }
            buffer.pushKey(TelnetKeyboard.CTRL_X) // ctrl+x 存檔
            buffer.pushString("s\n") // S-存檔
            if (_target == "M") {
                buffer.pushString("Y\n\n") // 是否自存底稿(Y/N)？[N]
            }
            buffer.sendToServer()
        } else {
            // New
            buffer.pushKey(TelnetKeyboard.CTRL_P) // Ctrl+P
            buffer.pushString("\n")
            buffer.pushString(_title!!)
            buffer.pushString("\n" + _sign + "\n")

            // 貼文
            // 將內文依照 *[ 切換成多段
            val outputs: MutableList<String> = convertContentToStringList(_content)
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
    override fun executeFinished(aListPage: TelnetListPage, aPageData: TelnetListPageBlock?) {
        aListPage.pushPreloadCommand(0)
        setDone(true)
    }

    override fun toString(): String {
        return "[PostArticle][title=" + _title + " content" + _content + "]"
    }

    companion object {
        fun convertContentToStringList(_content: String): MutableList<String> {
            // 將內文依照 *[ 切換成多段
            var _content = _content
            val outputs: MutableList<String> = ArrayList<String>()
            var endIndex: Int
            while (true) {
                endIndex = _content.indexOf("*[")
                if (endIndex > -1) {
                    outputs.add(_content.substring(0, endIndex))
                    outputs.add("*") // 標記起來之後用來替換字元
                    _content = _content.substring(endIndex + 1)
                } else {
                    outputs.add(_content)
                    break
                }
            }

            return outputs
        }
    }
}
