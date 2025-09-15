package com.kota.Bahamut.DataModels

import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class ArticleTemp {
    var content: String = ""
    var header: String = ""
    var title: String = ""

    @Throws(JSONException::class)
    fun exportToJSON(): JSONObject {
        val obj = JSONObject()
        obj.put("header", header)
        obj.put("title", title)
        obj.put("content", content)
        return obj
    }

    @Throws(JSONException::class)
    fun importFromJSON(obj: JSONObject) {
        header = obj.getString("header")
        title = obj.getString("title")
        content = obj.getString("content")
    }

    @Throws(IOException::class)
    fun exportToStream(aStream: ObjectOutputStream) {
        aStream.write(1)
        aStream.writeUTF(header)
        aStream.writeUTF(title)
        aStream.writeUTF(content)
    }

    @Throws(IOException::class)
    fun importFromStream(aStream: ObjectInputStream) {
        aStream.readInt()
        header = aStream.readUTF()
        title = aStream.readUTF()
        content = aStream.readUTF()
    }

    companion object {
        const val version = 1
    }
}
