# Bahamut BBS Android - Instructions 文件總覽

本目錄包含 Bahamut BBS Android 客戶端的詳細開發文件，專為 GitHub Copilot 優化。

## 📚 文件結構

### 📋 總覽文件
- [com.kota-structure.md](com.kota-structure.md) - **com.kota 套件完整結構總覽**（第一層+第二層）

---

## 🎯 第一層模組文件（6個）

### 核心模組

| 文件 | 模組 | 說明 | 重要性 |
|------|------|------|--------|
| [asFramework.md](asFramework.md) | asFramework | 應用程式基礎框架 | ⭐⭐⭐⭐⭐ |
| [Bahamut.md](Bahamut.md) | Bahamut | BBS 核心業務邏輯 | ⭐⭐⭐⭐⭐ |
| [telnet.md](telnet.md) | telnet | Telnet 客戶端核心 | ⭐⭐⭐⭐⭐ |
| [telnetUI.md](telnetUI.md) | telnetUI | Telnet UI 元件 | ⭐⭐⭐⭐ |
| [dataPool.md](dataPool.md) | dataPool | 資料緩衝管理 | ⭐⭐⭐ |
| [textEncoder.md](textEncoder.md) | textEncoder | Big5/UTF-8 編碼轉換 | ⭐⭐⭐⭐ |

---

## 🔧 第二層模組文件（17個）

### asFramework 子模組（7個）

| 文件 | 資料夾 | 說明 | 重要性 |
|------|--------|------|--------|
| [asFramework-pageController.md](asFramework-pageController.md) | pageController | **頁面控制器**（iOS 風格導航） | ⭐⭐⭐⭐⭐ |
| [asFramework-thread.md](asFramework-thread.md) | thread | **執行緒管理**（ASRunner 核心） | ⭐⭐⭐⭐⭐ |
| [asFramework-dialog.md](asFramework-dialog.md) | dialog | 對話框系統 | ⭐⭐⭐⭐ |
| [asFramework-ui.md](asFramework-ui.md) | ui | UI 元件庫 | ⭐⭐⭐⭐ |
| [asFramework-model.md](asFramework-model.md) | model | 基礎資料模型 | ⭐⭐⭐ |
| [asFramework-network.md](asFramework-network.md) | network | 網路狀態管理 | ⭐⭐⭐ |
| [asFramework-utils.md](asFramework-utils.md) | utils | 工具類別 | ⭐⭐⭐ |

### Bahamut 子模組（6個）

| 文件 | 資料夾 | 說明 | 重要性 |
|------|--------|------|--------|
| [Bahamut-listPage.md](Bahamut-listPage.md) | listPage | **列表頁面基礎架構**（20項/區塊） | ⭐⭐⭐⭐⭐ |
| [Bahamut-command.md](Bahamut-command.md) | command | BBS 命令系統 | ⭐⭐⭐⭐⭐ |
| [Bahamut-pages.md](Bahamut-pages.md) | pages | 業務頁面集合 | ⭐⭐⭐⭐ |
| [Bahamut-dialogs.md](Bahamut-dialogs.md) | dialogs | 業務對話框 | ⭐⭐⭐⭐ |
| [Bahamut-dataModels.md](Bahamut-dataModels.md) | dataModels | 資料模型與本地儲存 | ⭐⭐⭐⭐ |
| [Bahamut-service.md](Bahamut-service.md) | service | 背景服務與設定 | ⭐⭐⭐ |

### telnet 子模組（3個）

| 文件 | 資料夾 | 說明 | 重要性 |
|------|--------|------|--------|
| [telnet-model.md](telnet-model.md) | model | **Telnet 資料模型**（TelnetFrame） | ⭐⭐⭐⭐⭐ |
| [telnet-reference.md](telnet-reference.md) | reference | **ANSI/Telnet 規範**（色碼、鍵盤） | ⭐⭐⭐⭐⭐ |
| [telnet-logic.md](telnet-logic.md) | logic | 業務邏輯處理 | ⭐⭐⭐⭐ |

### telnetUI 子模組（1個）

| 文件 | 資料夾 | 說明 | 重要性 |
|------|--------|------|--------|
| [telnetUI-textView.md](telnetUI-textView.md) | textView | **文字視圖元件**（多字體大小） | ⭐⭐⭐⭐⭐ |

---

## 🎯 快速導航：我要...

### 修改 UI 相關
- 修改頁面導航 → [asFramework-pageController.md](asFramework-pageController.md)
- 修改對話框 → [asFramework-dialog.md](asFramework-dialog.md) 或 [Bahamut-dialogs.md](Bahamut-dialogs.md)
- 修改列表元件 → [asFramework-ui.md](asFramework-ui.md)
- 修改終端機顯示 → [telnetUI-textView.md](telnetUI-textView.md)

### 修改業務邏輯
- 修改文章列表 → [Bahamut-listPage.md](Bahamut-listPage.md)
- 新增 BBS 命令 → [Bahamut-command.md](Bahamut-command.md)
- 修改頁面功能 → [Bahamut-pages.md](Bahamut-pages.md)
- 修改本地資料 → [Bahamut-dataModels.md](Bahamut-dataModels.md)

