# Bahamut/command - BBS 命令系統（核心）

**applyto**: `app/src/main/java/com/kota/Bahamut/command/**/*.kt`

## 📋 模組概述

command 模組實現所有 BBS 操作命令，使用**命令模式 (Command Pattern)** 封裝 Telnet 指令序列。每個命令代表一個完整的 BBS 操作（如載入文章、發文、推文等），支援非同步執行和回呼通知。

**這是 BBS 客戶端與伺服器通訊的核心抽象層。**

**技術棧**: Kotlin, Telnet Protocol  
**設計模式**: 命令模式, 模板方法模式  
**命名前綴**: BahamutCommand

---

## 📂 命令分類

### 1️⃣ 文章命令

#### 基本操作
```kotlin
// 列出文章列表
class BahamutCommandListArticle : TelnetCommand()

// 載入文章內容
class BahamutCommandLoadArticle : TelnetCommand()

// 載入到文章結尾
class BahamutCommandLoadArticleEnd : TelnetCommand()

// 載入更多文章內容
class BahamutCommandLoadMoreArticle : TelnetCommand()
```

#### 發文和編輯
```kotlin
// 發表文章
class BahamutCommandPostArticle(
    private val title: String,
    private val content: String,
    private val sign: Int
) : TelnetCommand()

// 編輯文章
class BahamutCommandEditArticle : TelnetCommand()

// 刪除文章
class BahamutCommandDeleteArticle : TelnetCommand()
```

#### 推文和評論
```kotlin
// 推文
class BahamutCommandPushArticle(
    private val pushType: Int,  // 1=推, 2=噓, 3=→
    private val content: String
) : TelnetCommand()

// 標記好文
class BahamutCommandGoodArticle : TelnetCommand()
```

#### 搜尋
```kotlin
// 搜尋文章
class BahamutCommandSearchArticle(
    private val keyword: String,
    private val searchType: Int  // 1=標題, 2=作者, 3=內容
) : TelnetCommand()
```

---

### 2️⃣ 區塊命令（分頁載入）

```kotlin
// 載入指定區塊 (20 項/區塊)
class BahamutCommandLoadBlock(
    private val blockIndex: Int
) : TelnetCommand() {
    
    override fun execute(page: TelnetListPage) {
        // 計算區塊範圍
        val startIndex = blockIndex * 20 + 1
        val endIndex = startIndex + 19
        
        // 發送 Telnet 命令跳轉到起始位置
        TelnetOutputBuilder.create()
            .pushKey(TelnetKeyboard.CTRL_Z)  // 跳轉命令
            .pushString("$startIndex\n")      // 輸入起始編號
            .sendToServer()
    }
    
    override fun executeFinished(page: TelnetListPage, block: TelnetListPageBlock?) {
        // 設定載入的區塊資料
        page.setBlock(blockIndex, block)
        
        // 更新 UI
        object : ASRunner() {
            override fun run() {
                page.safeNotifyDataSetChanged()
            }
        }.runInMainThread()
    }
}

// 載入第一個區塊
class BahamutCommandLoadFirstBlock : TelnetCommand()

// 載入最後區塊（最新內容）
class BahamutCommandLoadLastBlock : TelnetCommand()

// 移動到最後區塊
class BahamutCommandMoveToLastBlock : TelnetCommand()
```

---

### 3️⃣ 導航命令

```kotlin
// 同標題導航
class BahamutCommandTheSameTitleTop : TelnetCommand()      // 到第一篇同標題
class BahamutCommandTheSameTitleUp : TelnetCommand()       // 上一篇同標題
class BahamutCommandTheSameTitleDown : TelnetCommand()     // 下一篇同標題
class BahamutCommandTheSameTitleBottom : TelnetCommand()   // 到最後同標題
```

---

### 4️⃣ 信件命令

```kotlin
// 寄信
class BahamutCommandSendMail(
    private val recipient: String,
    private val subject: String,
    private val content: String
) : TelnetCommand()

// 轉寄信件
class BahamutCommandFSendMail(
    private val recipient: String
) : TelnetCommand()
```

---

## 🎯 命令模式架構

### `TelnetCommand.kt` - 命令基類

```kotlin
abstract class TelnetCommand {
    
    /**
     * 執行命令（發送 Telnet 指令）
     * @param page 呼叫此命令的頁面
     */
    open fun execute(page: TelnetListPage) {
        // 子類實作：發送 Telnet 按鍵序列
    }
    
    /**
     * 命令執行完成回呼
     * @param page 呼叫此命令的頁面
     * @param block 伺服器回傳的資料區塊（如果有）
     */
    open fun executeFinished(page: TelnetListPage, block: TelnetListPageBlock?) {
        // 子類實作：處理伺服器回應
    }
    
    /**
     * 命令執行失敗回呼
     */
    open fun executeFailed(page: TelnetListPage, error: Exception) {
        object : ASRunner() {
            override fun run() {
                ASToast.show(page.context, "操作失敗：${error.message}")
            }
        }.runInMainThread()
    }
}
```

---

