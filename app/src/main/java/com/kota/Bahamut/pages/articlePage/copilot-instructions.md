# Bahamut/pages/articlePage

## 概述
articlePage 模組實現文章內容頁面，提供文章閱讀、推文顯示、圖片預覽等功能。

## 主要元件

### ArticlePage
文章頁面主類別，處理文章顯示和互動

### 視圖元件
- `ArticlePageHeaderItemView` - 文章標題項目視圖
- `ArticlePagePushItemView` - 推文項目視圖
- `ArticlePageTelnetItemView` - Telnet 內容項目視圖
- `ArticlePageTextItemView` - 文字項目視圖
- `ArticlePageTimeTimeView` - 時間項目視圖
- `ThumbnailItemView` - 縮圖項目視圖

### 資料模型
- `ArticlePageItemType` - 文章項目類型定義
- `ArticleViewMode` - 文章檢視模式

### 其他
- `MyUrlSpan` - 自訂 URL 超連結樣式

## 功能特點
- 多種內容類型顯示（文字、圖片、推文）
- 推文著色和排版
- 圖片縮圖預覽
- 超連結點擊處理
- 文章內容選取複製
- Telnet 原始畫面顯示

## 檢視模式
- 一般模式：解析後的文章內容
- Telnet 模式：保留原始 ANSI 畫面

## 技術特點
- Jetpack Compose 頁面與項目渲染
- 圖片延遲載入與手勢縮放
- URL 自動識別與預覽
- 使用 Kotlin 開發

## 開發與 AI 指引
- **禁止編譯**：嚴禁由 AI 執行 Gradle 編譯、測試或建置指令（如 `./gradlew compileDebugKotlin` 等），由使用者自行編譯。
- **共用規範**：盡量使用共用的 components（如 `com.kota.Bahamut.ui.components.*`）、colors 與 themes（如 `AppTheme.colors`、`BahamutAppTheme`）。

