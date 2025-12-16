# asFramework/dialog

## 概述
dialog 模組提供完整的對話框系統，包含各種類型的對話框元件和互動處理。

## 主要元件

### ASDialog
對話框基類，提供對話框的基本功能和介面

### ASAlertDialog
警告對話框，用於顯示訊息和確認操作
- `ASAlertDialogListener` - 警告對話框監聽器

### ASListDialog
列表對話框，顯示可選擇的列表項目
- `ASListDialogItemClickListener` - 項目點擊監聽器
- `ASListDialogExtendedItemClickListener` - 擴充項目點擊監聽器

### ASProcessingDialog
處理中對話框，顯示載入或處理狀態
- `ASProcessingDialogOnBackDelegate` - 返回鍵處理委派

### 輔助類別
- `ASDialogOnBackPressedDelegate` - 返回鍵按下委派
- `ASLayoutParams` - 版面配置參數

## 使用方式
```kotlin
// 顯示警告對話框
ASAlertDialog.show(context, title, message, listener)

// 顯示列表對話框
ASListDialog.show(context, items, clickListener)

// 顯示處理中對話框
ASProcessingDialog.show(context, message)
```

## 技術特點
- 統一的對話框介面
- 支援自訂監聽器
- 靈活的版面配置
- 使用 Kotlin 開發