## 📖 使用範例

### 1. 載入區塊

```kotlin
class BoardMainPage : TelnetListPage() {
    
    fun loadBlock(blockIndex: Int) {
        // 建立命令
        val command = BahamutCommandLoadBlock(blockIndex)
        
        // 加入命令佇列（自動執行）
        pushCommand(command)
    }
    
    fun loadFirstPage() {
        pushCommand(BahamutCommandLoadFirstBlock())
    }
    
    fun loadLatestArticles() {
        pushCommand(BahamutCommandLoadLastBlock())
    }
}
```

### 2. 發文

```kotlin
class PostArticlePage : ASViewController() {
    
    fun submitArticle(title: String, content: String, signIndex: Int) {
        ASProcessingDialog.showProcessingDialog("發文中...")
        
        val command = BahamutCommandPostArticle(title, content, signIndex)
        
        // 設定成功回呼
        command.onSuccess = {
            object : ASRunner() {
                override fun run() {
                    ASProcessingDialog.dismissProcessingDialog()
                    ASToast.show(context, "發文成功")
                    navigationController.popViewController()
                }
            }.runInMainThread()
        }
        
        // 設定失敗回呼
        command.onFailure = { error ->
            object : ASRunner() {
                override fun run() {
                    ASProcessingDialog.dismissProcessingDialog()
                    showErrorDialog(error.message)
                }
            }.runInMainThread()
        }
        
        pushCommand(command)
    }
}
```

### 3. 推文

```kotlin
class ArticlePage : TelnetPage() {
    
    fun pushArticle(pushType: Int, content: String) {
        val command = BahamutCommandPushArticle(pushType, content)
        pushCommand(command)
    }
    
    // 推
    fun thumbsUp(message: String) {
        pushArticle(1, message)
    }
    
    // 噓
    fun thumbsDown(message: String) {
        pushArticle(2, message)
    }
    
    // 留言
    fun comment(message: String) {
        pushArticle(3, message)
    }
}
```

### 4. 搜尋文章

```kotlin
fun searchByTitle(keyword: String) {
    val command = BahamutCommandSearchArticle(keyword, searchType = 1)
    pushCommand(command)
}

fun searchByAuthor(author: String) {
    val command = BahamutCommandSearchArticle(author, searchType = 2)
    pushCommand(command)
}

fun searchByContent(keyword: String) {
    val command = BahamutCommandSearchArticle(keyword, searchType = 3)
    pushCommand(command)
}
```

---

## 🔧 命令佇列機制

```kotlin
class TelnetListPage : ASViewController() {
    
    private val commandQueue = LinkedList<TelnetCommand>()
    private var isExecutingCommand = false
    
    /**
     * 加入命令到佇列
     */
    fun pushCommand(command: TelnetCommand) {
        synchronized(commandQueue) {
            commandQueue.add(command)
        }
        
        if (!isExecutingCommand) {
            executeNextCommand()
        }
    }
    
    /**
     * 執行下一個命令
     */
    private fun executeNextCommand() {
        synchronized(commandQueue) {
            if (commandQueue.isEmpty()) {
                isExecutingCommand = false
                return
            }
            
            isExecutingCommand = true
            val command = commandQueue.poll()
            
            try {
                command.execute(this)
            } catch (e: Exception) {
                command.executeFailed(this, e)
                isExecutingCommand = false
                executeNextCommand() // 繼續下一個
            }
        }
    }
    
    /**
     * 命令完成後呼叫
     */
    fun onCommandFinished(block: TelnetListPageBlock?) {
        val command = currentCommand
        command?.executeFinished(this, block)
        
        isExecutingCommand = false
        executeNextCommand() // 執行下一個命令
    }
}
```

---

## ⚠️ 注意事項

### 1. 命令執行順序

```kotlin
// ✅ 正確：命令會依序執行
pushCommand(command1)
pushCommand(command2)
pushCommand(command3)
// 執行順序：command1 -> command2 -> command3

// ⚠️ 注意：前一個命令完成後才會執行下一個
```

### 2. 取消命令

```kotlin
// 清空命令佇列
fun cancelAllCommands() {
    synchronized(commandQueue) {
        commandQueue.clear()
    }
}

// 取消當前命令（需要特殊處理）
fun cancelCurrentCommand() {
    TelnetClient.disconnect() // 中斷連線
}
```

---

## 📚 相關模組

- [Bahamut-listPage](Bahamut-listPage.md) - 列表頁面（命令執行環境）
- [telnet-logic](telnet-logic.md) - Telnet 業務邏輯
- [telnet-model](telnet-model.md) - Telnet 資料模型

---

## 📝 技術特點總結

1. **命令模式**: 封裝操作為物件，支援佇列和復原
2. **非同步執行**: 不阻塞 UI 執行緒
3. **模板方法**: execute/executeFinished 定義標準流程
4. **佇列管理**: 自動依序執行命令
5. **錯誤處理**: 統一的異常處理機制
6. **回呼通知**: 支援成功/失敗回呼
7. **可測試性**: 命令物件易於單元測試
