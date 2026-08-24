# 將文章內文配色移至主題 (Theme)

將 `ArticlePageTextItemView` 中使用的硬編碼顏色（透過 `R.color` 引用）轉移至主題屬性（Theme Attributes），以利於佈景主題切換與維護。

## Proposed Changes

### 資源定義 (Resources)

#### [MODIFY] [attrs.xml](file:///D:/Projects/bahamut-BBS-2/app/src/main/res/values/attrs.xml)
新增 4 個主題屬性，用於定義文章作者與內文的顏色（區分引用與非引用）。

#### [MODIFY] [styles.xml](file:///D:/Projects/bahamut-BBS-2/app/src/main/res/values/styles.xml)
在 `MyTheme` 中為新屬性指定預設值，引用自 `colors.xml` 中現有的顏色。

#### [MODIFY] [article_page_text_item_view.xml](file:///D:/Projects/bahamut-BBS-2/app/src/main/res/layout/article_page_text_item_view.xml)
將預設的 `textColor` 修改為使用主題屬性。

### 程式邏輯 (Logic)

#### [MODIFY] [CommonFunctions.kt](file:///D:/Projects/bahamut-BBS-2/app/src/main/java/com/kota/Bahamut/service/CommonFunctions.kt)
新增 `getThemeColor` 靜態方法，方便在程式碼中解析主題屬性顏色。

#### [MODIFY] [ArticlePageTextItemView.kt](file:///D:/Projects/bahamut-BBS-2/app/src/main/java/com/kota/Bahamut/pages/articlePage/ArticlePageTextItemView.kt)
在 `setQuote` 方法中改用主題屬性顏色，取代原本直接引用 `R.color` 的做法。

## Verification Plan

### Automated Tests
- 執行專案編譯，確保 `R.attr` 引用正確。

### Manual Verification
- 開啟文章頁面，確認一般文章與引用文章的顏色顯示正確（與修改前一致）。
- 測試切換深色模式（如有），確認顏色能隨之變化（目前主要在 `MyTheme` 中定義，暫不涉及 `values-night` 的額外調整，除非原本 `colors.xml` 有區分）。
