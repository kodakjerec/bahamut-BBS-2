# telnet

## 概述
telnet 模組實現完整的 Telnet 客戶端功能，支援傳統 Socket 和 WebSocket 連接，處理 ANSI 轉義序列和 BBS 顯示邏輯。

## 主要模組

### logic
業務邏輯處理：
- `ArticleHandler.kt` - 文章處理邏輯
- `ClassMode.kt` / `ClassStep.kt` - 分類模式和步驟
- `ItemUtils.kt` - 項目工具函式
- `SearchBoardHandler.kt` - 看板搜尋處理

### model
Telnet 資料模型：
- `TelnetData.kt` - Telnet 資料
- `TelnetFrame.kt` - 畫面幀
- `TelnetModel.kt` - Telnet 模型
- `TelnetRow.kt` - 終端機列資料
- `BitSpaceType.kt` - 位元空間類型

### reference
參考定義和常數：
- `TelnetAnsiCode.kt` - ANSI 轉義碼定義
- `TelnetAsciiCode.kt` - ASCII 碼定義
- `TelnetDef.kt` - Telnet 通用定義
- `TelnetKeyboard.kt` - 鍵盤輸入定義

## 核心元件
- `TelnetClient.kt` - Telnet 客戶端主類別
- `TelnetConnector.kt` - 連接管理器
- `TelnetChannel.kt` - 通道介面
- `TelnetDefaultSocketChannel.kt` - 傳統 Socket 實現
- `TelnetWebSocketChannel.kt` - WebSocket 實現
- `TelnetReceiver.kt` - 資料接收器
- `TelnetAnsi.kt` - ANSI 解析器
- `TelnetArticle.kt` / `TelnetArticleItem.kt` - 文章相關類別
- `TelnetStateHandler.kt` - 狀態處理器
- `TelnetUtils.kt` - 工具函式

## 技術特點
- 支援雙連接模式（Socket / WebSocket）
- 完整的 ANSI 轉義序列解析
- 終端機畫面模擬
- 非同步資料接收
- 事件驅動架構
- 使用 Kotlin 開發

## 連接流程
1. TelnetClient 初始化
2. 建立 TelnetChannel（Socket 或 WebSocket）
3. TelnetReceiver 接收資料
4. TelnetAnsi 解析 ANSI 碼
5. 更新 TelnetFrame 畫面
6. 通知 TelnetClientListener
