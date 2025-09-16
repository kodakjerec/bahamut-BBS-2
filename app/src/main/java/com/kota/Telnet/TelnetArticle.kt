package com.kota.Telnet

import android.text.SpannableString
import com.kota.Bahamut.Service.UserSettings
import com.kota.Telnet.Model.TelnetFrame
import com.kota.Telnet.Model.TelnetRow
import com.kota.Telnet.Reference.TelnetAnsiCode.getBackAsciiCode
import com.kota.Telnet.Reference.TelnetAnsiCode.getTextAsciiCode
import java.util.Arrays
import java.util.Vector

class TelnetArticle {
    @JvmField
    var Title: String = ""
    var author: String = ""
    @JvmField
    var BoardName: String = ""
    @JvmField
    var DateTime: String = ""
    @JvmField
    var Nickname: String? = ""
    @JvmField
    var fromIP: String = ""
    @JvmField
    var Number: Int = 0
    var Type: Int = 0 // NEW or REPLY
    private val _extend_items = Vector<TelnetArticleItem>()
    var frame: TelnetFrame? = null
        private set
    private val _info = Vector<TelnetArticleItemInfo>()
    private val _items = Vector<TelnetArticleItem?>()
    private val _main_items = Vector<TelnetArticleItem>()
    private val _push = Vector<TelnetArticlePush?>()

    fun setFrameData(rows: Vector<TelnetRow?>) {
        this.frame = TelnetFrame(rows.size)
        for (i in rows.indices) {
            frame!!.setRow(i, rows.get(i)!!.clone())
        }
    }

    fun addMainItem(aItem: TelnetArticleItem?) {
        _main_items.add(aItem)
    }

    fun addExtendItem(aItem: TelnetArticleItem?) {
        _extend_items.add(aItem)
    }

    fun addInfo(aInfo: TelnetArticleItemInfo?) {
        _info.add(aInfo)
    }

    val infoSize: Int
        get() = _info.size

    fun getInfo(index: Int): TelnetArticleItemInfo? {
        return _info.get(index)
    }

    fun addPush(aPush: TelnetArticlePush?) {
        _push.add(aPush)
    }

    fun build() {
        for (main_item in _main_items) {
            main_item.build()
        }
        for (extend_item in _extend_items) {
            extend_item.build()
        }
        val remove_items = Vector<TelnetArticleItem?>()
        for (item in _main_items) {
            if (item.isEmpty()) {
                remove_items.add(item)
            }
        }
        for (removeItem in remove_items) {
            _main_items.remove(removeItem)
        }
        remove_items.clear()
        for (item2 in _extend_items) {
            if (item2.isEmpty()) {
                remove_items.add(item2)
            }
        }
        for (remove_item in remove_items) {
            _extend_items.remove(remove_item)
        }
        remove_items.clear()
        if (!_extend_items.isEmpty()) {
            _extend_items.lastElement().setType(1)
        }
        _items.clear()
        _items.addAll(_main_items)
        _items.addAll(_extend_items)
    }

    fun clear() {
        Title = ""
        this.author = ""
        BoardName = ""
        DateTime = ""
        _main_items.clear()
        _items.clear()
        _push.clear()
        _info.clear()
        this.frame = null
    }

    fun generateReplyTitle(): String {
        return "Re: " + Title
    }

    /** 設定文章標題  */
    fun generateEditFormat(): String {
        val content_buffer = StringBuilder()
        val time_string = frame!!.getRow(2).toString().substring(4)
        content_buffer.append("作者: ").append(this.author)
        if (Nickname != null && !Nickname!!.isEmpty()) {
            content_buffer.append("(").append(Nickname).append(")")
        }
        content_buffer.append(" 看板: ").append(BoardName).append("\n")
        content_buffer.append("標題: %s\n")
        content_buffer.append("時間: ").append(time_string).append("\n")
        content_buffer.append("\n%s")
        return content_buffer.toString()
    }

    fun generateEditTitle(): String {
        return frame!!.getRow(1).toString().substring(4)
    }

