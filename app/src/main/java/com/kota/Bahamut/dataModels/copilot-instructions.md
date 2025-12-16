# Bahamut/dataModels

## 概述
dataModels 模組提供資料模型和本地儲存功能，包含書籤、文章暫存、縮網址等資料管理。

## 主要元件

### 書籤系統
- `Bookmark` - 書籤資料模型
- `BookmarkList` - 書籤列表
- `BookmarkStore` - 書籤儲存管理器

### 文章暫存
- `ArticleTemp` - 暫存文章資料模型
- `ArticleTempStore` - 文章暫存儲存管理器

### 縮網址
- `ShortenUrl` - 縮網址資料模型
- `UrlDatabase` - URL 資料庫

### 其他
- `ReferenceAuthor` - 引用作者資訊

## 功能
- 書籤的新增、刪除、查詢
- 文章草稿暫存
- 縮網址記錄和管理
- 本地資料持久化

## 儲存機制
- 使用 SharedPreferences 或檔案系統
- JSON 序列化/反序列化
- 資料快取管理

## 技術特點
- 資料持久化
- 本地資料庫
- 使用 Kotlin 開發
