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
