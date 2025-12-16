# Bahamut/dialogs - 業務對話框

**applyto**: `app/src/main/java/com/kota/Bahamut/dialogs/**/*.kt`

## 📋 模組概述

dialogs 模組提供所有 BBS 業務相關的對話框，包含發文、推文、搜尋、選色、圖片上傳等功能。基於 asFramework 對話框系統構建。

**技術棧**: Kotlin, Android Dialog  
**設計模式**: 委派模式 (Delegate), 監聽器模式  
**命名前綴**: Dialog

---

## 📂 主要對話框

### 1️⃣ 文章操作對話框

#### `DialogPostArticle.kt` - 發文對話框
```kotlin
class DialogPostArticle : ASDialog() {
    
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var signSpinner: Spinner
    
    var listener: DialogPostArticleListener? = null
    
    fun show(boardName: String) {
        // 設定對話框內容
        titleEditText.hint = "請輸入標題"
        contentEditText.hint = "請輸入內容"
        
        confirmButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val content = contentEditText.text.toString()
            val signIndex = signSpinner.selectedItemPosition
            
            listener?.onPostArticle(title, content, signIndex)
            dismiss()
        }
        
        super.show()
    }
}

interface DialogPostArticleListener {
    fun onPostArticle(title: String, content: String, signIndex: Int)
}
```

#### `DialogPushArticle.kt` - 推文對話框
```kotlin
class DialogPushArticle : ASDialog() {
    
    var pushType: Int = PUSH_TYPE_THUMB_UP  // 1=推, 2=噓, 3=→
    
    companion object {
        const val PUSH_TYPE_THUMB_UP = 1
        const val PUSH_TYPE_THUMB_DOWN = 2
        const val PUSH_TYPE_COMMENT = 3
    }
}
```

#### `DialogSearchArticle.kt` - 搜尋文章對話框
```kotlin
class DialogSearchArticle : ASDialog() {
    
    private lateinit var keywordEditText: EditText
    private lateinit var searchTypeRadioGroup: RadioGroup
    
    var listener: DialogSearchArticleListener? = null
    
    fun show() {
        searchButton.setOnClickListener {
            val keyword = keywordEditText.text.toString()
            val searchType = when (searchTypeRadioGroup.checkedRadioButtonId) {
                R.id.radio_title -> SEARCH_BY_TITLE
                R.id.radio_author -> SEARCH_BY_AUTHOR
                R.id.radio_content -> SEARCH_BY_CONTENT
                else -> SEARCH_BY_TITLE
            }
            
            listener?.onSearch(keyword, searchType)
            dismiss()
        }
        
        super.show()
    }
    
    companion object {
        const val SEARCH_BY_TITLE = 1
        const val SEARCH_BY_AUTHOR = 2
        const val SEARCH_BY_CONTENT = 3
    }
}

interface DialogSearchArticleListener {
    fun onSearch(keyword: String, searchType: Int)
}
```

---

### 2️⃣ 編輯工具對話框

#### `DialogInsertExpression.kt` - 插入表情符號
```kotlin
class DialogInsertExpression : ASDialog() {
    
    private val expressions = listOf(
        "○", "●", "◎", "◇", "◆", "□", "■", "△", "▲", "▽", "▼",
        "☆", "★", "♂", "♀", "♠", "♣", "♥", "♦", "♪", "♫"
    )
    
    var listener: DialogInsertExpressionListener? = null
    
    fun show() {
        // 建立 Grid 佈局顯示表情
        gridView.adapter = ExpressionAdapter(expressions)
        gridView.setOnItemClickListener { _, _, position, _ ->
            listener?.onExpressionSelected(expressions[position])
            dismiss()
        }
        
        super.show()
    }
}

interface DialogInsertExpressionListener {
    fun onExpressionSelected(expression: String)
}
```

#### `DialogSelectSign.kt` - 選擇簽名檔
```kotlin
class DialogSelectSign : ASDialog() {
    
    private val signs = mutableListOf<String>()
    
    var listener: DialogSelectSignListener? = null
    
    fun show(signList: List<String>) {
        signs.clear()
        signs.addAll(signList)
        
        listView.adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, signs)
        listView.setOnItemClickListener { _, _, position, _ ->
            listener?.onSignSelected(position, signs[position])
            dismiss()
        }
        
        super.show()
    }
}

interface DialogSelectSignListener {
    fun onSignSelected(index: Int, signContent: String)
}
```

---

### 3️⃣ 色彩工具對話框

#### `DialogColorPicker.kt` - 選色器
```kotlin
class DialogColorPicker : ASDialog() {
    
    private val colors = intArrayOf(
        Color.BLACK, Color.RED, Color.GREEN, Color.YELLOW,
        Color.BLUE, Color.MAGENTA, Color.CYAN, Color.WHITE
    )
    
    var listener: DialogColorPickerListener? = null
    
    fun show() {
        // 建立色塊 Grid
        gridView.adapter = ColorAdapter(colors)
        gridView.setOnItemClickListener { _, _, position, _ ->
            listener?.onColorSelected(colors[position])
            dismiss()
        }
        
        super.show()
    }
}

interface DialogColorPickerListener {
    fun onColorSelected(color: Int)
}
```

