# telnet/logic - Telnet 業務邏輯處理

**applyto**: `app/src/main/java/com/kota/telnet/logic/**/*.kt`

## 📋 模組概述

logic 模組提供 Telnet 相關的業務邏輯處理,包含文章解析、看板搜尋、分類模式等功能。

**技術棧**: Kotlin, String Processing  
**設計模式**: Handler Pattern

---

## 📂 主要元件

### 1️⃣ `ArticleHandler.kt` - 文章處理器

```kotlin
object ArticleHandler {
    
    /**
     * 解析文章內容
     */
    fun parseArticle(lines: List<String>): Article {
        val article = Article()
        
        // 解析標題
        article.title = lines.firstOrNull { it.contains("標題") }
            ?.substringAfter("標題:")
            ?.trim() ?: ""
        
        // 解析作者
        article.author = lines.firstOrNull { it.contains("作者") }
            ?.substringAfter("作者:")
            ?.substringBefore("(")
            ?.trim() ?: ""
        
        // 解析內容
        val contentStartIndex = lines.indexOfFirst { it.contains("─────") } + 1
        article.content = lines.subList(contentStartIndex, lines.size)
            .joinToString("\n")
        
        return article
    }
    
    /**
     * 解析推文
     */
    fun parsePushes(content: String): List<Push> {
        val pushes = mutableListOf<Push>()
        val pushPattern = Regex("^(推|噓|→) ([^:]+): (.+?) (\\d{2}/\\d{2} \\d{2}:\\d{2})$")
        
        content.lines().forEach { line ->
            pushPattern.find(line)?.let { match ->
                val push = Push(
                    type = when (match.groupValues[1]) {
                        "推" -> PushType.THUMB_UP
                        "噓" -> PushType.THUMB_DOWN
                        else -> PushType.COMMENT
                    },
                    author = match.groupValues[2],
                    content = match.groupValues[3],
                    time = match.groupValues[4]
                )
                pushes.add(push)
            }
        }
        
        return pushes
    }
}

data class Article(
    var title: String = "",
    var author: String = "",
    var date: String = "",
    var content: String = ""
)

data class Push(
    var type: PushType,
    var author: String,
    var content: String,
    var time: String
)

enum class PushType {
    THUMB_UP,    // 推
    THUMB_DOWN,  // 噓
    COMMENT      // →
}
```

---

### 2️⃣ `SearchBoardHandler.kt` - 看板搜尋處理器

```kotlin
object SearchBoardHandler {
    
    fun searchBoards(keyword: String, allBoards: List<String>): List<String> {
        return allBoards.filter { board ->
            board.contains(keyword, ignoreCase = true)
        }
    }
    
    fun parseBoardList(lines: List<String>): List<BoardInfo> {
        val boards = mutableListOf<BoardInfo>()
        
        lines.forEach { line ->
            val parts = line.split(Regex("\\s+"))
            if (parts.size >= 3) {
                boards.add(BoardInfo(
                    name = parts[0],
                    category = parts[1],
                    title = parts.drop(2).joinToString(" ")
                ))
            }
        }
        
        return boards
    }
}

data class BoardInfo(
    var name: String,
    var category: String,
    var title: String
)
```

---

### 3️⃣ `ClassMode.kt` - 分類模式

```kotlin
enum class ClassMode {
    NORMAL,      // 一般模式
    HIERARCHY,   // 階層模式
    FLAT         // 平面模式
}

data class ClassStep(
    var name: String,
    var title: String,
    var depth: Int
)
```

---

### 4️⃣ `ItemUtils.kt` - 項目工具

```kotlin
object ItemUtils {
    
    /**
     * 解析文章列表項目
     */
    fun parseArticleItem(line: String): ArticleItem? {
        // 格式: "1234 5 作者名稱 12/25 標題"
        val pattern = Regex("^(\\d+)\\s+(\\S+)\\s+(\\S+)\\s+(\\d{2}/\\d{2})\\s+(.+)$")
        val match = pattern.find(line) ?: return null
        
        return ArticleItem(
            index = match.groupValues[1].toInt(),
            pushCount = match.groupValues[2],
            author = match.groupValues[3],
            date = match.groupValues[4],
            title = match.groupValues[5]
        )
    }
    
    /**
     * 識別項目類型
     */
    fun identifyItemType(line: String): ItemType {
        return when {
            line.contains("[公告]") -> ItemType.ANNOUNCEMENT
            line.contains("[活動]") -> ItemType.ACTIVITY
            line.contains("[情報]") -> ItemType.INFO
            line.startsWith("R:") -> ItemType.REPLY
            line.startsWith("轉") -> ItemType.FORWARD
            else -> ItemType.NORMAL
        }
    }
}

data class ArticleItem(
    var index: Int,
    var pushCount: String,
    var author: String,
    var date: String,
    var title: String
)

enum class ItemType {
    NORMAL,         // 一般文章
    ANNOUNCEMENT,   // 公告
    ACTIVITY,       // 活動
    INFO,           // 情報
    REPLY,          // 回覆
    FORWARD         // 轉錄
}
```

---

## 🎯 使用範例

### 解析文章

```kotlin
class ArticlePage : TelnetPage() {
    
    fun displayArticle(lines: List<String>) {
        val article = ArticleHandler.parseArticle(lines)
        
        titleTextView.text = article.title
        authorTextView.text = article.author
        contentTextView.text = article.content
        
        // 解析推文
        val pushes = ArticleHandler.parsePushes(article.content)
        displayPushes(pushes)
    }
}
```

### 解析列表項目

```kotlin
class BoardMainPage : TelnetListPage() {
    
    override fun loadPage(): TelnetListPageBlock? {
        val block = TelnetListPageBlock.create()
        
        val lines = TelnetModel.getScreenLines()
        lines.forEachIndexed { index, line ->
            val item = ItemUtils.parseArticleItem(line)
            item?.let {
                block.setItem(index, BoardPageItem().apply {
                    this.itemIndex = it.index
                    this.title = it.title
                    this.author = it.author
                    this.date = it.date
                    this.pushCount = it.pushCount
                })
            }
        }
        
        return block
    }
}
```

---

## 📚 相關模組

- [telnet-model](telnet-model.md) - Telnet 資料模型
- [Bahamut-command](Bahamut-command.md) - BBS 命令
- [Bahamut-listPage](Bahamut-listPage.md) - 列表頁面

---

## 📝 技術特點總結

1. **字串解析**: 正則表達式處理 BBS 格式
2. **資料轉換**: 文字轉結構化資料
3. **類型識別**: 自動識別文章類型
4. **模式識別**: 解析 ANSI 格式
