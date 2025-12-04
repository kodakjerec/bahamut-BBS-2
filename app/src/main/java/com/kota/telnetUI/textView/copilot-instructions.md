# telnetUI/textView

## 概述
textView 模組提供 Telnet 文字視圖元件，支援多種字體大小的終端機文字顯示。

## 主要元件

### TelnetTextView
Telnet 文字視圖基類，提供終端機文字渲染的核心功能

### 字體大小變體
- `TelnetTextViewSmall` - 小字體（適合小螢幕或顯示更多內容）
- `TelnetTextViewNormal` - 標準字體（預設大小）
- `TelnetTextViewLarge` - 大字體（適合較大螢幕）
- `TelnetTextViewUltraLarge` - 超大字體（無障礙模式）

## 功能特點
- ANSI 色碼顯示
- 等寬字體渲染
- Big5 中文顯示
- 游標位置顯示
- 選取文字功能
- 複製貼上支援

## 渲染特性
- 字元精確對齊
- 高效能繪製
- 雙位元組字元處理
- 色碼快取優化

## 使用方式
根據使用者設定或螢幕大小選擇適當的字體大小：
```kotlin
val textView = when (fontSize) {
    FontSize.SMALL -> TelnetTextViewSmall(context)
    FontSize.NORMAL -> TelnetTextViewNormal(context)
    FontSize.LARGE -> TelnetTextViewLarge(context)
    FontSize.ULTRA_LARGE -> TelnetTextViewUltraLarge(context)
}
```

## 技術特點
- 自訂 View 繪製
- Canvas 直接繪製
- 字體快取
- 使用 Kotlin 開發
