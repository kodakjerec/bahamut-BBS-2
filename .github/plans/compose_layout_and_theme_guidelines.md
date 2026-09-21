# Jetpack Compose UI 排版準則、色彩規範與 TelnetView 規格指南

本文件整合近期 Compose 介面重構與樣式調整經驗，詳細定義色彩語意、通用元件設計、對話框底稿規範與 TelnetView 排版標準，作為後續所有 UI 開發與維護的最高準則。

---

## 🎯 核心準則：Theme 與色彩規範 (絕對準則)

> [!IMPORTANT]
> **嚴禁在 UI Composables 中寫死任何顏色表現**（例如 `Color.White`、`Color(0xFF...)`、`Color(0xFF808080)` 等）。
> 所有顏色必須回歸主題系統，透過 `AppTheme.colors.*` 取得。

### 1. 主題架構
- 色彩定義位於 `app/src/main/java/com/kota/Bahamut/ui/theme/`：
  - `Color.kt` (`AppPalette`): 基礎色彩常數定義。
  - `Theme.kt` (`AppColors`): 介面語意化屬性規格。
- **必須同時覆蓋全部 6 種主題配置**：
  1. `DefaultLightColors`: 預設亮色配置
  2. `DefaultDarkColors`: 預設深色配置 (Dark Mode)
  3. `PinkLightColors`: 粉紅主題亮色配置
  4. `PinkDarkColors`: 粉紅主題深色配置
  5. `EInkLightColors`: 電子紙日間高對比模式 (白底黑字)
  6. `EInkDarkColors`: 電子紙夜間高對比模式 (黑底白字)
- 若發現現有語意色彩不敷使用或表現不對，**必須先至 `Theme.kt` 擴充/修正 `AppColors` 與 6 種配置**，絕不可在 Composable 內部直接宣告臨時常數或硬編碼。

### 2. 常用語意色彩速查
| 屬性名稱 | 預設主題意涵 | 備註 |
|---|---|---|
| `pageBackground` | 頁面底色 | 預設為純黑 `#000000` / 深灰 `#101010` |
| `textPrimary` | 主要文字色彩 | 通常為白色 `#FFFFFF` / 深色柔和白 `#E0E0E0` |
| `textSecondary` | 次要/提示文字色彩 | 通常為 `#808080` (HalfWhite) |
| `textLink` | 連結與功能標籤按鈕色 | 亮青色 `#00FFFF` (BBS 高亮 Cyan) |
| `toolbarBackground` | 底部工具列底色 | BBS 深青色 `#003030` / `#002020` |
| `toolbarDivider` | 工具列按鈕分隔線 | 極深色細線 `#001A1A` / `#001414` |
| `checkboxTint` | 核取方塊勾選底色 | HoloGreen `#99CC00` |
| `checkboxCheckmark` | 核取方塊勾勾符號色 | **純黑色 `#000000`** (嚴禁使用預設白勾) |
| `checkboxUncheckedTint`| 核取方塊未選外框色 | `#808080` (HalfWhite) |
| `inputBoxBackground` | 輸入框實心底色 | 經典淺灰 `#E0E0E0` |
| `inputBoxText` | 輸入框文字與游標色 | 純黑 `#000000` |
| `dialogTitleBackground` | 對話框標題列底色 | 深灰 `#202020` ~ `#303030` |
| `dialogBorder` | 對話框外框線色 | 灰色 `#808080` (HalfWhite) |
| `dialogButtonBackground` | 對話框確認/取消按鈕底色 | 深暗紅 `#400000` (經典 Danger 樣式) |
| `dialogButtonText` | 對話框按鈕文字色 | 純白 `#FFFFFF` |
| `dialogButtonDivider` | 對話框按鈕間垂直細線 | 極深色細線 `#001A1A` |
| `chapterBackground` | 設定頁分類章節列底色 | 深灰 `#202020` (粉紅/電子紙模式依主題變換) |
| `chapterText` | 設定頁分類章節列文字色 | 灰白 `#C0C0C0` (粗體) |
| `titleBarBackground` | 標題列/勇者足跡作者底色 | 經典深海藍 `#000060` (BBS TitleBar) |
| `statusNotice` | 狀態數值/通知警示標示色 | 經典紅 `#800000` / `#C04040` (如線上人數、呼叫器狀態) |
| `classItemName` | 看板列表看板英文名稱 | 淺黃色 `#F0F080` (BBS Yellow) |
| `classItemManager` | 看板列表板主名稱 | 淺紫藍色 `#B0B0F0` (BBS Mail/Board Manager) |

