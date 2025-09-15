package com.kota.Bahamut.DataModels
/*
  基本的書籤元件
 */

import com.kota.Bahamut.R
import com.kota.Bahamut.Service.CommonFunctions.getContextString
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.ObjectInputStream

class Bookmark {
    private var _author = ""
    private var _board = ""
    private var _detail = ""
    private var _ext_data = ByteArray(0)
    private var _gy = ""
    private var _keyword = ""
    private var _mark = "n"
    private var _title = ""
    var index = 0
    var optional = OPTIONAL_BOOKMARK
    var weight = 0

    constructor()

    @Throws(IOException::class)
    constructor(aInputStream: ObjectInputStream) {
        importFromStream(aInputStream)
    }

    @Throws(JSONException::class)
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

    fun getTitle(): String {
        return _title
    }

    fun setTitle(title: String?) {
        _title = title ?: ""
    }

    fun getKeyword(): String {
        return _keyword
    }

    fun setKeyword(keyword: String?) {
        _keyword = keyword ?: ""
    }

    fun getAuthor(): String {
        return _author
    }

    fun setAuthor(author: String?) {
        _author = author ?: ""
    }

    fun getMark(): String {
        return _mark
    }

    fun setMark(mark: String?) {
        _mark = if (mark == null || mark != "y") "n" else mark
    }

    fun getGy(): String {
        return _gy
    }

    fun setGy(gy: String?) {
        _gy = gy ?: ""
    }

    fun setBoard(board: String?) {
        _board = board ?: ""
    }

    fun getBoard(): String {
        return _board
    }

    fun generateTitle(): String {
        var title = ""
        if (_keyword.trim().isNotEmpty()) {
            title = getContextString(R.string.title_) + _keyword
        }
        if (title.isEmpty() && _author.trim().isNotEmpty()) {
            title = getContextString(R.string.author_) + _author
        }
        if (title.isEmpty() && _gy.trim().isNotEmpty()) {
            title = getContextString(R.string.do_gy_) + _gy
        }
        if (_mark == "y") {
            title = "M $title"
        }
        if (title.isEmpty()) {
            return getContextString(R.string.no_assign)
        }
        return title
    }

    companion object {
        const val OPTIONAL_BOOKMARK = "0" // 書籤
        const val OPTIONAL_STORY = "1" // 紀錄
        const val version = 1
    }
}
