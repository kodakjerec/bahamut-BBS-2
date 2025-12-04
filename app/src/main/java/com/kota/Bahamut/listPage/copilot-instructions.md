# Bahamut/listPage

## 概述
listPage 模組提供列表頁面的基礎架構，實現分頁載入、狀態管理和資料區塊管理。

## 核心元件

### TelnetListPage
Telnet 列表頁面基類，處理列表頁面的通用邏輯

### TelnetListPageBlock
列表資料區塊，管理一個資料區塊的內容

### TelnetListPageItem
列表項目，表示列表中的單一項目

### 狀態管理
- `ListState` - 列表狀態
- `ListStateStore` - 列表狀態儲存器

### 命令
- `PagePreloadCommand` - 頁面預載命令
- `PageRefreshCommand` - 頁面重新整理命令

## 功能特點
- 分頁載入機制
- 資料區塊管理
- 狀態持久化
- 上拉/下拉更新
- 快取機制

## 使用場景
- 文章列表頁面
- 看板列表頁面
- 信箱列表頁面
- 精華區列表

## 資料流程
1. 載入第一個區塊
2. 解析區塊資料
3. 顯示列表項目
4. 使用者捲動觸發載入更多
5. 合併新區塊資料
6. 更新列表顯示

## 技術特點
- 區塊式資料管理
- 非同步載入
- 狀態復原機制
- 使用 Kotlin 開發