---

## 🧩 通用元件設計規範

### 1. 核取方塊 (`BahaCheckbox` 系列)
- 檔案：`app/src/main/java/com/kota/Bahamut/ui/components/BahaCheckbox.kt`
- 基礎封裝 `BahaCheckbox` 並統一綁定色彩：
  - `checkedColor = colors.checkboxTint` (HoloGreen 綠底)
  - `checkmarkColor = colors.checkboxCheckmark` (**黑色勾號**，嚴禁使用 Material 預設白勾)
  - `uncheckedColor = colors.checkboxUncheckedTint` (HalfWhite 灰色邊框)
- **專案全域 Checkbox 規範收斂為以下兩種標準型態**：
  1. **型態一：`BahaCheckboxLeft`（LoginPage / 對話框式）**
     - 排版：`[Checkbox] [Text]`（Checkbox 在左邊，文字在右邊，整列可點擊切換）。
     - 支援可選之 `trailingContent`（用於右側語法提示、代碼標記等）。
     - 適用場景：`LoginPage`（記住我、Web自動簽到）、對話框系列（`DialogWebLoginSettings`、`DialogPaintColor`、`DialogShortenUrl`、`DialogReference` 等）。
  2. **型態二：`SettingsCheckboxItem`（設定頁清單式）**
     - 排版：`[Text ................. Checkbox]`（文字靠左對齊，Checkbox 靠右對齊，預設帶底部細分隔線，整列可點擊切換）。
     - 適用場景：`SystemSettingsPage`、`UserConfigPage`。
- 嚴禁直接呼叫原生 Material 3 `Checkbox` 造成勾號與邊框跑色。

### 2. 輸入框 (`BahaInputField`)
- 檔案：`app/src/main/java/com/kota/Bahamut/ui/components/BahaInputField.kt`
- 特色：基於 `BasicTextField` 實作的經典實心矩形輸入框（非 Material OutlinedTextField）：
  - 高度固定為 `48.dp` 以上
  - 外觀：`2.dp` 輕微圓角或直角、實心淺灰底色 (`colors.inputBoxBackground`)、純黑文字 (`colors.inputBoxText`)
  - 支援密碼遮罩、最大字數限制與單行輸入。

### 3. 通用文字元件與字級階層 (`BahaText` & `AppFontSize`)
- 檔案：
  - 元件：`app/src/main/java/com/kota/Bahamut/ui/components/BahaText.kt`
  - 規格：`app/src/main/java/com/kota/Bahamut/ui/theme/Theme.kt` (`AppFontSize`)
- **核心設計概念**：
  - **App 基準大小為 24.sp**（對應 `AppTheme.fontSize.base`），預設字色為 `colors.textPrimary`。
  - 杜絕在各 Composable 寫死 `fontSize = 18.sp` 等魔術數字，一律透過 `BahaText(size = ...)` 語意化指定。
  - `AppFontSize` 內建 `scaleFactor: Float = 1.0f`，未來支援使用者在設定中動態縮放字體大小（全域即時響應重組）。
- **字級層級對應表 (`AppTheme.fontSize`)**：
  | 層級列舉 | 基準大小 | 適用場景 |
  |---|---|---|
  | `ULTRA_LARGE` | 28.sp | 特大標題 / 無障礙模式 |
  | `LARGE` | 26.sp | 主選單目錄項目、勇者足跡按鈕 |
  | `BASE` (預設) | 24.sp | 全域標準基準字、終端機標題大字、輸入框文字 |
  | `TITLE` | 20.sp | 區塊標題、重要狀態數值 (如線上人數、BB Call) |
  | `SUBTITLE` | 18.sp | 按鈕文字、輸入框標籤、單選項目文字 |
  | `BODY` | 16.sp | 內文、核取方塊文字、長篇說明第二段 |
  | `CAPTION` | 14.sp | 提示說明、輔助文字、版本號、章節列小字 |
  | `TINY` | 12.sp | 極小徽章、時間戳記、次要數據 |

