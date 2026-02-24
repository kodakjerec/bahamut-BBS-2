# Bahamut/dataModels - 資料模型與本地儲存

**applyto**: `app/src/main/java/com/kota/Bahamut/dataModels/**/*.kt`

## 📋 模組概述

dataModels 模組提供資料模型定義和本地儲存功能，包含書籤、文章暫存、縮網址等資料的持久化管理。

**技術棧**: Kotlin, SharedPreferences, JSON, SQLite  
**設計模式**: Data Model, Repository Pattern  
**命名前綴**: 無統一前綴

---

## 📂 主要元件

### 1️⃣ 書籤系統

#### `Bookmark.kt` - 書籤資料模型
```kotlin
data class Bookmark(
    var name: String = "",           // 看板名稱
    var title: String = "",          // 看板標題
    var type: Int = TYPE_BOARD,      // 類型：看板/分類/連結
    var url: String = "",            // 網址（連結類型使用）
    var category: String = ""        // 分類
) {
    companion object {
        const val TYPE_BOARD = 1
        const val TYPE_CATEGORY = 2
        const val TYPE_LINK = 3
    }
}
```

#### `BookmarkList.kt` - 書籤列表
```kotlin
class BookmarkList : ArrayList<Bookmark>() {
    fun addBookmark(bookmark: Bookmark) {
        if (!contains(bookmark)) {
            add(bookmark)
        }
    }
    
    fun removeBookmark(name: String) {
        removeAll { it.name == name }
    }
    
    fun findBookmark(name: String): Bookmark? {
        return find { it.name == name }
    }
}
```

#### `BookmarkStore.kt` - 書籤儲存管理器
```kotlin
object BookmarkStore {
    private const val PREF_NAME = "bookmarks"
    private const val KEY_BOOKMARKS = "bookmark_list"
    
    fun save(context: Context, bookmarks: BookmarkList) {
        val json = Gson().toJson(bookmarks)
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_BOOKMARKS, json)
            .apply()
    }
    
    fun load(context: Context): BookmarkList {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_BOOKMARKS, "[]")
        val type = object : TypeToken<BookmarkList>() {}.type
        return Gson().fromJson(json, type) ?: BookmarkList()
    }
}
```

**使用範例**:
```kotlin
// 新增書籤
val bookmark = Bookmark(
    name = "C_Chat",
    title = "C洽",
    type = Bookmark.TYPE_BOARD
)
val bookmarks = BookmarkStore.load(context)
bookmarks.addBookmark(bookmark)
BookmarkStore.save(context, bookmarks)

// 移除書籤
bookmarks.removeBookmark("C_Chat")
BookmarkStore.save(context, bookmarks)
```

---

### 2️⃣ 文章暫存

#### `ArticleTemp.kt` - 暫存文章
```kotlin
data class ArticleTemp(
    var id: String = "",              // 暫存 ID
    var boardName: String = "",       // 看板名稱
    var title: String = "",           // 標題
    var content: String = "",         // 內容
    var signIndex: Int = 0,           // 簽名檔索引
    var createTime: Long = 0L         // 建立時間
)
```

#### `ArticleTempStore.kt` - 文章暫存管理器
```kotlin
object ArticleTempStore {
    
    fun save(context: Context, temp: ArticleTemp) {
        val json = Gson().toJson(temp)
        context.getSharedPreferences("article_temp", Context.MODE_PRIVATE)
            .edit()
            .putString(temp.id, json)
            .apply()
    }
    
    fun load(context: Context, id: String): ArticleTemp? {
        val json = context.getSharedPreferences("article_temp", Context.MODE_PRIVATE)
            .getString(id, null)
        return json?.let { Gson().fromJson(it, ArticleTemp::class.java) }
    }
    
    fun delete(context: Context, id: String) {
        context.getSharedPreferences("article_temp", Context.MODE_PRIVATE)
            .edit()
            .remove(id)
            .apply()
    }
    
    fun loadAll(context: Context): List<ArticleTemp> {
        val prefs = context.getSharedPreferences("article_temp", Context.MODE_PRIVATE)
        return prefs.all.mapNotNull { (_, value) ->
            Gson().fromJson(value as? String, ArticleTemp::class.java)
        }
    }
}
```

---

### 3️⃣ 縮網址

#### `ShortenUrl.kt` - 縮網址資料模型
```kotlin
data class ShortenUrl(
    var originalUrl: String = "",     // 原始網址
    var shortUrl: String = "",        // 短網址
    var createTime: Long = 0L,        // 建立時間
    var service: String = ""          // 服務名稱 (e.g., "ppt.cc")
)
```

