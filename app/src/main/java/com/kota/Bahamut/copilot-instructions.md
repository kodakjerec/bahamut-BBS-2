# Bahamut

## 概述
Bahamut 是核心業務邏輯模組，實現巴哈姆特 BBS 的主要功能，包含頁面、對話框、命令處理、資料模型和服務。

## 主要模組

### command
BBS 命令實現，包含文章操作（讀取、發文、推文、搜尋、刪除等）和看板操作的各種命令

### dataModels
資料模型和本地儲存，包含書籤、文章暫存、縮網址資料庫等

### dialogs
各種對話框，包含發文對話框、搜尋對話框、選色器、插入表情符號、圖片上傳等功能

### listPage
列表頁面的基礎架構，實現文章列表和看板列表的分頁載入和狀態管理

### pages
各種業務頁面：
- 文章頁面（articlePage）
- 看板頁面（boardPage）
- 書籤管理（bookmarkPage）
- 精華區（essencePage）
- 登入頁面（login）
- 信箱頁面（mailPage）
- 訊息系統（messages）
- 主頁面和設定頁面
- 主題管理（theme）

### service
背景服務和設定管理，包含雲端備份、加密、使用者設定、通知設定等

## 關鍵檔案
- `BahamutController.kt` - 主控制器
- `BahamutPage.kt` - 頁面基類
- `MyApplication.kt` - 應用程式入口
- `PageContainer.kt` - 頁面容器

## 技術特點
- 使用 Telnet 協定連接 BBS
- 支援 ANSI 色碼解析
- 實現完整的 BBS 功能（看板瀏覽、文章閱讀、發文推文等）
- 本地資料快取和書籤管理
- 雲端備份功能
- 使用 Kotlin 開發
