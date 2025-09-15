package com.kota.Telnet

import android.text.SpannableString
import com.kota.Bahamut.Service.UserSettings
import com.kota.Telnet.Model.TelnetFrame
import com.kota.Telnet.Model.TelnetRow
import com.kota.Telnet.Reference.TelnetAnsiCode.getBackAsciiCode
import com.kota.Telnet.Reference.TelnetAnsiCode.getTextAsciiCode
import java.util.*

class TelnetArticle {
    companion object {
        const val MINIMUM_REMOVE_QUOTE = 1
        const val NEW = 0
        const val REPLY = 1
    }

    var Title = ""
    var Author = ""
    var BoardName = ""
    var DateTime = ""
    var Nickname = ""
    var fromIP = ""
    var Number = 0
    var Type = 0 // NEW or REPLY

    private val extendItems = Vector<TelnetArticleItem>()
    private var frame: TelnetFrame? = null
    private val info = Vector<TelnetArticleItemInfo>()
    private val items = Vector<TelnetArticleItem>()
    private val mainItems = Vector<TelnetArticleItem>()
    private val push = Vector<TelnetArticlePush>()

    fun setFrameData(rows: Vector<TelnetRow>) {
        frame = TelnetFrame(rows.size)
        for (i in rows.indices) {
            frame?.setRow(i, rows[i].clone())
        }
    }

    fun getFrame(): TelnetFrame? {
        return frame
    }

    fun addMainItem(item: TelnetArticleItem) {
        mainItems.add(item)
    }

    fun addExtendItem(item: TelnetArticleItem) {
        extendItems.add(item)
    }

    fun addInfo(info: TelnetArticleItemInfo) {
        this.info.add(info)
    }

    fun addPush(push: TelnetArticlePush) {
        this.push.add(push)
    }

    fun generateItems() {
        items.clear()
        items.addAll(mainItems)
        items.addAll(extendItems)
    }

    fun clear() {
        Title = ""
        Author = ""
        BoardName = ""
        DateTime = ""
        mainItems.clear()
        items.clear()
        push.clear()
        info.clear()
        frame = null
    }

    fun generateReplyTitle(): String {
        return "Re: $Title"
    }

    /** 設定文章標題 */
    fun generateEditFormat(): String {
        val contentBuffer = StringBuilder()
        val timeString = frame?.getRow(2)?.toString()?.substring(4) ?: ""
        contentBuffer.append("作者: ").append(Author)
        if (!Nickname.isNullOrEmpty()) {
            contentBuffer.append("(").append(Nickname).append(")")
        }
        contentBuffer.append(" 看板: ").append(BoardName).append("\n")
        contentBuffer.append("標題: %s\n")
        contentBuffer.append("時間: ").append(timeString).append("\n")
        contentBuffer.append("\n%s")
        return contentBuffer.toString()
    }

    fun generateEditTitle(): String {
        return frame?.getRow(1)?.toString()?.substring(4) ?: ""
    }

