package com.kota.Bahamut.pages.theme

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.core.content.edit
import com.kota.Bahamut.R
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.UserSettings

object ThemeStore {
    private lateinit var perf: SharedPreferences
    private const val PERF_NAME:String = "themeStore"
    private var themeStore:ArrayList<Theme> = ArrayList()

    // 變數
    private const val PER_SELECT_THEME_INDEX:String = "select_theme_index" // 選擇外觀

    /** 初始化並載入儲存的外觀資料 */
    fun upgrade(activity: Activity) {
        if (!::perf.isInitialized) {
            perf = activity.getSharedPreferences(PERF_NAME, 0)
            load()
        }
    }

    /** 取得目前所有的外觀清單 */
    fun getThemeStore(): ArrayList<Theme> {
        return themeStore
    }

    /** 新增一個外觀到清單中 */
    private fun addTheme(theme: Theme) {
        themeStore.add(theme)
    }

    /** 更新目前所有的外觀清單 */
    fun refreshThemeStore() {
        themeStore = ArrayList()
        // 預設
        addTheme(getDefaultTheme(0))
        // 粉紅
        addTheme(getDefaultTheme(1))
        // EInk (排第三)
        addTheme(getDefaultTheme(3))
    }

    /** 載入外觀資料，初始化預設外觀 */
    fun load() {
        refreshThemeStore()
    }

    /** 取得目前使用者選取的外觀索引 */
    fun getSelectIndex(): Int {
        if (!::perf.isInitialized) return 0
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
        if (UserSettings.propertiesFollowSystemDarkMode && TempSettings.myContext != null && isSystemDarkMode(TempSettings.myContext!!)) {
            // 強制返回深色主題 (即使它不顯示在清單中)
            return getDefaultTheme(2)
        }
        val themes = getThemeStore()
        val themeIndex = getSelectIndex()
        if (themeIndex >= 0 && themeIndex < themes.size) {
            return themes[themeIndex]
        }
        return themes[0]
    }

    /** 設定使用者選取的外觀索引並儲存 */
    fun setSelectIndex(selectedIndex: Int) {
        if (::perf.isInitialized) {
            perf.edit { putInt(PER_SELECT_THEME_INDEX, selectedIndex) }
        }
    }

    /** 取得目前應該套用的原生主題資源 ID (用於 Activity.setTheme) */
    fun getThemeResId(): Int {
        if (UserSettings.propertiesFollowSystemDarkMode && TempSettings.myContext != null && isSystemDarkMode(TempSettings.myContext!!)) {
            return R.style.MyTheme_Dark
        }
        
        val index = getSelectIndex()
        return when(index) {
            1 -> R.style.MyTheme_Pink
            2 -> R.style.MyTheme_eInk
            else -> R.style.MyTheme
        }
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
                themePink.contentAuthorColor = "#FFFFC0CB"

                themePink.backgroundColorDanger = "#FF800000"
                themePink.backgroundColorDangerPressed = "#FFFF0000"
                return themePink
            }
            2 -> { // 深色模式 (保留作為系統跟隨用)
                val themeDarkMode = Theme()
                themeDarkMode.name = "深色"

                // 全域基礎色彩 (通用按鈕與文字)
                themeDarkMode.textColor = "#FFE0E0E0"             // 主要文字 (柔和灰白)
                themeDarkMode.textColorPressed = "#FF808080"      // 按壓時文字 (中灰)
                themeDarkMode.textColorDisabled = "#FF808080"     // 停用時文字 (中灰)
                themeDarkMode.backgroundColor = "#FF202020"       // 通用背景 (極深灰)
                themeDarkMode.backgroundColorPressed = "#FF363636"// 按壓時背景 (深灰)
                themeDarkMode.backgroundColorDisabled = "#FF282828"// 停用時背景 (暗灰)
                themeDarkMode.contentAuthorColor = "#FFC0C0C0"

                themeDarkMode.articleAuthorColor0 = "#FFC0C0C0"
                themeDarkMode.articleContentColor0 = "#FFC0C0C0"
                themeDarkMode.articleAuthorColor1 = "#FF80FF80"
                themeDarkMode.articleContentColor1 = "#FF20FF20"
                themeDarkMode.articlePushAuthorColor = "#FF808080"
                themeDarkMode.articlePushContentColor = "#FF808000"

                themeDarkMode.backgroundColorDanger = "#FF4A1A1A"
                themeDarkMode.backgroundColorDangerPressed = "#FF8B3A3A"
                themeDarkMode.textColorDanger = "#FFE0E0E0"
                return themeDarkMode
            }
            3 -> { // EInk (真實電子紙專用高對比版)
                val themeEInk = Theme()
                themeEInk.name = "EInk"

                // 全域基礎色彩
                themeEInk.textColor = "#FF000000"
                themeEInk.textColorPressed = "#FFFFFFFF"
                themeEInk.textColorDisabled = "#FF777777"
                themeEInk.backgroundColor = "#FFFFFFFF"
                themeEInk.backgroundColorPressed = "#FF000000"
                themeEInk.backgroundColorDisabled = "#FFFFFFFF"
                themeEInk.contentAuthorColor = "#FF000000"

                themeEInk.articleAuthorColor0 = "#FF000000"
                themeEInk.articleContentColor0 = "#FF000000"
                themeEInk.articleAuthorColor1 = "#FF444444"
                themeEInk.articleContentColor1 = "#FF444444"
                themeEInk.articlePushAuthorColor = "#FF000000"
                themeEInk.articlePushContentColor = "#FF000000"

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
