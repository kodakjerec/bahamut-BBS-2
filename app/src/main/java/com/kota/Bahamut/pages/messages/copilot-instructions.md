# Bahamut/pages/messages

## 概述
messages 模組實現站內即時訊息系統，提供聊天和訊息管理功能。

## 主要元件

### MessageMain
訊息主頁面，顯示聊天列表或聊天內容

### MessageSub
訊息子頁面，顯示單一對話

### 資料模型
- `BahaMessage` - 訊息資料模型
- `BahaMessageSummarize` - 訊息摘要
- `MessageMainListItemStructure` - 列表項目結構

### 小視窗
- `MessageSmall` - 小訊息視窗（浮動視窗）

### Adapter 和 ViewHolder
- `MessageMainListAdapter` - 主列表適配器
- `MessageMainListItem` - 主列表項目
- `MessageMainChatAdapter` - 聊天適配器
- `MessageMainChatItem` - 聊天項目
- `MessageSubAdapter` - 子頁面適配器
- `MessageSubReceive` - 接收訊息項目
- `MessageSubSend` - 發送訊息項目

### 資料庫
- `MessageDatabase` - 訊息資料庫，本地儲存訊息記錄

## 功能特點
- 即時訊息收發
- 聊天列表
- 訊息通知
- 訊息歷史記錄
- 浮動聊天視窗
- 未讀訊息提醒

## 訊息類型
- 一對一聊天
- 系統通知
- 廣播訊息

## 技術特點
- 本地資料庫儲存
- 即時訊息推送
- 浮動視窗功能
- 使用 Kotlin 開發
