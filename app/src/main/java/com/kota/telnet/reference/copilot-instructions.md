# telnet/reference

## 概述
reference 模組提供 Telnet 相關的常數定義和參考資料，包含 ANSI 碼、ASCII 碼、鍵盤定義等。

## 主要元件

### TelnetAnsiCode
ANSI 轉義碼定義，包含：
- 色碼定義（前景色、背景色）
- 游標控制碼
- 畫面清除碼
- 文字屬性碼（粗體、閃爍、底線等）

### TelnetAsciiCode
ASCII 碼定義，包含：
- 控制字元（CR、LF、ESC、BS 等）
- 特殊字元常數

### TelnetDef
Telnet 通用定義，包含：
- 畫面尺寸常數（列數、行數）
- Telnet 協定常數
- BBS 特定定義

### TelnetKeyboard
鍵盤輸入定義，包含：
- 方向鍵（上、下、左、右）
- 功能鍵（Enter、Backspace、Delete 等）
- 特殊按鍵組合
- BBS 快速鍵對應

## 使用場景
- ANSI 碼解析
- 鍵盤輸入轉換
- 終端機顯示控制
- BBS 命令輸入

## 技術特點
- 集中管理常數定義
- 清晰的命名規範
- 完整的 ANSI 支援
- 使用 Kotlin 開發
