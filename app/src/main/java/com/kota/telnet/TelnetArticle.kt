package com.kota.telnet

import android.text.SpannableString
import com.kota.Bahamut.service.UserSettings
import com.kota.telnet.model.TelnetFrame
import com.kota.telnet.model.TelnetRow
import com.kota.telnet.reference.TelnetAnsiCode.getBackAsciiCode
import com.kota.telnet.reference.TelnetAnsiCode.getTextAsciiCode
import java.util.Arrays
import java.util.Vector

class TelnetArticle {
    @JvmField
    var title: String = ""
    var author: String = ""
    @JvmField
    var boardName: String = ""
    @JvmField
    var dateTime: String = ""
    @JvmField
    var nickName: String? = ""
    @JvmField
    var fromIP: String = ""
    @JvmField
    var myNumber: Int = 0
    var articleType: Int = 0 // NEW or REPLY
    private val extendItems = Vector<TelnetArticleItem>()
    var frame: TelnetFrame? = null
        private set
    private val infos = Vector<TelnetArticleItemInfo>()
    private val items = Vector<TelnetArticleItem?>()
    private val mainItems = Vector<TelnetArticleItem>()
    private val pushes = Vector<TelnetArticlePush?>()

    fun setFrameData(rows: Vector<TelnetRow>) {
        this.frame = TelnetFrame(rows.size)
        for (i in rows.indices) {
            frame?.setRow(i, rows[i].clone())
        }
    }

    fun addMainItem(aItem: TelnetArticleItem?) {
        mainItems.add(aItem)
    }

    fun addExtendItem(aItem: TelnetArticleItem?) {
        extendItems.add(aItem)
    }

    fun addInfo(aInfo: TelnetArticleItemInfo?) {
        infos.add(aInfo)
    }

    val infoSize: Int
        get() = infos.size

    fun getInfo(index: Int): TelnetArticleItemInfo? {
        return infos[index]
    }

    fun addPush(aPush: TelnetArticlePush?) {
        pushes.add(aPush)
    }

    fun build() {
        for (mainItem in mainItems) {
            mainItem.build()
        }
        for (extendItem in extendItems) {
            extendItem.build()
        }
        val removeItems = Vector<TelnetArticleItem?>()
        for (item in mainItems) {
            if (item.isEmpty) {
                removeItems.add(item)
            }
        }
        for (removeItem in removeItems) {
            mainItems.remove(removeItem)
        }
        removeItems.clear()
        for (item2 in extendItems) {
            if (item2.isEmpty) {
                removeItems.add(item2)
            }
        }
        for (removeItem in removeItems) {
            extendItems.remove(removeItem)
        }
        removeItems.clear()
        if (!extendItems.isEmpty()) {
            extendItems.lastElement().type = 1
        }
        items.clear()
        items.addAll(mainItems)
        items.addAll(extendItems)
    }

    fun clear() {
        title = ""
        this.author = ""
        boardName = ""
        dateTime = ""
        mainItems.clear()
        items.clear()
        pushes.clear()
        infos.clear()
        this.frame = null
    }

    fun generateReplyTitle(): String {
        return "Re: $title"
    }

    /** 設定文章標題  */
    fun generateEditFormat(): String {
        val contentBuffer = StringBuilder()
        val timeString = frame?.getRow(2).toString().substring(4)
        contentBuffer.append("作者: ").append(this.author)
        if (nickName != null && !nickName!!.isEmpty()) {
            contentBuffer.append("(").append(nickName).append(")")
        }
        contentBuffer.append(" 看板: ").append(boardName).append("\n")
        contentBuffer.append("標題: %s\n")
        contentBuffer.append("時間: ").append(timeString).append("\n")
        contentBuffer.append("\n%s")
        return contentBuffer.toString()
    }

    fun generateEditTitle(): String {
        return frame!!.getRow(1).toString().substring(4)
    }

