# 串接頁面編輯文章 - 實作計劃

## 📋 功能說明

讓使用者在文章串接頁面 (`BoardLinkPage`, `BoardSearchPage`) 也能修改自己的文章。

## 🔑 關鍵變數

| 變數 | 說明 | 來源 |
|------|------|------|
| `articleNumber` | LinkPage/SearchPage 中的位置 | `TelnetArticle.articleNumber` |
| `boardNumber` | 版面文章編號（真正的文章編號） | 送出 "t" 後從 row4 取得 |
| `currentPage` | 當前頁面狀態 | `BahamutStateHandler.currentPage` |

## 🔧 關鍵操作

| 操作 | 方法 | 說明 |
|------|------|------|
| 選擇文章位置 | `setListViewSelection(boardNumber - 1)` | 直接跳到指定編號 |
| 讀取文章 | `loadItemAtIndex(boardNumber - 1)` | 自動進入文章頁 |
| 判斷頁面 | `currentPage` | `BAHAMUT_BOARD` / `BAHAMUT_BOARD_LINK` / `BAHAMUT_BOARD_SEARCH` |

---

## 🔄 正常流程

```
┌─────────────────────────────────────────────────────────────────────────┐
│ [LinkPage/SearchPage] 使用者點擊編輯                                     │
│                                                                          │
│  1. 保存目標文章特徵 (title, author, dateTime, boardName)               │
│  2. articleNumber = targetArticle.articleNumber                         │
│                                                                          │
│  判斷: articleNumber % 20 == 0 ?                                        │
│        ├── YES → 例外流程1                                               │
│        └── NO  → 正常流程                                                │
└─────────────────────────────────────────────────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────────────────────────────────────┐
│ 1. 送出 "t"                                                              │
│    → 畫面上游標所在行的 articleNumber 會被替換為 boardNumber              │
└─────────────────────────────────────────────────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────────────────────────────────────┐
│ 2. StateHandler 檢查游標所在行                                           │
│                                                                          │
│    判斷: articleNumber 是否被替換為 boardNumber ?                        │
│          ├── YES → 成功取得 boardNumber，繼續流程                        │
│          └── NO  → 檢查 row4 編號是否為 1                                │
│                    ├── row4 == 1 → 例外流程2 (最後一篇，繞回第一篇)      │
│                    └── row4 != 1 → 異常狀態，中止並提示錯誤              │
└─────────────────────────────────────────────────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────────────────────────────────────┐
│ 3. 送出 "left" → 離開 LinkPage/SearchPage                                │
│    StateHandler 偵測到 BoardMainPage                                     │
└─────────────────────────────────────────────────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────────────────────────────────────┐
│ 4. boardPage.setListViewSelection(boardNumber - 1)                       │
│    → 選擇版面文章                                                        │
└─────────────────────────────────────────────────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────────────────────────────────────┐
│ 5. boardPage.loadItemAtIndex(boardNumber - 1)                            │
│    → 進入文章，讀取完畢後 ArticlePage.setArticle() 被呼叫                 │
└─────────────────────────────────────────────────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────────────────────────────────────┐
│ 6. 在 ArticlePage 驗證特徵:                                              │
│    article.title == target.title &&                                     │
│    article.author == target.author &&                                   │
│    article.dateTime == target.dateTime                                  │
│                                                                          │
│    ✓ 一致 → 自動進入編輯模式                                             │
│    ✗ 不一致 → Toast "找不到文章"，返回 BoardMainPage                     │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## ⚠️ 例外流程1：articleNumber 是 20 的倍數

**問題**：20 的倍數在頁面最後一行，送出 "t" 會顯示下一區塊第一筆的 boardNumber

```
┌─────────────────────────────────────────────────────────────────────────┐
│ 1. 送出 "Up" → 移到上一筆                                                │
│ 2. 送出 "t" → 取得 boardNumber (這是 articleNumber-1 的版面編號)         │
│ 3. 送出 "left" → 回到 BoardMainPage                                      │
│ 4. boardPage.setListViewSelection(boardNumber - 1)                       │
│ 5. boardPage.loadItemAtIndex(boardNumber - 1)                            │
│    → 此時特徵一定不一致                                                   │
└─────────────────────────────────────────────────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────────────────────────────────────┐
│ 6. 在 ArticlePage 驗證失敗後:                                            │
│    送出 "]" 移到下一篇，重新讀取並驗證                                    │
│    最多重試 3 次                                                         │
│                                                                          │
│    ✓ 一致 → 進入編輯                                                     │
│    ✗ 3次都不一致 → Toast "找不到文章"                                    │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## ⚠️ 例外流程2：boardNumber == 1 (最後一篇)

