package com.kota.telnet

import android.text.SpannableString
import com.kota.Bahamut.service.UserSettings
import com.kota.telnet.model.TelnetFrame
import com.kota.telnet.model.TelnetRow
import com.kota.telnet.reference.TelnetAnsiCode.getBackAsciiCode
import com.kota.telnet.reference.TelnetAnsiCode.getTextAsciiCode
import java.util.Arrays
import java.util.Vector

/**
 * 文章資料模型
 * 
 * 儲存從 Telnet 解析後的結構化文章資料，由 ArticleHandler 解析填充
 * 
 * 文章結構：
 * ┌─────────────────────────────────────────────────────┐
 * │ 標頭區（Header）                                      │
 * │   - title: 文章標題                                   │
 * │   - author: 作者 ID                                   │
 * │   - nickName: 作者暱稱                                │
 * │   - boardName: 看板名稱                               │
 * │   - dateTime: 發文時間                                │
 * ├─────────────────────────────────────────────────────┤
 * │ 主文區（Main Items）                                  │
 * │   - mainItems: 主文內容區塊（含引述）                  │
 * │   - infos: 引述作者資訊                               │
 * ├─────────────────────────────────────────────────────┤
 * │ 延伸區（Extend Items）                                │
 * │   - extendItems: "--" 分隔線後的額外內容              │
 * ├─────────────────────────────────────────────────────┤
 * │ 推文區（Pushes）                                      │
 * │   - pushes: 推/噓/→ 留言列表                         │
 * ├─────────────────────────────────────────────────────┤
 * │ 文章結尾                                              │
 * │   - fromIP: 發文來源 IP                               │
 * └─────────────────────────────────────────────────────┘
 */
class TelnetArticle {
    
    // ==================== 標頭欄位 ====================
    
    /** 文章標題（已移除 "Re: " 前綴） */
    @JvmField
    var title: String = ""
    
    /** 作者 ID */
    var author: String = ""
    
    /** 看板名稱 */
    @JvmField
    var boardName: String = ""
    
    /** 發文時間（格式如 "Mon Apr 29 12:00:00 2024"） */
    @JvmField
    var dateTime: String = ""
    
    /** 作者暱稱（括號內的顯示名稱） */
    @JvmField
    var nickName: String? = ""
    
    /** 發文來源 IP（從文章結尾 "※ Origin...From:" 解析） */
    @JvmField
    var fromIP: String = ""
    
    /** 
     * 文章編號
     * 在串接瀏覽模式下表示當前顯示的是第幾篇，不代表在版面上的實際編號
     */
    @JvmField
    var articleNumber: Int = 0
    
    /** 
     * 文章類型
     * @see NEW 新文章
     * @see REPLY 回覆文章（原標題有 "Re: " 前綴）
     */
    var articleType: Int = 0
    
    // ==================== 內容容器 ====================
    
    /** 
     * 延伸內容區塊
     * 儲存 "--" 分隔線之後、推文之前的內容（如簽名檔）
     */
    private val extendItems = Vector<TelnetArticleItem>()
    
    /** 
     * 原始 Telnet 畫面資料
     * 保留完整的 TelnetFrame 供後續處理（如產生回覆內容、複製全文）
     */
    var frame: TelnetFrame? = null
        private set
    
    /** 
     * 引述作者資訊列表
     * 記錄每層引述的作者與暱稱，用於產生回覆內容時標註來源
     */
    private val infos = Vector<TelnetArticleItemInfo>()
    
    /** 
     * 合併後的所有內容區塊（mainItems + extendItems）
     * 供 UI 顯示使用
     */
    private val items = Vector<TelnetArticleItem?>()
    
    /** 
     * 主文內容區塊
     * 儲存 "--" 分隔線之前的所有內容（含引述文字）
     */
    private val mainItems = Vector<TelnetArticleItem>()
    
    /** 
     * 推文列表
     * 儲存所有推/噓/→ 留言
     */
    private val pushes = Vector<TelnetArticlePush?>()
    
    /** 
     * 修改紀錄列表
     * 儲存所有 "※ 修改:..." 行的解析結果
     * 暫不輸出到畫面，僅供資料保存
     */
    private val editRecords = Vector<TelnetArticleEditRecord>()
    
    // ==================== 資料填充方法（由 ArticleHandler 呼叫） ====================

    /**
     * 設定原始畫面資料
     * 將解析後的行資料轉存為 TelnetFrame，供後續產生回覆/編輯內容使用
     */
    fun setFrameData(rows: Vector<TelnetRow>) {
        this.frame = TelnetFrame(rows.size)
        for (i in rows.indices) {
            frame?.setRow(i, rows[i].clone())
        }
    }

