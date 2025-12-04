# textEncoder

## 概述
textEncoder 模組提供 Big5 和 UTF-8 編碼之間的轉換功能，用於處理 BBS 的 Big5 編碼資料。

## 主要元件

### B2UEncoder
Big5 轉 UTF-8 編碼器，將 BBS 的 Big5 編碼文字轉換為 UTF-8

### U2BEncoder
UTF-8 轉 Big5 編碼器，將 UTF-8 文字轉換為 Big5 編碼以傳送到 BBS

### TextConverterBuffer
文字轉換緩衝區，提供高效的編碼轉換功能

## 使用場景
- BBS 資料接收時將 Big5 轉為 UTF-8 顯示
- 使用者輸入時將 UTF-8 轉為 Big5 傳送
- 文章內容編碼處理

## 技術特點
- 支援繁體中文 Big5 編碼
- 高效的編碼轉換
- 緩衝區管理
- 使用 Kotlin 開發

## 注意事項
- Big5 為雙位元組編碼，需正確處理字元邊界
- 某些特殊字元可能需要額外處理
- 注意編碼轉換時的記憶體管理