**問題**：送出 "t" 後 row4 顯示的 boardNumber 是 1，代表原文章是版面最後一篇（繞回第一篇）

```
┌─────────────────────────────────────────────────────────────────────────┐
│ 1. 送出 "left" → 回到 BoardMainPage                                      │
│ 2. boardPage.moveToLastPosition() → 移到版面最後                         │
└─────────────────────────────────────────────────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────────────────────────────────────┐
│ 3. 在 ArticlePage (從最後一筆開始):                                      │
│    送出 "[" 往上一篇，讀取並驗證                                         │
│    最多重試 3 次                                                         │
│                                                                          │
│    ✓ 一致 → 進入編輯                                                     │
│    ✗ 3次都不一致 → Toast "找不到文章"                                    │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 🏗️ 程式碼結構

### 1. 新增檔案

| 檔案 | 說明 |
|------|------|
| `TempSettings.kt` | 新增 `editFromLinkedState` |
| `EditFromLinkedState.kt` | 狀態資料類別 |
| `BahamutCommandDef.kt` | 新增 `EDIT_FROM_LINKED = 15` |

### 2. 狀態定義

```kotlin
// service/EditFromLinkedState.kt
data class EditFromLinkedState(
    val targetArticle: TelnetArticle,  // 目標文章特徵
    var boardNumber: Int = 0,          // 從 "t" 取得
    var step: EditFromLinkedStep = EditFromLinkedStep.INIT,
    var retryCount: Int = 0
) {
    val articleNumber: Int get() = targetArticle.articleNumber
    val isBlockBoundary: Boolean get() = articleNumber % 20 == 0
    var isLastArticle: Boolean = false  // boardNumber == 1
    
    fun matchesTarget(article: TelnetArticle): Boolean {
        return article.title == targetArticle.title &&
               article.author == targetArticle.author &&
               article.dateTime == targetArticle.dateTime
    }
}

enum class EditFromLinkedStep {
    INIT,                    // 初始
    MOVE_UP_FOR_BOUNDARY,    // (例外1) 已送出 Up，等待回應
    SENT_T,                  // 已送出 "t"，等待解析 boardNumber
    LEAVING_LINKED_PAGE,     // 已送出 "left"，等待進入 BoardMainPage
    ON_BOARD_PAGE,           // 已在 BoardMainPage，準備選擇文章
    READING_ARTICLE,         // 正在讀取文章
    VERIFYING,               // 驗證特徵中
    SEARCH_NEXT,             // (例外1) 送出 "]" 找下一篇
    SEARCH_PREV,             // (例外2) 送出 "[" 找上一篇
    GOTO_LAST,               // (例外2) 移到最後
    DONE,                    // 完成，進入編輯
    FAILED                   // 失敗
}
```

### 3. TempSettings 新增

```kotlin
// TempSettings.kt
object TempSettings {
    // ... 現有欄位 ...
    
    /** 從串接頁編輯文章的狀態 */
    var editFromLinkedState: EditFromLinkedState? = null
}
```

### 4. StateHandler 處理

```kotlin
// BahamutStateHandler.kt