    /** 新增主文內容區塊（"--" 分隔線之前的內容） */
    fun addMainItem(aItem: TelnetArticleItem?) {
        mainItems.add(aItem)
    }

    /** 新增延伸內容區塊（"--" 分隔線之後的內容） */
    fun addExtendItem(aItem: TelnetArticleItem?) {
        extendItems.add(aItem)
    }

    /** 新增引述作者資訊（解析 "※ 引述《作者》之銘言" 時呼叫） */
    fun addInfo(aInfo: TelnetArticleItemInfo?) {
        infos.add(aInfo)
    }

    /** 取得引述資訊數量 */
    val infoSize: Int
        get() = infos.size

    /** 取得指定索引的引述資訊 */
    fun getInfo(index: Int): TelnetArticleItemInfo? {
        return infos[index]
    }

    /** 新增推文 */
    fun addPush(aPush: TelnetArticlePush?) {
        pushes.add(aPush)
    }
    
    /** 新增修改紀錄 */
    fun addEditRecord(aRecord: TelnetArticleEditRecord?) {
        editRecords.add(aRecord)
    }
    
    /** 取得修改紀錄數量 */
    val editRecordSize: Int
        get() = editRecords.size
    
    /** 取得指定索引的修改紀錄 */
    fun getEditRecord(index: Int): TelnetArticleEditRecord? {
        return try {
            editRecords[index]
        } catch (e: Exception) {
            null
        }
    }
    
    // ==================== 建構與清除 ====================

    /**
     * 建構最終文章結構
     * 
     * 處理流程：
     * 1. 建構每個內容區塊的顯示文字
     * 2. 移除空白區塊
     * 3. 標記最後一個延伸區塊的類型
     * 4. 合併 mainItems + extendItems 到 items 供 UI 顯示
     */
    fun build() {
        // 建構每個區塊的內容
        for (mainItem in mainItems) {
            mainItem.build()
        }
        for (extendItem in extendItems) {
            extendItem.build()
        }
        
        // 移除空白的主文區塊
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
        
        // 移除空白的延伸區塊
        for (item2 in extendItems) {
            if (item2.isEmpty) {
                removeItems.add(item2)
            }
        }
        for (removeItem in removeItems) {
            extendItems.remove(removeItem)
        }
        removeItems.clear()
        
        // 標記最後一個延伸區塊（用於 UI 顯示判斷）
        if (!extendItems.isEmpty()) {
            extendItems.lastElement().type = 1
        }
        
        // 合併所有區塊供顯示使用
        items.clear()
        items.addAll(mainItems)
        items.addAll(extendItems)
    }

    /** 清除所有資料，準備載入新文章 */
    fun clear() {
        title = ""
        this.author = ""
        boardName = ""
        dateTime = ""
        mainItems.clear()
        items.clear()
        pushes.clear()
        infos.clear()
        editRecords.clear()
        this.frame = null
    }
    
    // ==================== 內容產生方法 ====================

    /** 產生回覆用標題（加上 "Re: " 前綴） */
    fun generateReplyTitle(): String {
        return "Re: $title"
    }

    /**
     * 產生編輯用的文章格式範本
     * 
     * 輸出格式：
     * ```
     * 作者: xxx(暱稱) 看板: BoardName
     * 標題: %s          ← 標題佔位符
     * 時間: Mon Apr 29 12:00:00 2024
     * 
     * %s                ← 內容佔位符
     * ```
     * 
     * @return 包含 %s 佔位符的格式字串，供 String.format() 使用
     */
    fun generateEditFormat(): String {
        val contentBuffer = StringBuilder()
        // 從 Row 2 取得時間，跳過前 4 個字元（"時間" 標籤）
        val timeString = frame?.getRow(2).toString().substring(4)
        
        contentBuffer.append("作者: ").append(this.author)
        if (nickName != null && !nickName!!.isEmpty()) {
            contentBuffer.append("(").append(nickName).append(")")
        }
        contentBuffer.append(" 看板: ").append(boardName).append("\n")
        contentBuffer.append("標題: %s\n")  // 標題佔位符
        contentBuffer.append("時間: ").append(timeString).append("\n")
        contentBuffer.append("\n%s")  // 內容佔位符
        return contentBuffer.toString()
    }

    /** 
     * 取得原始標題（從 Row 1 擷取，跳過 "標題" 標籤）
     * 用於編輯文章時保留原標題
     */
    fun generateEditTitle(): String {
        return frame!!.getRow(1).toString().substring(4)
    }

