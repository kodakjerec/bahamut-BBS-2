# Bahamut/pages/bookmarkPage

## 概述
bookmarkPage 模組提供書籤和瀏覽歷史管理功能。

## 主要元件

### BookmarkManagePage
書籤管理頁面，管理使用者的看板書籤

### Adapter 和 ViewHolder
- `BookmarkAdapter` - 書籤適配器
- `BookmarkViewHolder` - 書籤 ViewHolder
- `BookmarkClickListener` - 書籤點擊監聽器
- `HistoryAdapter` - 歷史記錄適配器
- `HistoryViewHolder` - 歷史記錄 ViewHolder

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
- RecyclerView 實現
- ItemTouchHelper 拖曳排序
- 本地資料持久化
- 使用 Kotlin 開發
