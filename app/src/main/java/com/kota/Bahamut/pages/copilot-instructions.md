# Bahamut/pages

## 概述
pages 模組包含應用程式的所有業務頁面，實現各種 BBS 功能介面。

## 主要頁面模組

### articlePage - 文章頁面
文章內容顯示和互動功能

### bbsUser - BBS 使用者
使用者資訊和設定頁面

### blockListPage - 區塊列表頁面
文章區塊和表情符號列表

### boardPage - 看板頁面
看板瀏覽和搜尋功能

### bookmarkPage - 書籤頁面
書籤和歷史記錄管理

### essencePage - 精華區頁面
精華區瀏覽功能

### login - 登入頁面
使用者登入介面

### mailPage - 信箱頁面
信件收發功能

### messages - 訊息系統
站內訊息和聊天功能

### model - 頁面模型
頁面共用的資料模型和工具類別

### theme - 主題系統
應用程式主題管理

## 根目錄頁面
- `MainPage.kt` - 主頁面
- `StartPage.kt` - 啟動頁面
- `ClassPage.kt` - 分類頁面
  - `ClassPageItemView.kt` - 分類項目視圖
- `HeroStepItemView.kt` - 勇者步數項目視圖
- `BillingPage.kt` - 付費頁面
- `SystemSettingsPage.kt` - 系統設定頁面
- `PostArticlePage.kt` - 發文頁面
  - `PostArticlePageListener.kt` - 發文監聽器
- `ListAction.kt` - 列表操作定義

## 技術特點
- 繼承自 ASViewController
- 頁面生命週期管理
- 模組化設計
- 使用 Kotlin 開發