    /**
     * 產生編輯用的文章內容（保留 ANSI 色碼）
     * 
     * 此方法會：
     * 1. 找到內容起始行（"時間" 行之後第 2 行）
     * 2. 保留引述標記行（"※ "、"> "、"--"）的原始格式
     * 3. 將其他行的顏色轉換為 ANSI 跳脫序列（*[1;32m 格式）
     * 
     * ANSI 色碼格式說明：
     * - *[m       : 重置為預設顏色
     * - *[1;32m   : 亮綠色（1=亮色, 32=綠色前景）
     * - *[1;32;44m: 亮綠色文字 + 藍色背景
     * 
     * @return 包含 ANSI 色碼的文章內容
     */
    fun generateEditContent(): String {
        val contentBuffer = StringBuilder()
        var paintTextColor: Byte   // 當前繪製的前景色
        var paintBackColor: Byte   // 當前繪製的背景色

        // ========== 找出內容起始行 ==========
        // 結構: "時間" 行 → 分隔線 → 內容起始
        // 如果內容起始行是空白（系統預設），再往下一行
        var startContentRowIndex = 0
        for (i in 0..<frame!!.rowSize) {
            val currentRow = frame!!.getRow(i)
            if (currentRow.rawString.contains("時間")) {
                startContentRowIndex = i + 2  // 跳過分隔線
                // 若該行為空，往下找第一個非空行
                if (frame!!.getRow(startContentRowIndex).rawString.isEmpty()) startContentRowIndex += 1
                break
            }
        }

        // ========== 逐行處理內容 ==========
        for (rowIndex in startContentRowIndex..<frame!!.rowSize) {
            val currentRow = frame!!.getRow(rowIndex)
            val rawString = currentRow.rawString
            
            // ---------- 特殊行：保持原樣輸出 ----------
            // "※ " 開頭：系統訊息（引述標記、修改紀錄等）
            // "> " 開頭：引述內容
            // "--" 開頭：分隔線
            if (rawString.matches("※ .*".toRegex()) || rawString.matches("> .*".toRegex()) || rawString.matches(
                    "--.*".toRegex()
                )
            ) {
                contentBuffer.append(rawString).append("\n")
            } else {
                // ---------- 一般行：處理顏色轉換 ----------
                val ss = SpannableString(rawString)
                val finalString = StringBuilder()
                val textColor: ByteArray = currentRow.getTextColorArray()!!  // 每個字元的前景色
                val backColor = currentRow.getBackgroundColor()              // 每個字元的背景色

                // 檢查是否需要顏色替換（若整行都是預設色，則跳過處理）
                var needReplaceForeColor = false
                paintTextColor = TelnetAnsi.DEFAULT_TEXT_COLOR      // 7 (白色)
                paintBackColor = TelnetAnsi.DEFAULT_BACKGROUND_COLOR // 0 (黑色)
                for (i in textColor.indices) {
                    if (textColor[i] != paintTextColor || backColor!![i] != paintBackColor) {
                        if ((i + 1) <= rawString.length) {
                            needReplaceForeColor = true
                        }
                        break
                    }
                }

                if (needReplaceForeColor) {
                    // ---------- 逐字元處理顏色變換 ----------
                    var isBlink = false  // 是否為亮色模式（色碼 >= 8）
                    for (i in 0..<ss.length) {
                        finalString.append(ss.get(i))

                        // 檢測顏色變化，需要插入 ANSI 跳脫序列
                        if (textColor[i] != paintTextColor || backColor!![i] != paintBackColor) {
                            var appendString = "*["  // ANSI 跳脫序列開頭

                            // 處理亮色標記（ANSI 碼 1 = 亮色/粗體）
                            // 前景色 >= 8 表示亮色版本（如 8=亮黑, 9=亮紅...）
                            if (!isBlink && textColor[i] >= 8) {
                                isBlink = true
                                appendString += "1;"  // 啟用亮色
                            } else if (isBlink && textColor[i] < 8) {
                                isBlink = false
                                appendString += ";"   // 關閉亮色
                            }

                            // 計算基礎前景色（去除亮色偏移）
                            // 色碼對應: 0=黑 1=紅 2=綠 3=黃 4=藍 5=紫 6=青 7=白
                            var noBlinkTextColor = textColor[i].toInt()
                            if (noBlinkTextColor >= 8) noBlinkTextColor = noBlinkTextColor - 8
                            
                            // 舊的前景色（去除亮色偏移）
                            var preBlinkTextColor = paintTextColor.toInt()
                            if (preBlinkTextColor >= 8) preBlinkTextColor =
                                preBlinkTextColor - 8

                            // 組合 ANSI 色碼
                            if (noBlinkTextColor != preBlinkTextColor) { // 前景色變化
                                appendString += getTextAsciiCode(noBlinkTextColor)  // 30-37

                                if (backColor!![i] != paintBackColor) { // 背景色也變化
                                    appendString += ";" + getBackAsciiCode(backColor[i])  // 40-47
                                }
                            } else if (backColor!![i] != paintBackColor) { // 只有背景色變化
                                appendString += getBackAsciiCode(backColor[i])
                            }
                            appendString += "m"  // ANSI 跳脫序列結尾
                            
                            // 將色碼插入到字元之前
                            finalString.insert(finalString.length - 1, appendString)

                            // 更新當前繪製顏色狀態
                            paintTextColor = textColor[i]
                            paintBackColor = backColor[i]
                        }
                    }
                    // 行尾加上重置色碼，避免影響下一行
                    finalString.append("*[m")
                } else {
                    // 整行都是預設色，直接輸出原文
                    finalString.append(rawString)
                }
                contentBuffer.append(finalString).append("\n")
            }
        }


        return contentBuffer.toString()
    }

