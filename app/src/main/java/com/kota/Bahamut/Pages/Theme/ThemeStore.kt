package com.kota.Bahamut.Pages.Theme

import android.app.Activity
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

object ThemeStore {
    private lateinit var perf: SharedPreferences
    private const val perfName:String = "themeStore"
    private var themeStore:ArrayList<Theme> = ArrayList()

    // 變數
    private const val perSelectThemeIndex:String = "select_theme_index" // 選擇外觀

    fun upgrade(activity: Activity) {
        perf = activity.getSharedPreferences(perfName, 0)
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

                // 灰白
                addTheme(getDefaultTheme(2))

                // 自訂1
                val themeDef1 = getDefaultTheme(0)
                themeDef1.name = "自訂1"
                addTheme(themeDef1)

                // 自訂2
                val themeDef2 = getDefaultTheme(0)
                themeDef2.name = "自訂2"
                addTheme(themeDef2)

                // 存檔
                save()
            } else {
                for (i in 0 until jsonArray.length()) {
                    val subJSONObject = jsonArray.getJSONObject(i)
                    val theme = Theme()
                    theme.importFromJSON(subJSONObject)
                    addTheme(theme)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
            perf.edit().putString("themeStore", obj.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getSelectIndex(): Int {
        return perf.getInt(perSelectThemeIndex, 0)
    }
    fun getSelectTheme(): Theme {
        val themeIndex = perf.getInt(perSelectThemeIndex, 0)
        return themeStore[themeIndex]
    }

    fun setSelectIndex(selectedIndex: Int) {
        perf.edit().putInt(perSelectThemeIndex, selectedIndex).apply()
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
            2 -> {
                val themeGrayDark = Theme()
                themeGrayDark.name = "灰白"
                themeGrayDark.textColor = "#FFE0E0E0"
                themeGrayDark.textColorPressed = "#FF808080"
                themeGrayDark.textColorDisabled = "#FF808080"
                themeGrayDark.backgroundColor =  "#FF3F3F3F"
                themeGrayDark.backgroundColorPressed = "#FF363636"
                themeGrayDark.backgroundColorDisabled = "#FF282828"
                return themeGrayDark
            }
            else -> {
                return Theme()
            }
        }
    }
}