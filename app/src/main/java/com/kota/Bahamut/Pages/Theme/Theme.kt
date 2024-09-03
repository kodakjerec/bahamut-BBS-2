package com.kota.Bahamut.Pages.Theme

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

    fun importFromJSON(obj: JSONObject) {
        name = obj.optString("name")
        textColor = obj.optString("tC", textColor)
        backgroundColor = obj.optString("bC", backgroundColor)
        textColorPressed = obj.optString("tCP", textColorPressed)
        backgroundColorPressed = obj.optString("bCP", backgroundColorPressed)
        textColorDisabled = obj.optString("tCD", textColorDisabled)
        backgroundColorDisabled = obj.optString("bCD", backgroundColorDisabled)
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
        return obj
    }
}