    /** 產生 修改用的文章內容
     * 有附上ASCII色碼  */
    fun generateEditContent(): String {
        val content_buffer = StringBuilder()
        var paintTextColor: Byte = TelnetAnsi.Companion.getDefaultTextColor()
        var paintBackColor: Byte = TelnetAnsi.Companion.getDefaultBackgroundColor()

        var startContentRowIndex = 0
        // 先找出指定行"時間", 下一行是分隔線, 再下一行是內容
        // 內容如果是空白"", 是發文時系統預設給的, 再往下找一行
        for (i in 0..<frame!!.rowSize) {
            val _row = frame!!.getRow(i)
            if (_row!!.rawString.contains("時間")) {
                startContentRowIndex = i + 2
                if (frame!!.getRow(startContentRowIndex)!!.rawString.isEmpty()) startContentRowIndex += 1
                break
            }
        }

        for (rowIndex in startContentRowIndex..<frame!!.rowSize) {
            val _row = frame!!.getRow(rowIndex)
            val rawString = _row!!.rawString
            if (rawString != null) {
                // 不用換顏色的內容
                if (rawString.matches("※ .*".toRegex()) || rawString.matches("> .*".toRegex()) || rawString.matches(
                        "--.*".toRegex()
                    )
                ) {
                    content_buffer.append(rawString).append("\n")
                } else {
                    // 換顏色
                    val ss = SpannableString(rawString)
                    val finalString = StringBuilder()
                    val textColor: ByteArray = _row.getTextColor()!!
                    val backColor = _row.getBackgroundColor()

                    // 檢查整串字元內有沒有包含預設顏色, 預設不用替換
                    var needReplaceForeColor = false
                    paintTextColor = TelnetAnsi.Companion.getDefaultTextColor()
                    paintBackColor = TelnetAnsi.Companion.getDefaultBackgroundColor()
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
                    content_buffer.append(finalString).append("\n")
                }
            }
        }


        return content_buffer.toString()
    }

    /** 產生 回應用的文章內容
     */
    fun generateReplyContent(): String {
        val maximum_quote: Int // 回應作者的階層, 0-父 1-祖父
        val content_builder = StringBuilder()
        val level_buffer: MutableSet<Int?> = HashSet<Int?>()
        level_buffer.add(0)
        for (telnetArticleItemInfo in _info) {
            level_buffer.add(telnetArticleItemInfo.quoteLevel)
        }
        val quote_level_list = level_buffer.toTypedArray<Int?>()
        Arrays.sort(quote_level_list)
        if (quote_level_list.size < 2) {
            maximum_quote = quote_level_list[quote_level_list.size - 1]!!
        } else {
            maximum_quote = quote_level_list[1]!!
        }

        // 第一個回覆作者(一定不會被黑名單block)
        if (maximum_quote > -1) content_builder.append(
            String.format(
                "※ 引述《%s (%s)》之銘言：\n",
                this.author, Nickname
            )
        )

        val blockListEnable = UserSettings.getPropertiesBlockListEnable()
        // 逐行上推作者
        for (info in _info) {
            if (!(blockListEnable && UserSettings.isBlockListContains(info.author)) && info.quoteLevel <= maximum_quote) {
                for (i in 0..<info.quoteLevel) {
                    content_builder.append("> ")
                }
                content_builder.append(
                    String.format(
                        "※ 引述《%s (%s)》之銘言：\n",
                        info.author,
                        info.nickname
                    )
                )
            }
        }
        // 作者內容
        for (item in _main_items) {
            if (!(blockListEnable && UserSettings.isBlockListContains(item.getAuthor())) && item.getQuoteLevel() <= maximum_quote) {
                val row_strings: Array<String?> =
                    item.getContent().split("\n".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                for (append in row_strings) {
                    for (j in 0..item.getQuoteLevel()) {
                        content_builder.append("> ")
                    }
                    content_builder.append(append)
                    content_builder.append("\n")
                }
            }
        }

        return content_builder.toString()
    }

    val itemSize: Int
        get() = _items.size

    fun getItem(index: Int): TelnetArticleItem? {
        if (index < 0 || index >= _items.size) {
            return null
        }
        return _items.get(index)
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
                builder.append(row!!.rawString)
            }
            return builder.toString()
        }

    val pushSize: Int
        get() = _push.size

    fun getPush(index: Int): TelnetArticlePush? {
        try {
            return _push.get(index)
        } catch (e: Exception) {
            return null
        }
    }

    companion object {
        const val MINIMUM_REMOVE_QUOTE: Int = 1
        const val NEW: Int = 0
        const val REPLY: Int = 1
    }
}
