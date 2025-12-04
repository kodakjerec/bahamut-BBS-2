# asFramework/thread

## 概述
thread 模組提供執行緒管理和非同步任務執行功能。

## 主要元件

### ASRunner
非同步任務執行器，提供在背景執行緒執行任務並在主執行緒回呼的功能

## 功能
- 背景任務執行
- 主執行緒回呼
- 執行緒管理

## 使用方式
```kotlin
ASRunner.run {
    // 背景任務
    doBackgroundWork()
} then {
    // 主執行緒回呼
    updateUI()
}
```

## 技術特點
- 簡化的非同步 API
- 自動執行緒切換
- 使用 Kotlin 開發
