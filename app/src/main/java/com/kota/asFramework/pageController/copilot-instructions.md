# asFramework/pageController

## 概述
pageController 模組提供完整的頁面導航和視圖控制器系統，實現類似 iOS UIKit 的頁面管理架構。

## 核心元件

### ASViewController
視圖控制器基類，管理單一頁面的生命週期和視圖
- `ASViewControllerAppearListener` - 頁面出現監聽器
- `ASViewControllerDisappearListener` - 頁面消失監聽器
- `ASViewControllerOperationListener` - 操作監聽器

### ASNavigationController
導航控制器，管理視圖控制器堆疊和頁面切換
- `ASNavigationControllerView` - 導航控制器視圖
- `ASNavigationControllerPushAnimation` - Push 動畫
- `ASNavigationControllerPopAnimation` - Pop 動畫

### ASListViewController
列表視圖控制器，專門處理列表頁面

### 視圖元件
- `ASView` - 視圖基類
- `ASPageView` - 頁面視圖
- `ASGestureView` - 手勢視圖
  - `ASGestureViewDelegate` - 手勢委派

### 動畫系統
- `ASAnimation` - 動畫基類
- `ASAnimationRunner` - 動畫執行器
- `ASPageAnimation` - 頁面動畫

### 輔助元件
- `ASDeviceController` - 裝置控制器
- `ASLinearLayout` - 線性布局
- `ASViewRemover` - 視圖移除器
- `ASWindowStateHandler` - 視窗狀態處理器

## 架構特點
- MVC 架構模式
- 頁面堆疊管理
- 生命週期管理（appear/disappear）
- 自訂頁面切換動畫
- 手勢導航支援

## 使用方式
```kotlin
// 建立視圖控制器
val viewController = MyViewController()

// Push 到導航堆疊
navigationController.pushViewController(viewController, animated = true)

// Pop 回上一頁
navigationController.popViewController(animated = true)
```

## 技術特點
- 類 iOS 的頁面管理架構
- 完整的生命週期回呼
- 靈活的動畫系統
- 使用 Kotlin 開發
