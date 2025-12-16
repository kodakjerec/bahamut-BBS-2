# Bahamut/command

## 概述
command 模組實現所有 BBS 操作命令，使用命令模式封裝 Telnet 指令。

## 主要元件

### BahamutCommandDef
命令定義和常數

### 文章命令
- `BahamutCommandListArticle` - 列出文章列表
- `BahamutCommandLoadArticle` - 載入文章內容
- `BahamutCommandLoadArticleEnd` - 載入到文章結尾
- `BahamutCommandLoadArticleEndForSearch` - 搜尋模式載入到結尾
- `BahamutCommandLoadMoreArticle` - 載入更多文章
- `BahamutCommandPostArticle` - 發表文章
- `BahamutCommandEditArticle` - 編輯文章
- `BahamutCommandDeleteArticle` - 刪除文章
- `BahamutCommandGoodArticle` - 標記好文
- `BahamutCommandPushArticle` - 推文
- `BahamutCommandSearchArticle` - 搜尋文章

### 區塊命令
- `BahamutCommandLoadBlock` - 載入區塊
- `BahamutCommandLoadFirstBlock` - 載入第一個區塊
- `BahamutCommandLoadLastBlock` - 載入最後區塊
- `BahamutCommandMoveToLastBlock` - 移動到最後區塊

### 導航命令
- `BahamutCommandTheSameTitleTop` - 同標題到最上
- `BahamutCommandTheSameTitleUp` - 同標題向上
- `BahamutCommandTheSameTitleDown` - 同標題向下
- `BahamutCommandTheSameTitleBottom` - 同標題到最下

### 信件命令
- `BahamutCommandSendMail` - 寄信
- `BahamutCommandFSendMail` - 轉寄信件

### TelnetCommand
命令基類，所有命令繼承此類別

## 命令模式
每個命令封裝一系列 Telnet 操作：
1. 發送按鍵序列
2. 等待回應
3. 解析結果
4. 回呼通知

## 技術特點
- 命令模式設計
- 非同步執行
- 錯誤處理
- 使用 Kotlin 開發
