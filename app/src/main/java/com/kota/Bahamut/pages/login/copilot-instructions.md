# Bahamut/pages/login

## 概述
login 模組提供 BBS 登入功能，支援傳統 Telnet 登入和 WebView 登入。

## 主要元件

### LoginPage
登入頁面主類別，處理使用者登入流程

### LoginWeb
WebView 登入，使用網頁版登入介面

### LoginWebDebugView
WebView 除錯視圖，用於開發時除錯網頁登入

### WebAutoSignInManager
Web 自動簽到與跨日登入管理器，協調 Web 自動簽到生命週期、換日排程與重疊狀態管理。

#### Web 自動簽到規則
1. 每次登入成功執行 Web 登入；回到 startPage 或斷線時停止。
2. 三條件檢核：只有當「Web自動簽到」、「Web帳號」、「Web密碼」三者同時存在時，才執行背景自動登入；若未同時具備，則自動開啟 DebugView 供使用者在 WebView 頁面中手動登入操作。
3. Web 帳號、密碼與自動簽到開關為本機 App 內部變數，由 `UserSettings` 儲存，**不進行雲端備份同步**（在 `CloudBackup` 的 `backup` 與 `restore` 中均予以排除）。
4. 保留最近一次 Web 登入，取消舊的進行中 Web 登入。
5. 跨日登入保留（換日每天只執行一次，避免累積）；若跨日登入與 Web 登入重疊，則保留跨日登入。
6. 使用者可透過 `DialogWebLoginSettings` 設定 Web 專用帳號密碼與除錯視窗開關。

## 登入流程
1. 輸入帳號密碼
2. 連接 BBS 伺服器
3. 自動輸入帳號密碼
4. 處理驗證碼（如需要）
5. 登入成功後進入主頁

## 登入方式
- **Telnet 登入**：傳統終端機登入方式
- **Web 登入**：透過 WebView 使用網頁版登入

## 功能特點
- 記住帳號密碼
- 自動登入
- 多帳號管理
- 登入狀態檢查
- 錯誤處理

## 安全性
- 密碼加密儲存
- 安全的連接方式
- 自動登出機制

## 技術特點
- Telnet 自動化
- WebView 整合
- 使用 Kotlin 開發
