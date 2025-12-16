# Bahamut/pages/essencePage

## 概述
essencePage 模組實現精華區功能，提供精華區瀏覽和文章精華區頁面。

## 主要元件

### BoardEssencePage
看板精華區頁面，瀏覽看板的精華文章目錄

### ArticleEssencePage
文章精華區頁面，顯示單篇精華文章

### BoardEssencePageHandler
精華區頁面處理器，處理精華區特殊邏輯

## 功能
- 精華區目錄瀏覽
- 精華文章閱讀
- 精華區導航
- 目錄層級管理

## 資料結構
精華區採用樹狀結構：
- 根目錄
  - 分類目錄
    - 子分類
      - 精華文章

## 操作功能
- 進入子目錄
- 返回上層目錄
- 閱讀精華文章
- 搜尋精華區

## 技術特點
- 樹狀目錄導航
- 狀態堆疊管理
- 使用 Kotlin 開發
