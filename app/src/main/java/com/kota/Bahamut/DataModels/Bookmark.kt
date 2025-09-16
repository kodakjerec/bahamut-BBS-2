package com.kota.Bahamut.DataModels

import com.kota.Bahamut.R
import com.kota.Bahamut.Service.CommonFunctions.getContextString
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.ObjectInputStream

/*
 基本的書籤元件
*/

class Bookmark {
    var _author: String? = ""
    var _board: String? = ""
    var _detail: String = ""
    var _ext_data: ByteArray = ByteArray(0)
    var _gy: String? = ""
    var _keyword: String? = ""
    var _mark: String? = "n"
    var _title: String? = ""
    @JvmField
    var index: Int = 0
    var optional: String = OPTIONAL_BOOKMARK
    var weight: Int = 0

    constructor()

    constructor(aInputStream: ObjectInputStream) {
        importFromStream(aInputStream)
    }

    constructor(obj: JSONObject) {
        importFromJSON(obj)
    }

    @Throws(Exception::class)
    fun exportToJSON(): JSONObject {
        val obj = JSONObject()
        obj.put("version", 1)
        obj.put("index", index)
        obj.put("optional", optional)
        obj.put("board", _board)
        obj.put("title", _title)
        obj.put("weight", weight)
        obj.put("detail", _detail)
        obj.put("keyword", _keyword)
        obj.put("author", _author)
        obj.put("mark", _mark)
        obj.put("gy", _gy)
        return obj
    }

    @Throws(JSONException::class)
    fun importFromJSON(obj: JSONObject) {
        index = if (obj.isNull("index")) 0 else obj.getInt("index")
        if (index == 0) {
            print("index:0")
        }
        optional = obj.getString("optional")
        _board = obj.getString("board")
        _title = obj.getString("title")
        weight = obj.getInt("weight")
        _detail = obj.getString("detail")
        _keyword = obj.getString("keyword")
        _author = obj.getString("author")
        _mark = obj.getString("mark")
        _gy = obj.getString("gy")
    }

    @Throws(IOException::class)
    fun importFromStream(aStream: ObjectInputStream) {
        aStream.readInt()
        index = 0
        optional = aStream.readUTF()
        _board = aStream.readUTF()
        _title = aStream.readUTF()
        weight = aStream.readInt()
        _detail = aStream.readUTF()
        _keyword = aStream.readUTF()
        _author = aStream.readUTF()
        _mark = aStream.readUTF()
        _gy = aStream.readUTF()
        _ext_data = ByteArray(aStream.readInt())
        aStream.read(_ext_data)
    }

    var title: String?
        get() = _title
        set(title) {
            if (title == null) {
                _title = ""
            } else {
                _title = title
            }
        }

    var keyword: String?
        get() = _keyword
        set(keyword) {
            if (keyword == null) {
                _keyword = ""
            } else {
                _keyword = keyword
            }
        }

    var author: String?
        get() = _author
        set(author) {
            if (author == null) {
                _author = ""
            } else {
                _author = author
            }
        }

    var mark: String?
        get() = _mark
        set(mark) {
            if (mark == null || mark != "y") {
                _mark = "n"
            } else {
                _mark = mark
            }
        }

    var gy: String?
        get() = _gy
        set(gy) {
            if (gy == null) {
                _gy = ""
            } else {
                _gy = gy
            }
        }

    var board: String?
        get() = _board
        set(board) {
            if (board == null) {
                _board = ""
            } else {
                _board = board
            }
        }

    fun generateTitle(): String {
        var title = ""
        if (_keyword!!.trim { it <= ' ' }.length > 0) {
            title = getContextString(R.string.title_) + _keyword
        }
        if (title.length == 0 && _author!!.trim { it <= ' ' }.length > 0) {
            title = getContextString(R.string.author_) + _author
        }
        if (title.length == 0 && _gy!!.trim { it <= ' ' }.length > 0) {
            title = getContextString(R.string.do_gy_) + _gy
        }
        if (_mark == "y") {
            title = "M " + title
        }
        if (title.length == 0) {
            return getContextString(R.string.no_assign)
        }
        return title
    }

    companion object {
        const val OPTIONAL_BOOKMARK: String = "0" // 書籤
        const val OPTIONAL_STORY: String = "1" // 紀錄
        const val version: Int = 1
    }
}
