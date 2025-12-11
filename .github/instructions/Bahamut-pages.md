# Bahamut/pages - 業務頁面集合

**applyto**: `app/src/main/java/com/kota/Bahamut/pages/**/*.kt`, `app/src/main/java/com/kota/Bahamut/pages/**/*.java`

## 📋 模組概述

pages 是最大的業務模組，包含所有 BBS 功能頁面的實作。每個子模組對應一個主要功能領域。

**技術棧**: Kotlin + Java 混合  
**設計模式**: MVC, 頁面容器單例模式  
**基類**: ASViewController, TelnetListPage

---

## 📂 子模組結構

### 1️⃣ `articlePage/` - 文章閱讀頁面
- `ArticlePage.kt` - 文章內容顯示
- 支援 ANSI 色碼渲染、推文顯示、內容捲動

### 2️⃣ `boardPage/` - 看板頁面
- `BoardMainPage.kt` - 看板文章列表（**最重要的頁面**）
- `BoardPageItem.kt` - 文章列表項目
- 實作 20 項/區塊載入、自動刷新、發文/推文

### 3️⃣ `bookmarkPage/` - 書籤頁面
- `BookmarkPage.kt` - 我的最愛管理
- 書籤新增、刪除、排序

### 4️⃣ `essencePage/` - 精華區頁面
- `EssencePage.kt` - 精華區瀏覽
- 階層式導航、文章收藏

### 5️⃣ `mailPage/` - 信箱頁面
- `MailListPage.kt` - 信件列表
- `MailArticlePage.kt` - 信件內容
- 收信、寄信、回信功能

### 6️⃣ `messages/` - 訊息系統
- `MessageBig.kt` - 大型訊息視窗
- `MessageSmall.kt` - 小型訊息提示
- 站內訊息、系統通知

### 7️⃣ `bbsUser/` - 使用者頁面
- `UserProfilePage.kt` - 使用者資料
- `UserInfoPage.kt` - 使用者資訊

### 8️⃣ `blockListPage/` - 區塊列表
- 表情符號列表、特殊符號列表

### 9️⃣ `login/` - 登入頁面
- `LoginPage.kt` - 登入介面
- 帳號密碼輸入、自動登入

### 🔟 `theme/` - 主題系統
- `ThemePage.kt` - 主題設定
- 顏色配置、字型設定

### 1️⃣1️⃣ `model/` - 頁面共用模型
- 頁面間共用的資料結構和工具

---

## 🎯 根目錄重要頁面

### `MainPage.kt` - 主頁面
```kotlin
class MainPage : TelnetPage() {
    
    override val pageLayout = R.layout.main_page_layout
    
    override fun onPageDidLoad() {
        super.onPageDidLoad()
        
        // 顯示主選單
        setupMenuButtons()
    }
    
    fun navigateToBoard(boardName: String) {
        val boardPage = PageContainer.instance!!.boardPage
        boardPage.setBoardName(boardName)
        navigationController.pushViewController(boardPage)
    }
}
```

### `PostArticlePage.kt` - 發文頁面
```kotlin
class PostArticlePage : ASViewController() {
    
    override val pageLayout = R.layout.post_article_page
    
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    
    var listener: PostArticlePageListener? = null
    
    override fun onPageDidLoad() {
        super.onPageDidLoad()
        
        submitButton.setOnClickListener {
            submitArticle()
        }
    }
    
    private fun submitArticle() {
        val title = titleEditText.text.toString()
        val content = contentEditText.text.toString()
        
        listener?.onArticleSubmitted(title, content)
        navigationController.popViewController()
    }
}

interface PostArticlePageListener {
    fun onArticleSubmitted(title: String, content: String)
}
```

### `ClassPage.kt` - 分類頁面
```kotlin
class ClassPage : TelnetListPage() {
    
    private var className: String = ""
    private var classTitle: String = ""
    
    fun setClass(name: String, title: String) {
        this.className = name
        this.classTitle = title
    }
    
    override fun loadPage(): TelnetListPageBlock? {
        // 載入分類下的看板列表
    }
}
```

---

## 🔧 PageContainer 單例模式

```kotlin
class PageContainer {
    
    // ===== 看板相關頁面 =====
    private var _boardPage: BoardMainPage? = null
    val boardPage: BoardMainPage
        get() {
            if (_boardPage == null) {
                _boardPage = BoardMainPage()
            }
            return _boardPage!!
        }
    
    fun cleanBoardPage() {
        _boardPage?.clear()
        _boardPage = null
    }
    
    // ===== 文章頁面 =====
    private var _articlePage: ArticlePage? = null
    val articlePage: ArticlePage
        get() {
            if (_articlePage == null) {
                _articlePage = ArticlePage()
            }
            return _articlePage!!
        }
    
    // ===== 書籤頁面 =====
    private var _bookmarkPage: BookmarkPage? = null
    val bookmarkPage: BookmarkPage
        get() {
            if (_bookmarkPage == null) {
                _bookmarkPage = BookmarkPage()
            }
            return _bookmarkPage!!
        }
    
    // ===== 分類頁面堆疊 =====
    private val classPageStack = Stack<ClassPage>()
    
    fun pushClassPage(className: String, classTitle: String) {
        val page = ClassPage()
        page.setClass(className, classTitle)
        classPageStack.push(page)
    }
    
    fun popClassPage(): ClassPage? {
        return if (classPageStack.isNotEmpty()) {
            classPageStack.pop()
        } else {
            null
        }
    }
    
    val currentClassPage: ClassPage?
        get() = classPageStack.lastOrNull()
    
    companion object {
        var instance: PageContainer? = null
            private set
        
        fun initialize() {
            if (instance == null) {
                instance = PageContainer()
            }
        }
    }
}
```

**使用範例**:
```kotlin
// 獲取看板頁面（單例）
val boardPage = PageContainer.instance!!.boardPage
boardPage.setBoardName("C_Chat")
navigationController.pushViewController(boardPage)

// 清理看板頁面
PageContainer.instance!!.cleanBoardPage()

// 分類頁面堆疊
PageContainer.instance!!.pushClassPage("SYSOP", "系統")
val classPage = PageContainer.instance!!.currentClassPage
navigationController.pushViewController(classPage!!)

// Pop 分類頁面
PageContainer.instance!!.popClassPage()
```

---

## 📚 相關模組

- [asFramework-pageController](asFramework-pageController.md) - 頁面基類
- [Bahamut-listPage](Bahamut-listPage.md) - 列表頁面基礎
- [Bahamut-command](Bahamut-command.md) - BBS 命令
- [Bahamut-dialogs](Bahamut-dialogs.md) - 業務對話框

---

## 📝 技術特點總結

1. **模組化設計**: 按功能劃分子模組
2. **單例管理**: PageContainer 統一管理頁面實例
3. **混合語言**: Kotlin + Java 混合開發
4. **生命週期管理**: 繼承 ASViewController
5. **分類堆疊**: 支援多層分類導航
6. **資源回收**: 清理頁面釋放記憶體