### 修改底層功能
- 修改執行緒邏輯 → [asFramework-thread.md](asFramework-thread.md) ⚠️ 核心
- 修改 Telnet 連接 → [telnet.md](telnet.md)
- 修改 ANSI 解析 → [telnet-model.md](telnet-model.md) + [telnet-reference.md](telnet-reference.md)
- 修改編碼轉換 → [textEncoder.md](textEncoder.md)
- 修改資料緩衝 → [dataPool.md](dataPool.md)

---

## ⚡ 關鍵設計模式速查

### 執行緒管理（所有 UI 更新）
```kotlin
// ✅ 正確：使用 ASRunner
object : ASRunner() {
    override fun run() {
        // UI 更新代碼
    }
}.runInMainThread()
```
📖 詳見: [asFramework-thread.md](asFramework-thread.md)

### 區塊分頁載入（所有列表）
```kotlin
// 20 項/區塊
val blockIndex = itemIndex / 20
val indexInBlock = itemIndex % 20
```
📖 詳見: [Bahamut-listPage.md](Bahamut-listPage.md)

### 物件池模式（效能優化）
```kotlin
companion object {
    private val _pool = Stack<Item>()
    fun create(): Item { /* 從池取出 */ }
    fun recycle(item: Item) { /* 回收 */ }
}
```
📖 詳見: [Bahamut-listPage.md](Bahamut-listPage.md)

### 命令模式（BBS 操作）
```kotlin
class MyCommand : TelnetCommand() {
    override fun execute(page: TelnetListPage) { /* 執行 */ }
    override fun executeFinished(...) { /* 回呼 */ }
}
```
📖 詳見: [Bahamut-command.md](Bahamut-command.md)

### 編碼轉換（Big5 ↔ UTF-8）
```kotlin
// 接收：Big5 → UTF-8
val utf8Text = B2UEncoder.convert(big5Data)

// 發送：UTF-8 → Big5
val big5Data = U2BEncoder.convert(utf8Text)
```
📖 詳見: [textEncoder.md](textEncoder.md)

---

## 🐛 常見問題速查

### ListView 崩潰
**症狀**: `IllegalStateException` 在 adapter 更新時  
**原因**: 多次調用 `notifyDataSetChanged()`  
**解決**: 只調用一次 `safeNotifyDataSetChanged()` 並包裝在 `ASRunner`  
📖 詳見: [Bahamut-listPage.md](Bahamut-listPage.md) 第 350-380 行

### 執行緒崩潰
**症狀**: `CalledFromWrongThreadException`  
**原因**: 在背景執行緒更新 UI  
**解決**: 使用 `ASRunner().runInMainThread()`  
📖 詳見: [asFramework-thread.md](asFramework-thread.md)

### 中文亂碼
**症狀**: 中文顯示為問號或方塊  
**原因**: Big5 雙位元組字元被截斷  
**解決**: 檢查字元邊界，使用 `TextConverterBuffer`  
📖 詳見: [textEncoder.md](textEncoder.md) 第 250-290 行

### 記憶體洩漏
**症狀**: 長時間使用後應用變慢  
**原因**: 未回收物件池的項目  
**解決**: 移除區塊時調用 `recycleItem()` / `recycleBlock()`  
📖 詳見: [Bahamut-listPage.md](Bahamut-listPage.md) 第 180-220 行

### ANSI 色碼錯誤
**症狀**: 文字顏色顯示錯誤  
**原因**: ANSI 序列解析錯誤  
**解決**: 檢查 `TelnetAnsi` 解析邏輯  
📖 詳見: [telnet-reference.md](telnet-reference.md)

---

## 📋 文件使用指南

### applyto 機制
每個文件都有 `applyto` 標記，告訴 Copilot 該文件適用於哪些檔案：

```markdown
**applyto**: `app/src/main/java/com/kota/Bahamut/listPage/**/*.kt`
```

當你修改 `listPage` 資料夾下的任何 `.kt` 檔案時，Copilot 會自動參考 `Bahamut-listPage.md`。

### 文件結構
所有文件都遵循統一結構：
1. **模組概述** - 快速了解模組功能
2. **核心類別** - 主要類別說明和程式碼
3. **使用模式** - 實際使用範例
4. **常見問題** - 問題排查和解決方案
5. **開發規範** - 編碼規範和最佳實踐
6. **模組關係** - 與其他模組的依賴關係

### 閱讀建議

**新手開發者**:
1. 先讀 [com.kota-structure.md](com.kota-structure.md) 了解整體架構
2. 再讀 [asFramework.md](asFramework.md) 了解基礎框架
3. 然後讀 [Bahamut.md](Bahamut.md) 了解業務邏輯
4. 最後根據需要閱讀第二層文件

**有經驗的開發者**:
- 直接使用「快速導航」找到需要的文件
- 使用「常見問題速查」快速解決問題
- 使用「關鍵設計模式速查」參考最佳實踐

---

## 🔄 文件更新

**最後更新**: 2025-12-11  
**文件總數**: 24 個（1 總覽 + 6 第一層 + 17 第二層）  
**涵蓋範圍**: com.kota 套件完整結構

**更新原則**:
- 模組結構變更時更新
- 新增重要設計模式時更新
- 發現重大問題時更新
- 每個重大版本發布前更新

---

## 💡 貢獻指南

如果你發現：
- 文件內容有誤
- 缺少重要資訊
- 範例代碼過時
- 需要新增常見問題

請提交 PR 或 Issue 更新文件。

---

**維護者**: Bahamut BBS 開發團隊  
**授權**: 內部開發文件  
**版本**: 1.0.0
