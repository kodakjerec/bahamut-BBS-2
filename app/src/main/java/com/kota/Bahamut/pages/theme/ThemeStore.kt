package com.kota.Bahamut.pages.theme

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.core.content.edit
import com.kota.Bahamut.R
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.UserSettings

object ThemeStore {
    private lateinit var perf: SharedPreferences
    private const val PERF_NAME: String = "themeStore"
    private const val PER_SELECT_THEME_INDEX: String = "select_theme_index"

    /** 初始化 ThemeStore */
    fun upgrade(context: Context) {
        if (!::perf.isInitialized) {
            perf = context.getSharedPreferences(PERF_NAME, Context.MODE_PRIVATE)
        }
    }

    /** 取得目前使用者選取的外觀索引 */
    fun getSelectIndex(): Int {
        if (!::perf.isInitialized) return 0
        return perf.getInt(PER_SELECT_THEME_INDEX, 0)
    }

    /** 設定使用者選取的外觀索引並儲存 */
    fun setSelectIndex(selectedIndex: Int) {
        if (::perf.isInitialized) {
            perf.edit { putInt(PER_SELECT_THEME_INDEX, selectedIndex) }
        }
    }

    /** 判斷目前系統是否處於深色模式 */
    fun isSystemDarkMode(context: Context): Boolean {
        val mode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }

    /** 取得目前應該套用的原生主題資源 ID (用於 Activity.setTheme) */
    fun getThemeResId(): Int {
        val isDark = UserSettings.propertiesFollowSystemDarkMode && TempSettings.myContext != null && isSystemDarkMode(TempSettings.myContext!!)
        return when (getSelectIndex()) {
            1 -> if (isDark) R.style.MyTheme_Pink_Dark else R.style.MyTheme_Pink
            2 -> if (isDark) R.style.MyTheme_eInk_Dark else R.style.MyTheme_eInk
            else -> if (isDark) R.style.MyTheme_Dark else R.style.MyTheme
        }
    }

    /** 取得目前應該套用的對話框 Activity 原生主題資源 ID */
    fun getDialogThemeResId(): Int {
        val isDark = UserSettings.propertiesFollowSystemDarkMode && TempSettings.myContext != null && isSystemDarkMode(TempSettings.myContext!!)
        return when (getSelectIndex()) {
            1 -> if (isDark) R.style.Dialog_NoTitleBar_Pink_Dark else R.style.Dialog_NoTitleBar_Pink
            2 -> if (isDark) R.style.Dialog_NoTitleBar_eInk_Dark else R.style.Dialog_NoTitleBar_eInk
            else -> if (isDark) R.style.Dialog_NoTitleBar_Dark else R.style.Dialog_NoTitleBar
        }
    }
}