    /**
     * 產生回覆用的文章內容（自動加上引述標記）
     * 
     * 此方法會：
     * 1. 決定最大引述層級（避免過度巢狀）
     * 2. 加上 "※ 引述《作者》之銘言：" 標記
     * 3. 每行前加上 "> " 引述符號
     * 4. 自動過濾黑名單用戶的引述
     * 
     * @return 格式化的引述內容
     */
    fun generateReplyContent(): String {
        val contentBuilder = StringBuilder()
        val levelBuffer: MutableSet<Int?> = HashSet()
        levelBuffer.add(0)
        
        // 收集所有引述層級
        for (telnetArticleItemInfo in infos) {
            levelBuffer.add(telnetArticleItemInfo.quoteLevel)
        }
        val quoteLevelList = levelBuffer.toTypedArray<Int?>()
        Arrays.sort(quoteLevelList)
        
        // 決定最大引述層級（最多引述到第二層，避免過度巢狀）
        // 0=原作者, 1=第一層引述的作者
        val maximumQuote: Int = if (quoteLevelList.size < 2) {
            quoteLevelList[quoteLevelList.size - 1]!!
        } else {
            quoteLevelList[1]!!
        }

        // ========== 加入原作者引述標記 ==========
        // 原作者一定會顯示（不受黑名單影響）
        if (maximumQuote > -1) contentBuilder.append(
            String.format(
                "※ 引述《%s (%s)》之銘言：\n",
                this.author, nickName
            )
        )

        val blockListEnable = UserSettings.propertiesBlockListEnable
        
        // ========== 加入各層引述作者標記 ==========
        // 遍歷每層引述的作者資訊
        for (info in infos) {
            // 檢查黑名單與層級限制
            if (!(blockListEnable && UserSettings.isBlockListContains(info.author)) && info.quoteLevel <= maximumQuote) {
                // 根據引述層級加上對應數量的 "> "
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
        
        // ========== 加入引述內容 ==========
        // 遍歷每個內容區塊
        for (item in mainItems) {
            // 檢查黑名單與層級限制
            if (!(blockListEnable && UserSettings.isBlockListContains(item.author)) && item.quoteLevel <= maximumQuote) {
                // 將內容拆成多行處理
                val rowStrings: Array<String?> =
                    item.content.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                for (append in rowStrings) {
                    // 加上引述符號（層級+1，因為這是回覆內容）
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
    
    // ==================== 存取方法 ====================

    /** 取得內容區塊數量（主文 + 延伸） */
    val itemSize: Int
        get() = items.size

    /** 取得指定索引的內容區塊 */
    fun getItem(index: Int): TelnetArticleItem? {
        if (index < 0 || index >= items.size) {
            return null
        }
        return items[index]
    }

    /** 
     * 取得文章全文（純文字，不含色碼）
     * 用於複製全文功能
     */
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

    /** 取得推文數量 */
    val pushSize: Int
        get() = pushes.size

    /** 取得指定索引的推文 */
    fun getPush(index: Int): TelnetArticlePush? {
        return try {
            pushes[index]
        } catch (e: Exception) {
            null
        }
    }
    
    // ==================== 常數定義 ====================

    companion object {
        /** 最小移除引述層級（用於回覆時的引述處理） */
        const val MINIMUM_REMOVE_QUOTE: Int = 1
        
        /** 文章類型：新文章 */
        const val NEW: Int = 0
        
        /** 文章類型：回覆文章（標題原有 "Re: " 前綴） */
        const val REPLY: Int = 1
    }
}
