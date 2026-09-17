# telnetUI

## 概述
telnetUI 模組提供 Telnet 終端機的使用者介面元件，包含自訂視圖和文字渲染器。

## 核心元件
- `TelnetPage.kt` - Telnet 頁面基類
- `TelnetComposePage.kt` - Jetpack Compose 頁面基類
- `TelnetView.kt` - Telnet 終端機視圖
- `TelnetViewDrawer.kt` - 視圖繪製器

## 技術特點
- 支援 ANSI 色碼顯示
- 多種字體大小選項
- 高效的畫面渲染
- 自訂繪製邏輯
- 使用 Kotlin 開發

## 渲染流程
1. 接收 Telnet 資料
2. TelnetViewDrawer 處理繪製邏輯
3. TelnetView / Compose 渲染文字與畫面
4. 顯示終端機畫面
