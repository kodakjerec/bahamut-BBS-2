# Bahamut/pages/bookmarkPage

## 概述
bookmarkPage 模組提供書籤和瀏覽歷史管理功能。

## 主要元件

### BookmarkManagePage
書籤管理頁面，管理使用者的看板書籤

### UI 架構
- 純 Jetpack Compose 實作 (`setBahamutContent`)，列表使用 `LazyColumn` 與 `BookmarkRowItem` / `HistoryRowItem`

### 介面
- `BoardExtendOptionalPageListener` - 看板擴充頁面監聽器

## 功能
- 書籤新增、刪除、排序
- 書籤分類管理
- 瀏覽歷史記錄
- 快速進入看板

## 資料來源
- 本地 BookmarkStore
- SharedPreferences 持久化

## 操作功能
- 點擊書籤進入看板
- 長按編輯或刪除
- 拖曳排序
- 清除歷史記錄

## 技術特點
- Jetpack Compose LazyColumn 實現
- VIP 排序功能
- 本地資料持久化
- 使用 Kotlin 開發
