# Bahamut/dialogs

## 概述
dialogs 模組提供所有業務相關的對話框，包含發文、推文、搜尋、選色、圖片上傳等功能。

## 主要對話框

### 文章操作
- `DialogPostArticle` - 發文對話框
  - `DialogPostArticleListener` - 發文監聽器
- `DialogPushArticle` - 推文對話框
- `DialogSelectArticle` - 選擇文章對話框
  - `DialogSelectArticleListener` - 選擇監聽器
- `DialogSearchArticle` - 搜尋文章對話框
  - `DialogSearchArticleListener` - 搜尋監聽器
- `DialogReference` - 引用對話框
  - `DialogReferenceListener` - 引用監聽器

### 編輯工具
- `DialogInsertExpression` - 插入表情符號對話框
  - `DialogInsertExpressionListener` - 插入監聽器
- `DialogInsertSymbol` - 插入符號對話框
  - `DialogInsertSymbolListener` - 插入監聽器
- `DialogSelectSign` - 選擇簽名檔對話框
  - `DialogSelectSignListener` - 選擇監聽器

### 色彩工具
- `DialogColorPicker` - 選色器對話框
  - `DialogColorPickerListener` - 選色監聽器
- `DialogPaintColor` - 著色對話框
  - `DialogPaintColorListener` - 著色監聽器

### 圖片和網址
- `DialogShortenUrl` - 縮網址對話框
  - `DialogShortenUrlListener` - 縮網址監聽器
  - `DialogShortenUrlItemViewAdapter` - 項目適配器
  - `DialogShortenUrlItemViewListener` - 項目監聽器
  - `DialogShortenUrlViewHolder` - ViewHolder
- `DialogShortenImage` - 縮圖對話框
- **uploadImgMethod** - 圖片上傳方法子模組
  - `UploaderLitterCatBox` - LitterCatBox 上傳器
  - `UploaderPostimageorg` - Postimage.org 上傳器

### 看板和搜尋
- `DialogSearchBoard` - 搜尋看板對話框
  - `DialogSearchBoardListener` - 搜尋監聽器

### 其他
- `DialogQueryHero` - 查詢勇者對話框
- `DialogHeroStep` - 勇者步數對話框

## 技術特點
- 繼承自 asFramework 對話框系統
- 事件監聽機制
- 支援多種圖床上傳
- 使用 Kotlin 開發
