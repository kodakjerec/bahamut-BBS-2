# Bahamut/pages/boardPage

## 概述
boardPage 模組實現看板相關頁面，包含看板主頁、看板搜尋、看板連結等功能。

## 主要元件

### BoardMainPage
看板主頁，顯示文章列表和看板資訊

### BoardSearchPage
看板搜尋頁面，搜尋文章功能

### BoardLinkPage
看板連結頁面，顯示看板的連結看板

### 視圖元件
- `BoardHeaderView` - 看板標題視圖
- `BoardPageItemView` - 看板項目視圖
- `BoardExtendBookmarkItemView` - 書籤擴充項目視圖
- `BoardExtendHistoryItemView` - 歷史記錄擴充項目視圖

### 其他
- `BoardPageAction` - 看板頁面操作定義

## 功能特點
- 文章列表分頁載入
- 看板資訊顯示
- 文章搜尋（作者、標題、推文數）
- 快速書籤功能
- 瀏覽歷史記錄
- 看板連結導航

## 操作功能
- 閱讀文章
- 發表新文章
- 搜尋文章
- 加入書籤
- 重新整理

## 技術特點
- 繼承自 TelnetListPage
- 分頁載入機制
- 狀態持久化
- 使用 Kotlin 開發
