package com.kota.Bahamut.pages.theme

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.util.Log
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.UserSettings
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.content.edit

object ThemeStore {
    private lateinit var perf: SharedPreferences
    private const val PERF_NAME:String = "themeStore"
    private var themeStore:ArrayList<Theme> = ArrayList()

    // 變數
    private const val PER_SELECT_THEME_INDEX:String = "select_theme_index" // 選擇外觀

    /** 初始化並載入儲存的外觀資料 */
    fun upgrade(activity: Activity) {
        perf = activity.getSharedPreferences(PERF_NAME, 0)
        load()
    }

    /** 取得目前所有的外觀清單 */
    fun getThemeStore(): ArrayList<Theme> {
        return themeStore
    }

    /** 新增一個外觀到清單中 */
    private fun addTheme(theme: Theme) {
        themeStore.add(theme)
    }

    /** 更新指定索引的外觀資料並儲存 */
    fun updateTheme(index:Int, theme: Theme) {
        themeStore.removeAt(index)
        themeStore.add(index, theme)
        save()
    }

    /** 從 SharedPreferences 載入外觀資料，若無資料則初始化預設外觀 */
    fun load() {
        val data:String = perf.getString("themeStore", "{\"data\":[]}")!!
        themeStore = ArrayList()

        try {
            // string to JSONObject
            val jsonObject = JSONObject(data)
            val jsonArray = jsonObject.getJSONArray("data")
            if (jsonArray.length()==0) {
                // 預設
                addTheme(getDefaultTheme(0))

                // 粉紅
                addTheme(getDefaultTheme(1))

                // eInk
                addTheme(getDefaultTheme(2))

                // eInk2
                addTheme(getDefaultTheme(3))

                // 自訂2
                val themeDef2 = getDefaultTheme(0)
                themeDef2.name = "自訂2"
                addTheme(themeDef2)
            } else {
                for (i in 0 until jsonArray.length()) {
                    val subJSONObject = jsonArray.getJSONObject(i)
                    val theme = Theme()
                    theme.importFromJSON(subJSONObject)
                    addTheme(theme)
                }
            }
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, e.message.toString())
        }
    }

    /** 將目前的外觀清單序列化為 JSON 並儲存到 SharedPreferences */
    fun save() {
        val obj = JSONObject()
        try {
            val jsonArray = JSONArray()
            for (theme in themeStore) {
                jsonArray.put(theme.exportToJSON())
            }
            obj.put("data", jsonArray)
            perf.edit { putString("themeStore", obj.toString()) }
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, e.message.toString())
        }
    }

    /** 取得目前使用者選取的外觀索引 */
    fun getSelectIndex(): Int {
        return perf.getInt(PER_SELECT_THEME_INDEX, 0)
    }

    /** 判斷目前系統是否處於深色模式 */
    fun isSystemDarkMode(context: Context): Boolean {
        val mode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }

    /** 
     * 取得目前應該套用的外觀物件。
     * 若開啟「跟隨系統深色模式」且系統處於深色模式，則強制返回深色主題。
     */
    fun getSelectTheme(): Theme {
        if (UserSettings.propertiesFollowSystemDarkMode && isSystemDarkMode(TempSettings.myContext!!)) {
            // 返回 index 2 的深色主題
            if (themeStore.size > 2) {
                return themeStore[2]
            }
        }
        val themeIndex = perf.getInt(PER_SELECT_THEME_INDEX, 0)
        return themeStore[themeIndex]
    }

    /** 設定使用者選取的外觀索引並儲存 */
    fun setSelectIndex(selectedIndex: Int) {
        perf.edit { putInt(PER_SELECT_THEME_INDEX, selectedIndex) }
    }

    /** 取得特定索引的初始預設外觀資料 */
    fun getDefaultTheme(selectedIndex: Int): Theme {
        when(selectedIndex) {
            1 -> {
                val themePink = Theme()
                themePink.name = "粉紅"
                themePink.textColorDisabled = "#FF808080"
                themePink.backgroundColor = "#FFFE00FE"
                themePink.backgroundColorPressed = "#FFE400E4"
                themePink.backgroundColorDisabled = "#FF650065"

                themePink.backgroundColorDanger = "#FF800000"
                themePink.backgroundColorDangerPressed = "#FFFF0000"
                return themePink
            }
            2 -> { // 深色模式
                val themeDarkMode = Theme()
                themeDarkMode.name = "深色"

                // 全域基礎色彩 (通用按鈕與文字)
                themeDarkMode.textColor = "#FFE0E0E0"             // 主要文字 (柔和灰白)
                themeDarkMode.textColorPressed = "#FF808080"      // 按壓時文字 (中灰)
                themeDarkMode.textColorDisabled = "#FF808080"     // 停用時文字 (中灰)
                themeDarkMode.backgroundColor = "#FF202020"       // 通用背景 (極深灰)
                themeDarkMode.backgroundColorPressed = "#FF363636"// 按壓時背景 (深灰)
                themeDarkMode.backgroundColorDisabled = "#FF282828"// 停用時背景 (暗灰)

                // 標題列 (Header)
                themeDarkMode.headerBackColor = "#FF121212"     // 標題列背景 (深灰黑)
                themeDarkMode.headerHeaderColor = "#FFB0B3B8"  // 看板名稱 (柔和灰白)
                themeDarkMode.headerManagerColor = "#FF4A7A5A" // 版主名稱 (沉穩苔蘚綠)
                themeDarkMode.headerBorderColor = "#FF555555"  // 邊框與分隔線 (暗灰)

                // 文章內文 (Content)
                themeDarkMode.contentBackColor = "#FF121212"   // 內文背景 (深灰黑)
                themeDarkMode.contentAuthorColor = "#FFB0B3B8" // 發文作者/抬頭 (藍灰)
                themeDarkMode.contentTextColor = "#FF8B939C"   // 正文內容 (柔和灰白)

                // 引用內容 (Quote)
                themeDarkMode.quoteBackColor = "#FF121212"     // 引用背景 (深灰黑)
                themeDarkMode.quoteAuthorColor = "#FF6C757D"   // 被引用者 (中灰)
                themeDarkMode.quoteTextColor = "#FF5A5E63"     // 引用內文 (深灰)

                // 看板列表 (List) - 低亮度特色：低彩度、利用色彩與階層沉降達到護眼效果
                themeDarkMode.listBackColor = "#FF121212"      // 列表背景 (深灰黑)

                // 一般文章標題
                themeDarkMode.listTitleColor = "#FFB0B3B8"     // 未讀標題 (柔和灰白)
                themeDarkMode.listTitleReadColor = "#FF5A5E63" // 已讀標題 (深灰)

                // 關注首篇 (◆) - 最高優先順序
                themeDarkMode.listTitleFollowFirstColor = "#FF4CAF50"      // 關注首篇未讀 (低飽和草綠)
                themeDarkMode.listTitleFollowFirstReadColor = "#FF2E6B32"  // 關注首篇已讀 (暗綠)

                // 關注回應 (Re) - 次要優先順序
                themeDarkMode.listTitleFollowColor = "#FFC0A030"           // 關注回應未讀 (沉穩芥末黃)
                themeDarkMode.listTitleFollowReadColor = "#FF66541A"       // 關注回應已讀 (暗土黃)

                // 列表輔助資訊
                themeDarkMode.listNumberColor = "#FF8B5A5A"    // 文章編號 (莫蘭迪暗磚紅)
                themeDarkMode.listDateColor = "#FF4A7A5A"      // 發文日期 (深苔蘚綠)
                themeDarkMode.listAuthorColor = "#FF6B728E"    // 文章作者 (低彩度灰藍)
                themeDarkMode.listMarkColor = "#FFB25900"      // M文標記 (暗橘色)
                themeDarkMode.listStatusColor = "#FF9E8C00"    // 狀態標記 Re/◆ (暗黃/芥末綠)
                themeDarkMode.listDividerColor = "#FF222222"   // 項目分隔線 (極暗灰)

                themeDarkMode.backgroundColorDanger = "#FF4A1A1A"
                themeDarkMode.backgroundColorDangerPressed = "#FF8B3A3A"
                themeDarkMode.textColorDanger = "#FFE0E0E0"
                return themeDarkMode
            }
            3 -> { // eInk (真實電子紙專用高對比版)
                val themeEInk = Theme()
                themeEInk.name = "eInk"

                // 全域基礎色彩
                themeEInk.textColor = "#FF000000"
                themeEInk.textColorPressed = "#FFFFFFFF"
                themeEInk.textColorDisabled = "#FF777777"
                themeEInk.backgroundColor = "#FFFFFFFF"
                themeEInk.backgroundColorPressed = "#FF000000"
                themeEInk.backgroundColorDisabled = "#FFFFFFFF"

                // 標題列 ( Header 避免大面積灰底造成殘影，改純白底加粗黑框)
                themeEInk.headerBackColor = "#FFFFFFFF"
                themeEInk.headerHeaderColor = "#FF000000"
                themeEInk.headerManagerColor = "#FF444444"
                themeEInk.headerBorderColor = "#FF000000"

                // 文章內文 (極高對比)
                themeEInk.contentBackColor = "#FFFFFFFF"
                themeEInk.contentAuthorColor = "#FF000000"
                themeEInk.contentTextColor = "#FF444444"

                // 引用 (避免使用過淡的灰，採用顯眼深灰區隔)
                themeEInk.quoteBackColor = "#FFFFFFFF"
                themeEInk.quoteAuthorColor = "#FF555555"
                themeEInk.quoteTextColor = "#FF444444"

                // 看板列表
                themeEInk.listBackColor = "#FFFFFFFF"

                // 一般文章標題
                themeEInk.listTitleColor = "#FF000000"
                themeEInk.listTitleReadColor = "#FF666666" // 已讀不用過淺的灰，改用 666 保障銳利度

                // 關注首篇 (◆)
                themeEInk.listTitleFollowFirstColor = "#FF000000"
                themeEInk.listTitleFollowFirstReadColor = "#FF555555"

                // 關注回應 (Re)
                themeEInk.listTitleFollowColor = "#FF000000"
                themeEInk.listTitleFollowReadColor = "#FF555555"

                // 列表輔助資訊 (取消淺灰，全面提高對比)
                themeEInk.listNumberColor = "#FF444444"
                themeEInk.listDateColor = "#FF444444"
                themeEInk.listAuthorColor = "#FF000000"
                themeEInk.listMarkColor = "#FF000000"
                themeEInk.listStatusColor = "#FF000000"
                themeEInk.listDividerColor = "#FF888888" // 稍微加深分隔線，確保清晰不留白痕

                themeEInk.backgroundColorDanger = "#FF000000"
                themeEInk.backgroundColorDangerPressed = "#FF444444"
                themeEInk.textColorDanger = "#FFFFFFFF"
                return themeEInk
            }
            else -> {
                return Theme()
            }
        }
    }
}
