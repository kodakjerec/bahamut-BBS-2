package com.kota.Bahamut.dataModels

import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextString
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.ObjectInputStream

/*
 基本的書籤元件
*/

class Bookmark {
    var myAuthor: String? = ""
    var myBoard: String? = ""
    var myDetail: String = ""
    var myExtData: ByteArray = ByteArray(0)
    var myGY: String? = ""
    var myKeyword: String? = ""
    var myMark: String? = "n"
    var myTitle: String? = ""
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
        obj.put("board", myBoard)
        obj.put("title", myTitle)
        obj.put("weight", weight)
        obj.put("detail", myDetail)
        obj.put("keyword", myKeyword)
        obj.put("author", myAuthor)
        obj.put("mark", myMark)
        obj.put("gy", myGY)
        return obj
    }

    @Throws(JSONException::class)
    fun importFromJSON(obj: JSONObject) {
        index = if (obj.isNull("index")) 0 else obj.getInt("index")
        if (index == 0) {
            print("index:0")
        }
        optional = obj.getString("optional")
        myBoard = obj.getString("board")
        myTitle = obj.getString("title")
        weight = obj.getInt("weight")
        myDetail = obj.getString("detail")
        myKeyword = obj.getString("keyword")
        myAuthor = obj.getString("author")
        myMark = obj.getString("mark")
        myGY = obj.getString("gy")
    }

    @Throws(IOException::class)
    fun importFromStream(aStream: ObjectInputStream) {
        aStream.readInt()
        index = 0
        optional = aStream.readUTF()
        myBoard = aStream.readUTF()
        myTitle = aStream.readUTF()
        weight = aStream.readInt()
        myDetail = aStream.readUTF()
        myKeyword = aStream.readUTF()
        myAuthor = aStream.readUTF()
        myMark = aStream.readUTF()
        myGY = aStream.readUTF()
        myExtData = ByteArray(aStream.readInt())
        aStream.read(myExtData)
    }

    var title: String?
        get() = myTitle
        set(title) {
            myTitle = title ?: ""
        }

    var keyword: String?
        get() = myKeyword
        set(keyword) {
            myKeyword = keyword ?: ""
        }

    var author: String?
        get() = myAuthor
        set(author) {
            myAuthor = author ?: ""
        }

    var mark: String?
        get() = myMark
        set(mark) {
            myMark = if (mark == null || mark != "y") {
                "n"
            } else {
                mark
            }
        }

    var gy: String?
        get() = myGY
        set(gy) {
            myGY = gy ?: ""
        }

    var board: String?
        get() = myBoard
        set(board) {
            myBoard = board ?: ""
        }

    fun generateTitle(): String {
        var title = ""
        if (myKeyword?.trim { it <= ' ' }.isNotEmpty()) {
            title = getContextString(R.string.title_) + myKeyword
        }
        if (title.isEmpty() && myAuthor?.trim { it <= ' ' }.isNotEmpty()) {
            title = getContextString(R.string.author_) + myAuthor
        }
        if (title.isEmpty() && myGY?.trim { it <= ' ' }.isNotEmpty()) {
            title = getContextString(R.string.do_gy_) + myGY
        }
        if (myMark == "y") {
            title = "M $title"
        }
        if (title.isEmpty()) {
            return getContextString(R.string.no_assign)
        }
        return title
    }

    companion object {
        const val OPTIONAL_BOOKMARK: String = "0" // 書籤
        const val OPTIONAL_STORY: String = "1" // 紀錄
        const val VERSION: Int = 1
    }
}
