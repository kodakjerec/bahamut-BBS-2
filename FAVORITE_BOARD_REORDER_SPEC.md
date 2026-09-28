# ClassPage 重構與「我的最愛」次序移動（大 M）AI 實作指引手冊

> **本手冊提供給承接實作之 AI Assistant。**  
> 請依循本文件的模組架構、職責定義、介面規格與步驟指引，精準完成代碼實作。

---

## 1. 任務概述 (Mission Overview)

### 1.1 背景與問題
現有的 [`ClassPage`](file:///D:/Projects/bahamut-BBS-2/app/src/main/java/com/kota/Bahamut/pages/ClassPage.kt) 繼承自舊式 [`TelnetListPage`](file:///D:/Projects/bahamut-BBS-2/app/src/main/java/com/kota/Bahamut/listPage/TelnetListPage.kt)（基於 `ListView`），且散落在 `pages/` 根目錄下。此外，長按手勢原本綁定了「移出我的最愛 (d)」，若要再做「長按拖曳次序 (大 M)」會產生嚴重的手勢重疊。

### 1.2 核心目標
1. **模組化**：在 `app/src/main/java/com/kota/Bahamut/pages/classPage/` 建立專屬目錄。
2. **架構現代化**：比照 [`BookmarkManagePage`](file:///D:/Projects/bahamut-BBS-2/app/src/main/java/com/kota/Bahamut/pages/bookmarkPage/BookmarkManagePage.kt)，將底層替換為 **`RecyclerView` + `ItemTouchHelper` + `Adapter` / `ViewHolder`** 架構。
3. **職責分離**：
   - **移出我的最愛 (d)**：每列右側配置獨立的「刪除按鈕」（點擊觸發）。
   - **移動看板次序 (M)**：長按 100% 專注於 **手勢拖曳排序 (Drag & Drop)**。
4. **協定對齊**：拖曳放開後精準送出 BBS 終端移動指令：`$fromIndex\nM$toIndex\n`。

---

## 2. 檔案目錄與改動清單 (File Changes)

### 2.1 新增/遷移檔案（`pages/classPage/` 套件）
| 檔案路徑 | 類型 | 職責定義 |
| :--- | :--- | :--- |
| `app/src/main/java/com/kota/Bahamut/pages/classPage/ClassPageClickListener.kt` | 介面 | 定義項目點擊 (`onItemClick`) 與刪除按鈕點擊 (`onDeleteClick`) 回呼。 |
| `app/src/main/java/com/kota/Bahamut/pages/classPage/ClassPageViewHolder.kt` | 類別 | 快取列表列 View 元件，處理「我的最愛」模式下刪除按鈕的顯示與事件分發。 |
| `app/src/main/java/com/kota/Bahamut/pages/classPage/ClassPageAdapter.kt` | 類別 | `RecyclerView.Adapter`，維護看板清單資料集合，管理視圖建立與綁定。 |
| `app/src/main/java/com/kota/Bahamut/pages/classPage/ClassPage.kt` | 類別 | 頁面主控制器（繼承 `TelnetPage`），管理 `ItemTouchHelper` 拖曳、BBS 封包收發與生命週期。 |

### 2.2 佈局與現有檔案調整
| 檔案路徑 | 異動重點指引 |
| :--- | :--- |
| `app/src/main/res/layout/class_page.xml` | 將內部的 `com.kota.asFramework.ui.ASListView` 替換為 `androidx.recyclerview.widget.RecyclerView`。 |
| `app/src/main/res/layout/class_page_item_view.xml` | 在右側加入刪除按鈕（ID: `ClassPage_ItemView_DeleteButton`，樣式使用 `@style/ToolbarItem.Danger`）。 |
| `app/src/main/java/com/kota/Bahamut/PageContainer.kt` | 更新 `ClassPage` 的 import 套件路徑至 `com.kota.Bahamut.pages.classPage.ClassPage`。 |
| `app/src/main/java/com/kota/Bahamut/pages/MainPage.kt` | 更新 `ClassPage` 引用。 |

---

## 3. 步驟化實作指引 (Step-by-Step Implementation Guide)

### 步驟 1：建立事件監聽介面 `ClassPageClickListener.kt`
* **套件路徑**：`com.kota.Bahamut.pages.classPage`
* **介面規格**：
  - `fun onItemClick(view: View?, position: Int)`：點擊看板列時觸發（進入看板或子目錄）。
  - `fun onDeleteClick(view: View?, position: Int)`：點擊右側刪除按鈕時觸發（移出我的最愛）。

---

### 步驟 2：調整列表單元佈局 `class_page_item_view.xml`
* **佈局規格指引**：
  - 根元件為水平 `LinearLayout`。
  - 左側為主資訊區塊（權重 `layout_weight="1"`），包含：
    - `ClassPage_ItemView_classTitle` (`TextViewNormal`，看板中文標題)。
    - `ClassPage_ItemView_className` (`TextViewSmall`，看板英文名)。
    - `ClassPage_ItemView_classManager` (`TextViewSmall`，板主群)。
  - 右側配置兩個互斥的視圖：
    1. **一般模式**：引入 `@layout/right_arrow_item_view`（ID: `ListItem_ArrowView`）。
    2. **我的最愛模式**：新增 `Button`（ID: `ClassPage_ItemView_DeleteButton`）：
       - 樣式：`style="@style/ToolbarItem.Danger"`。
       - 寬高：寬度 `60dp`，高度 `match_parent`。
       - 文字：`@string/delete_short`（刪除），文字大小 `14sp`。
       - 預設可見度：`View.GONE`。

---

### 步驟 3：實作 `ClassPageViewHolder.kt`
* **繼承與介面**：繼承 `RecyclerView.ViewHolder`，實作 `View.OnClickListener`。
* **元件快取 (findViewById)**：
  - `classTitle`、`className`、`classManager`
  - `arrowView` (`ListItem_ArrowView`)
  - `deleteButton` (`ClassPage_ItemView_DeleteButton`)
  - `backgroundView` (`ClassPage_ItemView_backgroundView`)
* **核心方法：`setItem(item: ClassPageItem?, isFavoriteMode: Boolean)`**：
  - 將 `item` 的 `title`、`name`、`manager` 填入對應 TextView。
  - 依據 `isFavoriteMode` 切換右側按鈕：
    - 若 `isFavoriteMode == true`：顯示 `deleteButton`，隱藏 `arrowView`。
    - 若 `isFavoriteMode == false`：隱藏 `deleteButton`，顯示 `arrowView`。
* **點擊分發 (`onClick`)**：
  - 取得 `pos = bindingAdapterPosition`（需防護 `NO_POSITION`）。
  - 若點擊 `ClassPage_ItemView_DeleteButton` -> 轉發給 `mListener?.onDeleteClick(view, pos)`。
  - 其餘點擊 -> 轉發給 `mListener?.onItemClick(view, pos)`。

---

### 步驟 4：實作 `ClassPageAdapter.kt`
* **繼承**：`RecyclerView.Adapter<ClassPageViewHolder>()`
* **建構參數**：
  - `private val items: MutableList<ClassPageItem>`
  - `var isFavoriteMode: Boolean = false`
* **實作要點**：
  - `onCreateViewHolder`：加載 `R.layout.class_page_item_view`，實例化 `ClassPageViewHolder` 並傳入監聽器。
  - `onBindViewHolder`：取得對應 position 的項目，呼叫 `holder.setItem(item, isFavoriteMode)`。
  - `getItem(position: Int): ClassPageItem?`：提供安全取值方法。
  - `setOnItemClickListener(listener: ClassPageClickListener?)`：設定點擊事件代理。

---

### 步驟 5：調整主佈局 `class_page.xml`
* **佈局規格指引**：
  - 頂部保留 `com.kota.telnetUI.TelnetHeaderItemView`（ID: `ClassPage_headerView`）。
  - 將原 `<com.kota.asFramework.ui.ASListView android:id="@+id/ClassPage_listView"` 替換為：
    ```xml
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/ClassPage_recyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scrollbars="vertical" />
    ```
  - 保留空列表提示元件 `ClassPage_listEmptyView`。
  - 底部保留工具列容器 `toolbar_container`（含搜尋、第一頁、最後頁按鈕）。

---

### 步驟 6：實作主控制器 `ClassPage.kt`

#### 6.1 類別定義與屬性
* **宣告**：
  ```kotlin
  class ClassPage : TelnetPage(), View.OnClickListener, ClassPageClickListener, DialogSearchBoardListener
  ```
* **關鍵屬性**：
  - `lateinit var recyclerView: RecyclerView`
  - `var adapter: ClassPageAdapter? = null`
  - `val boardItems: MutableList<ClassPageItem> = Vector()`
  - `var listName: String = ""`（判斷當前模式：`listName == "Favorite"` 時為「我的最愛」）

#### 6.2 頂部導覽列提示指引 (`onPageRefresh`)
* 呼叫 `headerView.setData(title, detail, headerDetail2)`：
  - `title`：看板標題（或系統通知提示）。
  - `detail`："看板列表"。
  - `headerDetail2`：**唯有在 `listName == "Favorite"` 時傳入 `"長按可移動位置"`，其餘模式傳入 `""`**（完全對齊 `BookmarkManagePage` 規格）。
* 同步更新 `adapter?.isFavoriteMode = (listName == "Favorite")` 並呼叫 `adapter?.notifyDataSetChanged()`。

#### 6.3 手勢拖曳排序核心 (`ItemTouchHelper`)
* 實作 `ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0)`：
  1. **`onMove(recyclerView, viewHolder, target)`**：
     - 取得起始位置 `from = viewHolder.bindingAdapterPosition` 與目標位置 `to = target.bindingAdapterPosition`。
     - 記錄最後拖曳目標位置 `endDragPos = to`。
     - **條件保護**：僅在 `listName == "Favorite"` 時執行：
       `Collections.swap(boardItems, from, to)` 並調用 `adapter?.notifyItemMoved(from, to)`。
  2. **`onSelectedChanged(viewHolder, actionState)`**：
     - 若 `actionState == ItemTouchHelper.ACTION_STATE_DRAG`：
       記錄初始位置 `startDragPos = viewHolder.bindingAdapterPosition`，將拖曳列背景設為高亮（如 `R.color.ripple_material`）。
     - 若 `actionState == ItemTouchHelper.ACTION_STATE_IDLE`：
       還原拖曳列背景。
       若 `startDragPos != -1 && endDragPos != -1 && startDragPos != endDragPos`：
       計算 1-based 索引：`fromIndex = startDragPos + 1`、`toIndex = endDragPos + 1`。
       呼叫發送指令方法 `sendBbsMoveOrderCommand(fromIndex, toIndex)`。
       重置位置暫存變數。
  3. **綁定**：在 `onPageDidLoad()` 執行 `itemTouchHelper.attachToRecyclerView(recyclerView)`。

#### 6.4 點擊進入看板與刪除按鈕處理
* **`onItemClick(view, position)`**：
  - 取得 `item = adapter?.getItem(position)`。
  - 若 `item.isDirectory` 為目錄 -> 呼叫 `PageContainer.instance!!.pushClassPage(item.name, item.title)` 並切換頁面。
  - 若為一般看板 -> 送出進入看板指令：`TelnetClient.myInstance!!.sendStringToServer("${position + 1}\n")`。
* **`onDeleteClick(view, position)`**：
  - 取得看板資訊，彈出 `ASAlertDialog` 確認視窗。
  - 使用者點選「確定」後：
    1. 送出 BBS 刪除指令：`TelnetClient.myInstance!!.sendStringToServer("${position + 1}\nd")`。
    2. 從 `boardItems` 移除該項並通知適配器：`boardItems.removeAt(position)`、`adapter?.notifyItemRemoved(position)`。

#### 6.5 BBS 資料解析與畫面更新 (`onPagePreload`)
* 呼叫 `ClassPageHandler.instance.load()` 取得 `ClassPageBlock`。
* 將 Block 中的項目取出填入 `boardItems`。
* 透過 `ASCoroutine.ensureMainThread` 呼叫 `adapter?.notifyDataSetChanged()` 刷新畫面。

---

## 4. BBS 底層通訊協定規範 (BBS Telnet Protocol Specs)

### 4.1 移動次序指令（大 M）
* **觸發時機**：`ItemTouchHelper` 拖曳放開並換位成功時。
* **送出封包格式**：
  ```kotlin
  TelnetOutputBuilder.create()
      .pushString("$fromIndex\nM$toIndex\n")
      .sendToServer()
  ```
* **BBS 伺服端行為序列**：
  1. `$fromIndex\n`：游標跳至原看板位置。
  2. `M`：進入次序移動功能，BBS 底層輸出提示：`請輸入第 fromIndex 看板的新位置：`。
  3. `$toIndex\n`：輸入前端拖曳所得之新位置編號並按 Enter，BBS 完成重整並重繪看板列表。

### 4.2 移出我的最愛指令（小 d）
* **觸發時機**：使用者點擊獨立刪除按鈕並於對話框確認後。
* **送出封包格式**：
  ```kotlin
  TelnetClient.myInstance!!.sendStringToServer("$itemIndex\nd")
  ```
* **BBS 伺服端行為**：刪除該看板項目，伺服器重繪。

---

## 5. 邊界條件與驗證清單 (Acceptance Criteria)

實作完成後，請依循以下清單逐項自我驗證：

- [ ] **目錄結構**：`ClassPage` 及其相關組件完整放置於 `com.kota.Bahamut.pages.classPage` 目錄下。
- [ ] **模式嚴格限制**：
  - 從主選單按 `f` 進入「我的最愛」時：
    - 頂部導覽列副標題顯示 `"長按可移動位置"`。
    - 每列看板右側顯示紅色刪除按鈕。
    - 支援長按上下拖曳排序，有流暢推擠動畫。
  - 從主選單進入「佈告討論區」或「分組討論區」時：
    - 頂部導覽列副標題不顯示 `"長按可移動位置"`。
    - 每列看板右側僅顯示箭頭圖示，無刪除按鈕。
    - 長按不觸發拖曳換位。
- [ ] **手勢與點擊獨立性**：
  - 快速點擊看板列可正常進入看板。
  - 點擊刪除按鈕可正常彈窗並送出 `d` 指令。
  - 長按僅啟動拖曳排序，放開後自動送出 `$fromIndex\nM$toIndex\n`。
- [ ] **編譯安全**：未經使用者明確指示前，不得擅自於本地執行未授權之編譯指令。
