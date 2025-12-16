# Bahamut/pages/mailPage

## 概述
mailPage 模組實現 BBS 信箱功能，提供信件收發和管理。

## 主要元件

### MailBoxPage
信箱列表頁面，顯示收到的信件列表

### MailPage
信件內容頁面，顯示單封信件的內容

### SendMailPage
寄信頁面，撰寫和發送信件
- `SendMailPageListener` - 寄信監聽器

### 視圖元件
- `MailBoxPageItemView` - 信箱項目視圖

## 功能
- 收件匣瀏覽
- 閱讀信件
- 寄信給使用者
- 回信
- 轉寄信件
- 刪除信件
- 信件搜尋

## 信件操作
- 讀取未讀信件
- 標記為已讀/未讀
- 刪除信件
- 回覆信件
- 保存信件

## 技術特點
- 繼承自 TelnetListPage
- 分頁載入機制
- 信件狀態管理
- 使用 Kotlin 開發
