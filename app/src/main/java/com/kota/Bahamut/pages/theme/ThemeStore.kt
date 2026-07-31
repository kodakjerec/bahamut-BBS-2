package com.kota.Bahamut.pages.theme

import android.app.Activity
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.content.edit

object ThemeStore {
    private lateinit var perf: SharedPreferences
    private const val PERF_NAME:String = "themeStore"
    private var themeStore:ArrayList<Theme> = ArrayList()

    // 變數
    private const val PER_SELECT_THEME_INDEX:String = "select_theme_index" // 選擇外觀

    fun upgrade(activity: Activity) {
        perf = activity.getSharedPreferences(PERF_NAME, 0)
        load()
    }

    /** 外觀 */
    fun getThemeStore(): ArrayList<Theme> {
        return themeStore
    }

    /** 新增外觀 */
    private fun addTheme(theme: Theme) {
        themeStore.add(theme)
    }

    /** 更新外觀 */
    fun updateTheme(index:Int, theme: Theme) {
        themeStore.removeAt(index)
        themeStore.add(index, theme)
        save()
    }

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

    fun getSelectIndex(): Int {
        return perf.getInt(PER_SELECT_THEME_INDEX, 0)
    }
    fun getSelectTheme(): Theme {
        val themeIndex = perf.getInt(PER_SELECT_THEME_INDEX, 0)
        return themeStore[themeIndex]
    }

    fun setSelectIndex(selectedIndex: Int) {
        perf.edit { putInt(PER_SELECT_THEME_INDEX, selectedIndex) }
    }

    /** 取得預設外觀 */
    fun getDefaultTheme(selectedIndex: Int): Theme {
        when(selectedIndex) {
            1 -> {
                val themePink = Theme()
                themePink.name = "粉紅"
                themePink.textColorDisabled = "#FF808080"
                themePink.backgroundColor = "#FFFE00FE"
                themePink.backgroundColorPressed = "#FFE400E4"
                themePink.backgroundColorDisabled = "#FF650065"
                return themePink
            }
            2 -> { // 低亮度
                val themeEInk = Theme()
                themeEInk.name = "低亮度"
                themeEInk.textColor = "#FFE0E0E0"
                themeEInk.textColorPressed = "#FF808080"
                themeEInk.textColorDisabled = "#FF808080"
                themeEInk.backgroundColor =  "#FF202020"
                themeEInk.backgroundColorPressed = "#FF363636"
                themeEInk.backgroundColorDisabled = "#FF282828"
                themeEInk.headerBackColor = "#FF202020"
                themeEInk.headerHeaderColor = "#FFFFFFFF"
                themeEInk.headerManagerColor = "#FFCCCCCC"
                themeEInk.headerBorderColor = "#FF999999"
                themeEInk.contentBackColor = "#FF202020"
                themeEInk.contentAuthorColor = "#FFEEEEEE"
                themeEInk.contentTextColor = "#FFD0D0D0"
                themeEInk.quoteBackColor = "#FF202020"
                themeEInk.quoteAuthorColor = "#FF999999"
                themeEInk.quoteTextColor = "#FF808080"
                // 看板列表 (低亮度特色：利用灰階亮度區分優先順序)
                themeEInk.listBackColor = "#FF202020"      // 深灰背景

                // 一般文章
                themeEInk.listTitleColor = "#FFE0E0E0"     // 未讀 (淺灰)
                themeEInk.listTitleReadColor = "#FF808080" // 已讀 (中灰)

                // 關注首篇 (◆) - 最顯眼
                themeEInk.listTitleFollowFirstColor = "#FFFFFFFF"      // 關注首篇未讀 (純白)
                themeEInk.listTitleFollowFirstReadColor = "#FFB0B0B0"  // 關注首篇已讀 (淺灰)

                // 關注回應 (Re) - 次顯眼
                themeEInk.listTitleFollowColor = "#FFCCCCCC"           // 關注回應未讀 (淺灰)
                themeEInk.listTitleFollowReadColor = "#FF666666"       // 關注回應已讀 (深灰)

                // 其他資訊
                themeEInk.listNumberColor = "#FF999999"    // 編號灰
                themeEInk.listDateColor = "#FF999999"      // 日期灰
                themeEInk.listAuthorColor = "#FFCCCCCC"    // 作者淺灰
                themeEInk.listMarkColor = "#FFFFFFFF"      // M文白色
                themeEInk.listStatusColor = "#FFCCCCCC"    // 狀態淺灰
                themeEInk.listDividerColor = "#FF444444"   // 分隔線
                return themeEInk
            }
            3 -> { // eInk2 (純白版)
                val themeEInk2 = Theme()
                themeEInk2.name = "eInk2"
                themeEInk2.textColor = "#FF000000"
                themeEInk2.textColorPressed = "#FF000000"
                themeEInk2.textColorDisabled = "#FF000000"
                themeEInk2.backgroundColor =  "#FFFFFFFF"
                themeEInk2.backgroundColorPressed = "#FFFFFFFF"
                themeEInk2.backgroundColorDisabled = "#FFFFFFFF"
                themeEInk2.headerBackColor = "#FF808080"
                themeEInk2.headerHeaderColor = "#FF000000"
                themeEInk2.headerManagerColor = "#FF000000"
                themeEInk2.headerBorderColor = "#FF000000"
                themeEInk2.contentBackColor = "#FFFFFFFF"
                themeEInk2.contentAuthorColor = "#FF000000"
                themeEInk2.contentTextColor = "#FF000000"
                themeEInk2.quoteBackColor = "#FFFFFFFF"
                themeEInk2.quoteAuthorColor = "#FF808080"
                themeEInk2.quoteTextColor = "#FF808080"
                // 看板列表 (eInk2 特色：純白底黑字)
                themeEInk2.listBackColor = "#FFFFFFFF"      // 純白背景
                themeEInk2.listTitleColor = "#FF000000"     // 未讀純黑
                themeEInk2.listTitleReadColor = "#FF999999" // 已讀淺灰
                themeEInk2.listTitleFollowColor = "#FF000000" // 關注黑 (加粗由UI處理)
                themeEInk2.listTitleFollowReadColor = "#FF666666" // 關注已讀深灰
                themeEInk2.listNumberColor = "#FF666666"    // 編號深灰
                themeEInk2.listDateColor = "#FF666666"      // 日期深灰
                themeEInk2.listAuthorColor = "#FF333333"    // 作者黑灰
                themeEInk2.listMarkColor = "#FF000000"      // M文黑色
                themeEInk2.listStatusColor = "#FF333333"    // 狀態黑灰
                themeEInk2.listDividerColor = "#FFCCCCCC"   // 分隔線淺灰
                return themeEInk2
            }
            else -> {
                return Theme()
            }
        }
    }
}