---

## 🪟 通用對話框底稿規範 (`BahaAlertDialogContent`)

- 檔案：`app/src/main/java/com/kota/Bahamut/ui/dialogs/BahaAlertDialog.kt`
- 參照經典 BBS 設計（例如 Web 登入設定視窗）：
  1. **外框**：
     - 直角無圓角矩形容器（移除一般對話框的圓角卡片感）。
     - 外框線使用 `1.5dp` 的 `colors.dialogBorder`。
     - 底色使用 `colors.pageBackground`。
  2. **標題列 (Title Bar)**：
     - 滿版頂部配置，底色為 `colors.dialogTitleBackground`。
     - 標題文字為 **白色粗體** (`colors.textPrimary`, `18sp`)，**不可使用黃色**。
     - 標題下方附帶 1dp 的橫向分隔線 (`colors.divider`)。
  3. **內容區 (Content Area)**：
     - 左右內距 `16.dp`，垂直內距 `14.dp`。
     - 提示說明文字使用 `colors.textPrimary` (`14sp`, `lineHeight = 20sp`)。
     - 輸入欄位一律使用 `BahaInputField`，標籤使用 `18sp`。
     - 選項一律使用 `BahaCheckbox`。
  4. **底部按鈕列 (Button Bar)**：
     - **滿版無縫貼底**（移除所有外圍 padding）。
     - 高度固定為 `60.dp`。
     - 頂部與內容以 1dp 分隔線 (`colors.dialogButtonDivider`) 區隔。
     - 按鈕採等寬配置 (`Modifier.weight(1f)`)，底色為深暗紅 (`colors.dialogButtonBackground` / `#400000`)。
     - 按鈕文字為粗體白色 (`colors.dialogButtonText`, `18sp`)。
     - 按鈕之間以 1dp 垂直細線 (`colors.dialogButtonDivider`) 分隔。

---

## 🖥️ TelnetView 寬度與排版準則

### 1. 核心需求：「左右對齊視窗寬度就好」
- BBS 終端畫面標準為 80 欄字元。
- `TelnetView` 的寬度**必須剛好對齊裝置視窗寬度**，嚴禁超出螢幕邊界導致左右內容（如狀態列文字、ASCII 圖樣左右翼）被裁切，亦不可兩側留黑邊。

### 2. 測量邏輯 (`TelnetView.kt` 中的 `onMeasure`)
- 當容器傳入確定寬度（`widthMode != UNSPECIFIED`）：
  - 寬度直接以 `viewWidth` 滿版測量。
  - 單字元寬度以 `(viewWidth / 80 / 2 * 2)` 取得整數雙字元規格。
  - 計算 `myScaleX = viewWidth / drawWidth`，確保畫布縮放後 80 欄字元精準吻合裝置寬度。
  - 當 `layout.height == WRAP_CONTENT` 時，高度以自然字元高度測量 (`drawHeight = blockHeight * rowSize`)，`myScaleY` 保持 `1.0f`，絕不可垂直拉伸扭曲。

### 3. Compose 頁面結構標準（適用於 `LoginPage`、`MainPage`）
```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .background(colors.pageBackground)
) {
    // 1. 上半部 Telnet 終端文字/歡迎畫面 (置頂，左右對齊視窗寬度，自然高度)
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        factory = { ctx ->
            TelnetView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                telnetView = this
                setFrameToTelnetView()
            }
        },
        update = { view ->
            telnetView = view
        }
    )

    // 2. 彈性留白 (推擠下方區塊使其沉底對齊工具列)
    Spacer(modifier = Modifier.weight(1f))

    // 3. 功能面板/輸入區塊
    ...

    // 4. 底部滿版工具列
    ...
}
```

---

## 📌 重點頁面與功能實作紀錄

