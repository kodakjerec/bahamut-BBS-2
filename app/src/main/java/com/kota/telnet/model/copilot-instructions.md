# telnet/model

## 概述
model 模組提供 Telnet 資料模型，定義終端機畫面的資料結構。

## 主要元件

### TelnetModel
Telnet 模型，表示完整的終端機狀態

### TelnetFrame
終端機畫面幀，表示一個完整的終端機畫面（24x80 字元）

### TelnetRow
終端機列資料，表示畫面中的一列文字和屬性

### TelnetData
Telnet 資料封裝，包含字元和顏色資訊

### BitSpaceType
位元空間類型，定義字元佔用的空間類型（單位元組、雙位元組等）

## 資料結構
```
TelnetModel
  └── TelnetFrame (24列)
        └── TelnetRow[] (每列)
              └── TelnetData[] (每個字元)
                    ├── 字元內容
                    ├── 前景色
                    ├── 背景色
                    └── 屬性（粗體、閃爍等）
```

## 功能
- 儲存終端機畫面狀態
- 處理 ANSI 色碼
- 支援雙位元組字元（中文）
- 畫面差異比對

## 技術特點
- 記憶體高效的資料結構
- 支援 Big5 編碼
- ANSI 屬性管理
- 使用 Kotlin 開發