    /** 產生 修改用的文章內容
     * 有附上ASCII色碼 */
    fun generateEditContent(): String {
        val contentBuffer = StringBuilder()
        var paintTextColor = TelnetAnsi.getDefaultTextColor()
        var paintBackColor = TelnetAnsi.getDefaultBackgroundColor()

        var startContentRowIndex = 0
        frame?.let { frameData ->
            // 先找出指定行"時間", 下一行是分隔線, 再下一行是內容
            // 內容如果是空白"", 是發文時系統預設給的, 再往下找一行
            for (i in 0 until frameData.getRowSize()) {
                val row = frameData.getRow(i)
                if (row.getRawString().contains("時間")) {
                    startContentRowIndex = i + 2
                    if (frameData.getRow(startContentRowIndex).getRawString().isEmpty()) {
                        startContentRowIndex += 1
                    }
                    break
                }
            }

            for (rowIndex in startContentRowIndex until frameData.getRowSize()) {
                val row = frameData.getRow(rowIndex)
                val rawString = row.getRawString()
                rawString?.let { content ->
                    // 不用換顏色的內容
                    if (content.matches(Regex("※ .*")) || 
                        content.matches(Regex("> .*")) || 
                        content.matches(Regex("--.*"))) {
                        contentBuffer.append(content).append("\n")
                    } else {
                        // 換顏色
                        val ss = SpannableString(content)
                        val finalString = StringBuilder()
                        val textColor = row.getTextColor()
                        val backColor = row.getBackgroundColor()

                        // 檢查整串字元內有沒有包含預設顏色, 預設不用替換
                        var needReplaceForeColor = false
                        paintTextColor = TelnetAnsi.getDefaultTextColor()
                        paintBackColor = TelnetAnsi.getDefaultBackgroundColor()
                        for (i in textColor.indices) {
                            if (textColor[i] != paintTextColor || backColor[i] != paintBackColor) {
                                if ((i + 1) <= content.length) {
                                    needReplaceForeColor = true
                                }
                                break
                            }
                        }

                        if (needReplaceForeColor) {
                            var isBlink = false
                            for (i in 0 until ss.length) {
                                finalString.append(ss[i])

                                // 有附加顏色
                                if (textColor[i] != paintTextColor || backColor[i] != paintBackColor) {
                                    var appendString = "*["

                                    if (!isBlink && textColor[i] >= 8) {
                                        isBlink = true
                                        appendString += "1;"
                                    } else if (isBlink && textColor[i] < 8) {
                                        isBlink = false
                                        appendString += ";"
                                    }

                                    // 前景代號去除亮色
                                    var noBlinkTextColor = textColor[i].toInt()
                                    if (noBlinkTextColor >= 8) {
                                        noBlinkTextColor -= 8
                                    }
                                    // 舊:前景代號去除亮色
                                    var preBlinkTextColor = paintTextColor.toInt()
                                    if (preBlinkTextColor >= 8) {
                                        preBlinkTextColor -= 8
                                    }

                                    if (noBlinkTextColor != preBlinkTextColor) { // 前景不同
                                        appendString += getTextAsciiCode(noBlinkTextColor.toByte())

                                        if (backColor[i] != paintBackColor) { // 背景不同
                                            appendString += ";" + getBackAsciiCode(backColor[i])
                                        }
                                    } else if (backColor[i] != paintBackColor) { // 背景不同
                                        appendString += getBackAsciiCode(backColor[i])
                                    }
                                    appendString += "m"
                                    finalString.insert(finalString.length - 1, appendString)

                                    // 下一輪
                                    paintTextColor = textColor[i]
                                    paintBackColor = backColor[i]
                                }
                            }
                            // 有替換顏色, 該行最後面加上還原碼
                            finalString.append("*[m")
                        } else {
                            finalString.append(content)
                        }
                        contentBuffer.append(finalString).append("\n")
                    }
                }
            }
        }

        return contentBuffer.toString()
    }

    /** 產生 回應用的文章內容 */
    fun generateReplyContent(): String {
        val contentBuilder = StringBuilder()
        val levelBuffer = HashSet<Int>()
        levelBuffer.add(0)
        for (telnetArticleItemInfo in info) {
            levelBuffer.add(telnetArticleItemInfo.quoteLevel)
        }
        val quoteLevelList = levelBuffer.toTypedArray()
        quoteLevelList.sort()
        
        val maximumQuote = if (quoteLevelList.size < 2) {
            quoteLevelList[quoteLevelList.size - 1]
        } else {
            quoteLevelList[1]
        }

        // 第一個回覆作者(一定不會被黑名單block)
        if (maximumQuote > -1) {
            contentBuilder.append(String.format("※ 引述《%s (%s)》之銘言：\n", Author, Nickname))
        }

        val blockListEnable = UserSettings.getPropertiesBlockListEnable()
        // 逐行上推作者
        for (infoItem in info) {
            if (!(blockListEnable && UserSettings.isBlockListContains(infoItem.author)) && 
                infoItem.quoteLevel <= maximumQuote) {
                for (i in 0 until infoItem.quoteLevel) {
                    contentBuilder.append("> ")
                }
                contentBuilder.append(String.format("※ 引述《%s (%s)》之銘言：\n", infoItem.author, infoItem.nickname))
            }
        }
        
        // 作者內容
        for (item in mainItems) {
            if (!(blockListEnable && UserSettings.isBlockListContains(item.getAuthor())) && 
                item.getQuoteLevel() <= maximumQuote) {
                val rowStrings = item.getContent().split("\n")
                for (append in rowStrings) {
                    for (j in 0..item.getQuoteLevel()) {
                        contentBuilder.append("> ")
                    }
                    contentBuilder.append(append)
                    contentBuilder.append("\n")
                }
            }
        }

        return contentBuilder.toString()
    }

    fun getItemSize(): Int {
        return items.size
    }

    fun getItem(index: Int): TelnetArticleItem? {
        return if (index < 0 || index >= items.size) {
            null
        } else {
            items[index]
        }
    }

    fun getFullText(): String {
        frame?.let { frameData ->
            val builder = StringBuilder()
            val len = frameData.getRowSize()
            for (i in 0 until len) {
                val row = frameData.getRow(i)
                if (i > 0) {
                    builder.append("\n")
                }
                builder.append(row.getRawString())
            }
            return builder.toString()
        }
        return ""
    }

    fun getAuthor(): String {
        return Author
    }

    fun getPushSize(): Int {
        return push.size
    }

    fun getPush(index: Int): TelnetArticlePush? {
        return try {
            push[index]
        } catch (e: Exception) {
            null
        }
    }
}