#### `UrlDatabase.kt` - URL 資料庫
```kotlin
class UrlDatabase(context: Context) {
    
    private val db: SQLiteDatabase
    
    init {
        val helper = UrlDatabaseHelper(context)
        db = helper.writableDatabase
    }
    
    fun insertUrl(url: ShortenUrl) {
        val values = ContentValues().apply {
            put("original_url", url.originalUrl)
            put("short_url", url.shortUrl)
            put("create_time", url.createTime)
            put("service", url.service)
        }
        db.insert("urls", null, values)
    }
    
    fun findByOriginal(originalUrl: String): ShortenUrl? {
        val cursor = db.query(
            "urls",
            null,
            "original_url = ?",
            arrayOf(originalUrl),
            null, null, null
        )
        
        return if (cursor.moveToFirst()) {
            ShortenUrl(
                originalUrl = cursor.getString(cursor.getColumnIndex("original_url")),
                shortUrl = cursor.getString(cursor.getColumnIndex("short_url")),
                createTime = cursor.getLong(cursor.getColumnIndex("create_time")),
                service = cursor.getString(cursor.getColumnIndex("service"))
            ).also { cursor.close() }
        } else {
            cursor.close()
            null
        }
    }
}
```

---

### 4️⃣ 其他模型

#### `ReferenceAuthor.kt` - 引用作者
```kotlin
data class ReferenceAuthor(
    var author: String = "",
    var date: String = ""
)
```

---

## 🎯 使用場景

### 1. 書籤管理

```kotlin
class BookmarkManager(private val context: Context) {
    
    private val bookmarks = BookmarkStore.load(context)
    
    fun addBoard(boardName: String, boardTitle: String) {
        val bookmark = Bookmark(
            name = boardName,
            title = boardTitle,
            type = Bookmark.TYPE_BOARD
        )
        bookmarks.addBookmark(bookmark)
        BookmarkStore.save(context, bookmarks)
    }
    
    fun removeBoard(boardName: String) {
        bookmarks.removeBookmark(boardName)
        BookmarkStore.save(context, bookmarks)
    }
    
    fun isBookmarked(boardName: String): Boolean {
        return bookmarks.any { it.name == boardName }
    }
    
    fun getAllBookmarks(): List<Bookmark> {
        return bookmarks.toList()
    }
}
```

### 2. 草稿自動儲存

```kotlin
class PostArticlePage : ASViewController() {
    
    private var draftId = UUID.randomUUID().toString()
    
    override fun onPageWillDisappear() {
        super.onPageWillDisappear()
        saveDraft()
    }
    
    private fun saveDraft() {
        val temp = ArticleTemp(
            id = draftId,
            boardName = currentBoard,
            title = titleEditText.text.toString(),
            content = contentEditText.text.toString(),
            signIndex = selectedSignIndex,
            createTime = System.currentTimeMillis()
        )
        ArticleTempStore.save(context, temp)
    }
    
    private fun loadDraft() {
        val temp = ArticleTempStore.load(context, draftId)
        temp?.let {
            titleEditText.setText(it.title)
            contentEditText.setText(it.content)
            selectedSignIndex = it.signIndex
        }
    }
    
    private fun deleteDraft() {
        ArticleTempStore.delete(context, draftId)
    }
}
```

### 3. 縮網址快取

```kotlin
class ShortenUrlManager(context: Context) {
    
    private val urlDb = UrlDatabase(context)
    
    suspend fun shortenUrl(originalUrl: String): String {
        // 檢查快取
        val cached = urlDb.findByOriginal(originalUrl)
        if (cached != null) {
            return cached.shortUrl
        }
        
        // 呼叫 API 縮網址
        val shortUrl = callShortenApi(originalUrl)
        
        // 儲存到資料庫
        val record = ShortenUrl(
            originalUrl = originalUrl,
            shortUrl = shortUrl,
            createTime = System.currentTimeMillis(),
            service = "ppt.cc"
        )
        urlDb.insertUrl(record)
        
        return shortUrl
    }
}
```

---

## 📚 相關模組

- [Bahamut-pages](Bahamut-pages.md) - 使用這些資料模型
- [Bahamut-service](Bahamut-service.md) - UserSettings 設定管理

---

## 📝 技術特點總結

1. **資料持久化**: SharedPreferences + SQLite
2. **JSON 序列化**: 使用 Gson 處理複雜物件
3. **快取機制**: 減少網路請求
4. **自動儲存**: 草稿自動暫存
5. **Repository 模式**: 統一的資料存取介面
