# asFramework

## 概述
asFramework 是一個自訂的應用程式框架，提供 UI、對話框、網路、執行緒管理等基礎功能。

## 主要模組

### dialog
對話框系統，包含各種對話框元件（警告框、列表對話框、處理中對話框等）

### model
基礎資料模型，包含點（ASPoint）和尺寸（ASSize）等幾何模型

### network
網路狀態監控和管理功能

### pageController
頁面導航和視圖控制器系統，處理頁面切換、動畫和生命週期管理

### thread
執行緒管理和非同步任務執行器

### ui
UI 元件庫，包含自訂的列表視圖、捲動視圖、手勢處理等

### utils
工具類別，包含串流讀寫器和型別轉換器

## 架構特點
- 採用自訂的視圖控制器架構（ASViewController）
- 提供完整的頁面導航系統（ASNavigationController）
- 支援自訂手勢和動畫效果
- 使用 Kotlin 開發
