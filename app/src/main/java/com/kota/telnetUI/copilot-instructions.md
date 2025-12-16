# telnetUI

## 概述
telnetUI 模組提供 Telnet 終端機的使用者介面元件，包含自訂視圖和文字渲染器。

## 主要模組

### textView
Telnet 文字視圖元件，支援多種字體大小：
- `TelnetTextView.kt` - 文字視圖基類
- `TelnetTextViewSmall.kt` - 小字體視圖
- `TelnetTextViewNormal.kt` - 標準字體視圖
- `TelnetTextViewLarge.kt` - 大字體視圖
- `TelnetTextViewUltraLarge.kt` - 超大字體視圖

## 核心元件
- `TelnetPage.kt` - Telnet 頁面
- `TelnetView.kt` - Telnet 視圖
- `TelnetViewDrawer.kt` - 視圖繪製器
- `TelnetHeaderItemView.kt` - 標題項目視圖
- `DividerView.kt` - 分隔線視圖

## 技術特點
- 支援 ANSI 色碼顯示
- 多種字體大小選項
- 高效的畫面渲染
- 自訂繪製邏輯
- 使用 Kotlin 開發

## 渲染流程
1. 接收 Telnet 資料
2. TelnetViewDrawer 處理繪製邏輯
3. TelnetTextView 渲染文字和色碼
4. 顯示終端機畫面