/** 處理從串接頁編輯文章的狀態 */
fun handleEditFromLinkedState(): Boolean {
    val state = TempSettings.editFromLinkedState ?: return false
    
    when (state.step) {
        EditFromLinkedStep.MOVE_UP_FOR_BOUNDARY -> {
            // 偵測到 LinkPage/SearchPage，送出 "t"
            if (currentPage == BahamutPage.BAHAMUT_BOARD_LINK || 
                currentPage == BahamutPage.BAHAMUT_BOARD_SEARCH) {
                state.step = EditFromLinkedStep.SENT_T
                TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.SMALL_T)
                return true
            }
        }
        
        EditFromLinkedStep.SENT_T -> {
            // 解析 row4 取得 boardNumber
            val boardNum = parseBoardNumberFromRow4()
            state.boardNumber = boardNum
            state.isLastArticle = (boardNum == 1)
            
            // 離開串接頁
            state.step = EditFromLinkedStep.LEAVING_LINKED_PAGE
            TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.LEFT_ARROW)
            return true
        }
        
        EditFromLinkedStep.LEAVING_LINKED_PAGE -> {
            // 偵測到 BoardMainPage
            if (currentPage == BahamutPage.BAHAMUT_BOARD) {
                state.step = EditFromLinkedStep.ON_BOARD_PAGE
                
                ASCoroutine.ensureMainThread {
                    val boardPage = PageContainer.instance!!.boardPage
                    
                    if (state.isLastArticle) {
                        // 例外2: 移到最後
                        state.step = EditFromLinkedStep.GOTO_LAST
                        boardPage.moveToLastPosition()
                    } else {
                        // 正常/例外1: 選擇文章並進入
                        state.step = EditFromLinkedStep.READING_ARTICLE
                        boardPage.setListViewSelection(state.boardNumber - 1)
                        boardPage.loadItemAtIndex(state.boardNumber - 1)
                    }
                }
                return true
            }
        }
        
        // ... 其他步驟
    }
    return false
}

private fun parseBoardNumberFromRow4(): Int {
    val row4 = getRowString(4).trim()
    // 解析格式: "  1234  作者  日期  標題"
    val match = Regex("^\\s*(\\d+)").find(row4)
    return match?.groupValues?.get(1)?.toIntOrNull() ?: 0
}
```

### 5. ArticlePage 驗證

```kotlin
// ArticlePage.kt

override fun setArticle(article: TelnetArticle) {
    // ... 原本的邏輯 ...
    
    // 檢查是否有編輯任務
    val editState = TempSettings.editFromLinkedState
    if (editState != null && editState.step == EditFromLinkedStep.READING_ARTICLE) {
        verifyAndEditArticle(article, editState)
    }
}

private fun verifyAndEditArticle(article: TelnetArticle, state: EditFromLinkedState) {
    if (state.matchesTarget(article)) {
        // 特徵一致，進入編輯
        state.step = EditFromLinkedStep.DONE
        TempSettings.editFromLinkedState = null
        enterEditMode()
    } else {
        // 特徵不一致
        state.retryCount++
        
        if (state.retryCount >= 3) {
            // 重試次數用盡
            state.step = EditFromLinkedStep.FAILED
            TempSettings.editFromLinkedState = null
            showShortToast("找不到文章")
            onBackPressed()
        } else if (state.isBlockBoundary) {
            // 例外1: 找下一篇
            state.step = EditFromLinkedStep.SEARCH_NEXT
            loadTheSameTitleDown() // 送出 "]"
        } else if (state.isLastArticle) {
            // 例外2: 找上一篇  
            state.step = EditFromLinkedStep.SEARCH_PREV
            loadTheSameTitleUp() // 送出 "["
        } else {
            // 正常情況不應該不一致
            state.step = EditFromLinkedStep.FAILED
            TempSettings.editFromLinkedState = null
            showShortToast("找不到文章")
            onBackPressed()
        }
    }
}
```

---

## 📝 實作步驟

1. [ ] 建立 `EditFromLinkedState.kt` 狀態類別
2. [ ] 在 `TempSettings.kt` 新增 `editFromLinkedState`
3. [ ] 在 `BahamutCommandDef.kt` 新增常數
4. [ ] 在 `BahamutStateHandler.kt` 新增 `handleEditFromLinkedState()`
5. [ ] 在 `ArticlePage.kt` 新增驗證邏輯
6. [ ] 在 `BoardLinkPage` / `BoardSearchPage` 新增觸發按鈕
7. [ ] 測試正常流程
8. [ ] 測試例外流程1 (20的倍數)
9. [ ] 測試例外流程2 (最後一篇)