### 1. 勇者登入頁面 (`LoginPage.kt`)
- 上方 `TelnetView` 置頂滿寬，自然高度顯示龍頭 ASCII 動畫。
- 中間彈性留白。
- 下方輸入區塊：
  - Web 自動簽到列：水平置中，包含 `BahaCheckbox`、"Web自動簽到" 標籤，右側保留 `[帳密設定]` 連結文字 (`colors.textLink`)，點擊觸發 `DialogWebLoginSettings().show()`。
  - 帳號與密碼欄位：左側標籤 (`18sp`)，右側 `BahaInputField`。
  - 記住我的資料列：水平置中，包含 `BahaCheckbox` 與標籤。
- 底部工具列：滿版無縫、高度 `50dp`、深青色背景 (`colors.toolbarBackground`)、單一登入按鈕。

### 2. 主選單頁面 (`MainPage.kt`)
- 上方 `TelnetView` 置頂滿寬，自然高度顯示歡迎標語與龍頭截幅。
- 中間彈性留白 (`Spacer(Modifier.weight(1f))`)。
- 狀態列 (三欄式)：
  - 高度 `72dp`，上下帶 1dp 分隔線 (`colors.divider`)，三欄之間**無**垂直分隔線。
  - 線上人數：標籤使用 `13sp` 次要字色，數值使用 `20sp` 粗體警示色 (`colors.statusNotice`)。
  - 呼叫器：標籤使用 `13sp` 次要字色 + 留言圖示，狀態/次數使用 `20sp` 粗體警示色 (`colors.statusNotice`)。
  - 勇者足跡按鈕：使用 `26sp` 粗體白色字 (`colors.buttonText`)，居中。
- 討論區目錄清單 (佈告討論區、分組討論區、我的最愛)：
  - 每項固定高度 `72dp`，文字居中、大小 `26sp`、粗體白色 (`colors.buttonText`)。
  - 項目間以 1dp 分隔線 (`colors.divider`) 區隔。
- 底部工具列：
  - 滿版無縫高度 `50dp`，深青色背景 (`colors.toolbarBackground`)，頂部 1dp 分隔線 (`colors.toolbarDivider`)。
  - 三顆標準按鈕：「登出」、「信箱」、「設定」，均使用 `BahaButton` (Type NORMAL，深青底白字，包含登出按鈕亦維持統一青色)，中間以 1dp 垂直細線 (`colors.toolbarDivider`) 區隔。

### 3. BBS 個人設定頁面 (`SystemSettingsPage.kt`)
- 分類章節列 (`SettingsChapter`)：
  - 底色為深灰條 (`colors.chapterBackground`)，頂底帶 0.5dp 分隔線。
  - 文字為 `17sp` 粗體灰白色 (`colors.chapterText`)，左右對齊邊界。
- 導向項目列 (`SettingsNavigationItem`)：
  - 左側標籤為 `17sp` 主要文字 (`colors.textPrimary`)。
  - 右側箭頭為純文字 `>` (`18sp` 粗體 `colors.textSecondary`)，而非原生 Icon。
  - 底部帶 0.5dp 分隔線。
- 開關選項列 (`SettingsCheckboxItem`)：
  - 左側標籤為 `17sp` 主要文字。
  - 右側核取方塊一律使用 `BahaCheckbox` (綠底黑勾)。
  - 底部帶 0.5dp 分隔線。
- 下拉選單列 (`SettingsSpinnerItem`)：
  - 左側標籤為 `17sp` 主要文字。
  - 右側顯示目前選中項目值 (`16sp`)，附帶下拉小三角形 `▾` (`14sp`)。
  - 底部帶 0.5dp 分隔線。
- 底部工具列：
  - 滿版無縫高度 `50dp`，深青色背景 (`colors.toolbarBackground`)，單一返回按鈕 (`BahaButton`)。

### 4. 黑名單設定頁面 (`BlockListPage.kt`)
- 頂部輸入工具列：
  - 高度 `50dp`，滿版無縫。
  - 左側：「重置」按鈕 (`BahaButton` Type DANGER，紅底白字)。
  - 中間：ID 輸入框，底色為頁面黑底，文字居中 (`18sp`)，提示字「請輸入ID」居中。
  - 右側：「新增」按鈕 (`BahaButton` Type DANGER，紅底白字)。
