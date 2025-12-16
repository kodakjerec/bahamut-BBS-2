# Bahamut/pages/model

## 概述
model 模組提供頁面共用的資料模型、處理器和工具元件。

## 主要元件

### 看板頁面模型
- `BoardPageBlock` - 看板頁面區塊
- `BoardPageHandler` - 看板頁面處理器
- `BoardPageItem` - 看板項目

### 分類頁面模型
- `ClassPageBlock` - 分類頁面區塊
- `ClassPageHandler` - 分類頁面處理器
- `ClassPageItem` - 分類項目

### 信箱頁面模型
- `MailBoxPageBlock` - 信箱頁面區塊
- `MailBoxPageHandler` - 信箱頁面處理器
- `MailBoxPageItem` - 信箱項目

### 精華區頁面模型
- `BoardEssencePageItem` - 精華區項目
- `BoardEssencePageItemView` - 精華區項目視圖

### UI 元件
- `PostEditText` - 發文編輯文字框
- `ToolBarFloating` - 浮動工具列

## 功能
- 頁面資料解析
- Telnet 資料轉換為結構化資料
- 頁面狀態處理
- 項目視圖封裝

## Handler 功能
- 解析 Telnet 畫面內容
- 識別項目類型
- 提取項目資訊
- 狀態判斷

## 技術特點
- 資料模型封裝
- 處理器模式
- 可重用元件
- 使用 Kotlin 開發
