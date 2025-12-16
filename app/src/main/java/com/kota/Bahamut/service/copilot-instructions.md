# Bahamut/service

## 概述
service 模組提供背景服務、資料加密、設定管理、雲端備份等核心功能。

## 主要元件

### 背景服務
- `BahaBBSBackgroundService` - BBS 背景服務，處理推播通知等功能

### 加密和安全
- `AESCrypt` - AES 加密工具
- `AhoCorasick` - Aho-Corasick 字串匹配演算法（關鍵字過濾）

### 設定管理
- `UserSettings` - 使用者設定
- `TempSettings` - 暫存設定
- `NotificationSettings` - 通知設定

### 雲端備份
- `CloudBackup` - 雲端備份功能
  - `CloudBackupListener` - 備份監聽器

### 付費系統
- `MyBillingClient` - Google Play 付費客戶端

### 通用函式
- `CommonFunctions` - 通用工具函式

## 功能
- 資料加密和解密
- 使用者偏好設定管理
- 雲端資料備份和還原
- 背景推播服務
- 關鍵字過濾
- 應用程式內購

## 技術特點
- 背景服務管理
- 安全的資料加密
- Google Play Billing API
- 使用 Kotlin 開發

## 注意事項
- AES 加密用於敏感資料保護
- 雲端備份需要網路權限
- 背景服務需要適當的生命週期管理
