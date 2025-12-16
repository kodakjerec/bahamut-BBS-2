# Bahamut/pages/blockListPage

## 概述
blockListPage 模組提供區塊列表頁面功能，包含文章標題列表和表情符號列表。

## 主要元件

### BlockListPage
區塊列表頁面基類

### ArticleHeaderListPage
文章標題列表頁面，顯示相同標題的文章串

### ArticleExpressionListPage
文章表情符號列表頁面，顯示可插入的表情符號

### Adapter 和 ViewHolder
- `BlockListAdapter` - 區塊列表適配器
- `BlockListViewHolder` - 區塊列表 ViewHolder
- `BlockListClickListener` - 點擊監聽器

## 功能
- 同標題文章串瀏覽
- 表情符號選擇
- 快速插入功能

## 使用場景
- 同標題文章向上/向下瀏覽
- 發文時插入表情符號
- 推文時選擇表情

## 技術特點
- RecyclerView 實現
- 點擊事件處理
- 使用 Kotlin 開發