    /** 產生 修改用的文章內容
     * 有附上ASCII色碼  */
    fun generateEditContent(): String {
        val contentBuffer = StringBuilder()
        var paintTextColor: Byte
        var paintBackColor: Byte

        var startContentRowIndex = 0
        // 先找出指定行"時間", 下一行是分隔線, 再下一行是內容
        // 內容如果是空白"", 是發文時系統預設給的, 再往下找一行
        for (i in 0..<frame!!.rowSize) {
            val currentRow = frame!!.getRow(i)
            if (currentRow.rawString.contains("時間")) {
                startContentRowIndex = i + 2
                if (frame!!.getRow(startContentRowIndex).rawString.isEmpty()) startContentRowIndex += 1
                break
            }
        }

        for (rowIndex in startContentRowIndex..<frame!!.rowSize) {
            val currentRow = frame!!.getRow(rowIndex)
            val rawString = currentRow.rawString
            if (rawString.matches("※ .*".toRegex()) || rawString.matches("> .*".toRegex()) || rawString.matches(
                    "--.*".toRegex()
                )
            ) {
                contentBuffer.append(rawString).append("\n")
            } else {
                // 換顏色
                val ss = SpannableString(rawString)
                val finalString = StringBuilder()
                val textColor: ByteArray = currentRow.getTextColorArray()!!
                val backColor = currentRow.getBackgroundColor()

                // 檢查整串字元內有沒有包含預設顏色, 預設不用替換
                var needReplaceForeColor = false
                paintTextColor = TelnetAnsi.DEFAULT_TEXT_COLOR
                paintBackColor = TelnetAnsi.DEFAULT_BACKGROUND_COLOR
                for (i in textColor.indices) {
                    if (textColor[i] != paintTextColor || backColor!![i] != paintBackColor) {
                        if ((i + 1) <= rawString.length) {
                            needReplaceForeColor = true
                        }
                        break
                    }
                }

                if (needReplaceForeColor) {
                    var isBlink = false
                    for (i in 0..<ss.length) {
                        finalString.append(ss.get(i))

                        // 有附加顏色
                        if (textColor[i] != paintTextColor || backColor!![i] != paintBackColor) {
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
                            if (noBlinkTextColor >= 8) noBlinkTextColor = noBlinkTextColor - 8
                            // 舊:前景代號去除亮色
                            var preBlinkTextColor = paintTextColor.toInt()
                            if (preBlinkTextColor >= 8) preBlinkTextColor =
                                preBlinkTextColor - 8

                            if (noBlinkTextColor != preBlinkTextColor) { // 前景不同
                                appendString += getTextAsciiCode(noBlinkTextColor)

                                if (backColor!![i] != paintBackColor) { // 背景不同
                                    appendString += ";" + getBackAsciiCode(backColor[i])
                                }
                            } else if (backColor!![i] != paintBackColor) { // 背景不同
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
                    finalString.append(rawString)
                }
                contentBuffer.append(finalString).append("\n")
            }
        }


        return contentBuffer.toString()
    }

    /** 產生 回應用的文章內容
     */
    fun generateReplyContent(): String {
        val maximumQuote: Int // 回應作者的階層, 0-父 1-祖父
        val contentBuilder = StringBuilder()
        val levelBuffer: MutableSet<Int?> = HashSet()
        levelBuffer.add(0)
        for (telnetArticleItemInfo in infos) {
            levelBuffer.add(telnetArticleItemInfo.quoteLevel)
        }
        val quoteLevelList = levelBuffer.toTypedArray<Int?>()
        Arrays.sort(quoteLevelList)
        maximumQuote = if (quoteLevelList.size < 2) {
            quoteLevelList[quoteLevelList.size - 1]!!
        } else {
            quoteLevelList[1]!!
        }

        // 第一個回覆作者(一定不會被黑名單block)
        if (maximumQuote > -1) contentBuilder.append(
            String.format(
                "※ 引述《%s (%s)》之銘言：\n",
                this.author, nickName
            )
        )

        val blockListEnable = UserSettings.propertiesBlockListEnable
        // 逐行上推作者
        for (info in infos) {
            if (!(blockListEnable && UserSettings.isBlockListContains(info.author)) && info.quoteLevel <= maximumQuote) {
                for (i in 0..<info.quoteLevel) {
                    contentBuilder.append("> ")
                }
                contentBuilder.append(
                    String.format(
                        "※ 引述《%s (%s)》之銘言：\n",
                        info.author,
                        info.nickname
                    )
                )
            }
        }
        // 作者內容
        for (item in mainItems) {
            if (!(blockListEnable && UserSettings.isBlockListContains(item.author)) && item.quoteLevel <= maximumQuote) {
                val rowStrings: Array<String?> =
                    item.content.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                for (append in rowStrings) {
                    for (j in 0..item.quoteLevel) {
                        contentBuilder.append("> ")
                    }
                    contentBuilder.append(append)
                    contentBuilder.append("\n")
                }
            }
        }

        return contentBuilder.toString()
    }

    val itemSize: Int
        get() = items.size

    fun getItem(index: Int): TelnetArticleItem? {
        if (index < 0 || index >= items.size) {
            return null
        }
        return items[index]
    }

    val fullText: String
        get() {
            if (this.frame == null) {
                return ""
            }
            val builder = StringBuilder()
            val len = frame!!.rowSize
            for (i in 0..<len) {
                val row = frame!!.getRow(i)
                if (i > 0) {
                    builder.append("\n")
                }
                builder.append(row.rawString)
            }
            return builder.toString()
        }

    val pushSize: Int
        get() = pushes.size

    fun getPush(index: Int): TelnetArticlePush? {
        return try {
            pushes[index]
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        const val MINIMUM_REMOVE_QUOTE: Int = 1
        const val NEW: Int = 0
        const val REPLY: Int = 1
    }
}