---

### 4️⃣ 圖片和網址對話框

#### `DialogShortenUrl.kt` - 縮網址
```kotlin
class DialogShortenUrl : ASDialog() {
    
    private lateinit var urlEditText: EditText
    private lateinit var resultTextView: TextView
    
    var listener: DialogShortenUrlListener? = null
    
    fun show() {
        shortenButton.setOnClickListener {
            val originalUrl = urlEditText.text.toString()
            
            ASProcessingDialog.showProcessingDialog("縮網址中...")
            
            ASCoroutine.runInNewCoroutine {
                val shortUrl = ShortenUrlService.shorten(originalUrl)
                
                object : ASRunner() {
                    override fun run() {
                        ASProcessingDialog.dismissProcessingDialog()
                        resultTextView.text = shortUrl
                        listener?.onUrlShortened(originalUrl, shortUrl)
                    }
                }.runInMainThread()
            }
        }
        
        super.show()
    }
}

interface DialogShortenUrlListener {
    fun onUrlShortened(originalUrl: String, shortUrl: String)
}
```

#### 圖片上傳子模組 (`uploadImgMethod/`)

```kotlin
// LitterCatBox 上傳器
class UploaderLitterCatBox {
    
    suspend fun upload(imageFile: File): String {
        // 上傳到 LitterCatBox.com
        val client = OkHttpClient()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", imageFile.name,
                imageFile.asRequestBody("image/*".toMediaTypeOrNull()))
            .build()
        
        val request = Request.Builder()
            .url("https://litterbox.catbox.moe/resources/internals/api.php")
            .post(requestBody)
            .build()
        
        val response = client.newCall(request).execute()
        return response.body?.string() ?: throw Exception("上傳失敗")
    }
}

// Postimage.org 上傳器
class UploaderPostimageorg {
    
    suspend fun upload(imageFile: File): String {
        // 上傳到 Postimage.org
        // 實作類似上面
    }
}
```

---

### 5️⃣ 看板搜尋對話框

#### `DialogSearchBoard.kt` - 搜尋看板
```kotlin
class DialogSearchBoard : ASDialog() {
    
    private lateinit var searchEditText: EditText
    private lateinit var resultListView: ListView
    
    private val results = mutableListOf<BoardSearchResult>()
    
    var listener: DialogSearchBoardListener? = null
    
    fun show() {
        searchButton.setOnClickListener {
            val keyword = searchEditText.text.toString()
            searchBoards(keyword)
        }
        
        resultListView.setOnItemClickListener { _, _, position, _ ->
            listener?.onBoardSelected(results[position])
            dismiss()
        }
        
        super.show()
    }
    
    private fun searchBoards(keyword: String) {
        ASProcessingDialog.showProcessingDialog("搜尋中...")
        
        ASCoroutine.runInNewCoroutine {
            val searchResults = BBS.searchBoards(keyword)
            
            object : ASRunner() {
                override fun run() {
                    ASProcessingDialog.dismissProcessingDialog()
                    results.clear()
                    results.addAll(searchResults)
                    adapter.notifyDataSetChanged()
                }
            }.runInMainThread()
        }
    }
}

interface DialogSearchBoardListener {
    fun onBoardSelected(board: BoardSearchResult)
}
```

---

## 🎯 使用範例

### 發文流程

```kotlin
class BoardMainPage : TelnetListPage() {
    
    fun showPostDialog() {
        val dialog = DialogPostArticle()
        dialog.listener = object : DialogPostArticleListener {
            override fun onPostArticle(title: String, content: String, signIndex: Int) {
                submitArticle(title, content, signIndex)
            }
        }
        dialog.show(currentBoardName)
    }
    
    private fun submitArticle(title: String, content: String, signIndex: Int) {
        val command = BahamutCommandPostArticle(title, content, signIndex)
        pushCommand(command)
    }
}
```

### 推文流程

```kotlin
fun showPushDialog() {
    val dialog = DialogPushArticle()
    dialog.pushType = DialogPushArticle.PUSH_TYPE_THUMB_UP
    
    dialog.listener = object : DialogPushArticleListener {
        override fun onPush(content: String) {
            val command = BahamutCommandPushArticle(dialog.pushType, content)
            pushCommand(command)
        }
    }
    
    dialog.show()
}
```

---

## 📚 相關模組

- [asFramework-dialog](asFramework-dialog.md) - 基礎對話框系統
- [Bahamut-command](Bahamut-command.md) - 執行 BBS 命令
- [Bahamut-pages](Bahamut-pages.md) - 呼叫這些對話框

---

## 📝 技術特點總結

1. **業務邏輯封裝**: 將複雜的 BBS 操作封裝為對話框
2. **監聽器模式**: 使用介面回呼解耦
3. **非同步操作**: 整合 ASRunner/ASCoroutine
4. **多圖床支援**: 支援多種圖片上傳服務
5. **使用者友善**: 提供直觀的操作介面
