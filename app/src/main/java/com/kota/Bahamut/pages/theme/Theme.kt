package com.kota.Bahamut.pages.theme

import org.json.JSONObject

class Theme {
    var name:String = "預設"
    // 預設值是 @style/ToolbarItem
    var textColor:String = "#FFFFFFFF"
    var backgroundColor:String = "#FF002020"
    var textColorPressed:String = "#FF000000"
    var backgroundColorPressed:String = "#FFB5E61D"
    var textColorDisabled:String = "#FF608060"
    var backgroundColorDisabled:String = "#FF001A1A"

    // 標題列
    var headerBackColor: String = "#FF000060"
    var headerHeaderColor: String = "#FFF0F080"
    var headerManagerColor: String = "#FFFFFFFF"
    var headerBorderColor: String = "#FFC0FFFF"

    // 內文
    var contentBackColor: String = "#FF000000"
    var contentAuthorColor: String = "#FFFFFFFF"
    var contentTextColor: String = "#FFC0C0C0"

    // 引用
    var quoteBackColor: String = "#FF000000"
    var quoteAuthorColor: String = "#FF80FF80"
    var quoteTextColor: String = "#FF20FF20"

    fun importFromJSON(obj: JSONObject) {
        name = obj.optString("name")
        textColor = obj.optString("tC", textColor)
        backgroundColor = obj.optString("bC", backgroundColor)
        textColorPressed = obj.optString("tCP", textColorPressed)
        backgroundColorPressed = obj.optString("bCP", backgroundColorPressed)
        textColorDisabled = obj.optString("tCD", textColorDisabled)
        backgroundColorDisabled = obj.optString("bCD", backgroundColorDisabled)
        headerBackColor = obj.optString("hBC", headerBackColor) // 標題列背景
        headerHeaderColor = obj.optString("hHC", headerHeaderColor) // 標題列標題
        headerManagerColor = obj.optString("hMC", headerManagerColor) // 標題列版主
        headerBorderColor = obj.optString("hBDC", headerBorderColor) // 標題列版面
        contentBackColor = obj.optString("cBC", contentBackColor) // 內文背景
        contentAuthorColor = obj.optString("cAC", contentAuthorColor) // 內文作者
        contentTextColor = obj.optString("cTC", contentTextColor) // 內文
        quoteBackColor = obj.optString("qBC", quoteBackColor) // 引用背景
        quoteAuthorColor = obj.optString("qAC", quoteAuthorColor) // 引用作者
        quoteTextColor = obj.optString("qTC", quoteTextColor) // 引用內文
    }

    fun exportToJSON(): JSONObject {
        val obj = JSONObject()
        obj.put("name", name)
        obj.put("tC", textColor)
        obj.put("bC", backgroundColor)
        obj.put("tCP", textColorPressed)
        obj.put("bCP", backgroundColorPressed)
        obj.put("tCD", textColorDisabled)
        obj.put("bCD", backgroundColorDisabled)
        obj.put("hBC", headerBackColor) // 標題列背景
        obj.put("hHC", headerHeaderColor) // 標題列標題
        obj.put("hMC", headerManagerColor) // 標題列版主
        obj.put("hBDC", headerBorderColor) // 標題列版面
        obj.put("cBC", contentBackColor) // 內文背景
        obj.put("cAC", contentAuthorColor) // 內文作者
        obj.put("cTC", contentTextColor) // 內文
        obj.put("qBC", quoteBackColor) // 引用背景
        obj.put("qAC", quoteAuthorColor) // 引用作者
        obj.put("qTC", quoteTextColor) // 引用內文
        return obj
    }
}