- 列表項目：
  - 高度 `48dp`，左側 ID 名稱 (`18sp` 白色)。
  - 右側「刪除」按鈕：貼齊右邊界、寬度約 80dp、填滿列高、深青底白字 (`toolbarItemBackground`)。
  - 項目間以 1dp 分隔線區隔。
- 底部工具列：
  - 滿版無縫高度 `50dp`，深青色背景，單一返回按鈕 (`BahaButton`)。

### 5. Web 登入設定視窗 (`DialogWebLoginSettings.kt`)
- 繼承 `ASDialog`，完全採用 `BahaAlertDialogContent` 底稿。
- 欄位包含：
  - 頂部白字說明："Web 帳密與 App 帳密分開，僅儲存於本機，不會同步至雲端。"
  - 帳號輸入：`BahaInputField`
  - 密碼輸入：`BahaInputField` (密碼遮罩)
  - 除錯視窗勾選項：`BahaCheckboxLeft` ("每次登入顯示除錯視窗")
  - 底部直角滿版雙按鈕："取消" 與 "確認" (深紅底白字，中間帶分隔細線)。

### 6. 使用者操作模式設定頁面 (`UserConfigPage.kt`)
- 採用與 `SystemSettingsPage` 一致的規範：
  - 分類章節列：使用 `colors.chapterBackground` 與 `colors.chapterText`。
  - 選項列表：全面採用 `SettingsCheckboxItem`（型態二），取代原私有 `ConfigItem`。
  - 底部工具列：滿版無縫高度 `50dp`，單一「返回」按鈕 (`BahaButton`)。

### 7. 上色對話框 (`DialogPaintColor.kt`)
- 「還原」與「亮色」核取方塊採用 `BahaCheckboxLeft`（型態一）。
- 利用 `trailingContent` 呈現右側語法提示標記（如 `*[m`、`1;`）。

### 8. 短網址對話框 (`DialogShortenUrl.kt`)
- 「去識別化」核取方塊採用 `BahaCheckboxLeft`（型態一），文字靠左邊緣 Checkbox 對齊，整列可點擊。

### 9. 引用對話框 (`DialogReference.kt`)
- 前文作者選項、子選項「去除空白行」、以及「無」選項全面採用 `BahaCheckboxLeft`（型態一）。
- 子選項保留 28dp 左縮排層次感。

---

## 📱 螢幕邊界與 Inset 規範（嚴禁在底部工具列使用 `navigationBarsPadding()`）

> [!CAUTION]
> **嚴禁在 Compose 底部工具列或外層容器使用 `Modifier.navigationBarsPadding()`！**

### 1. 核心成因分析
- 專案主 Activity (`BahamutController`) 採用傳統標準視窗配置，並未啟用全螢幕 Edge-to-Edge（穿透繪製至系統導覽列下方）。
- Android 系統本身已為虛擬導覽列（返回、首頁、多工三鍵或手勢小白條）保留獨立的實體區域（通常為 48dp 黑底）。
- 若在 Compose 底部工具列或其外層 Box/Column 上附加 `navigationBarsPadding()`，Compose 會測量到系統 Inset 並於元件內部額外墊高 48dp。
- 當背景色 (`toolbarBackground`) 套用時，這 48dp 內距會被工具列底色一併填滿，導致原本標準的 `50dp` 工具列暴增至近 `100dp`，視覺上呈現**「兩行高度」且下半部為大片空綠/青色區塊**的排版錯誤。

### 2. 標準底部工具列排版樣板
所有頁面（`StartPage`、`MainPage`、`SystemSettingsPage`、`UserConfigPage`、`UserInfoPage` 等）底部操作列請遵循純粹的固定高度結構：
```kotlin
// 1dp 頂部分隔線
Box(
    modifier = Modifier
        .fillMaxWidth()
        .height(1.dp)
        .background(colors.toolbarDivider)
)
// 60dp 工具列容器
Row(
    modifier = Modifier
        .fillMaxWidth()
        .height(60.dp)
        .background(colors.toolbarBackground),
    verticalAlignment = Alignment.CenterVertically
) {
    BahaButton(
        text = stringResource(R.string.exit),
        onClick = { ... },
        modifier = Modifier.weight(1f).fillMaxHeight()
    )
    ...
}
```


