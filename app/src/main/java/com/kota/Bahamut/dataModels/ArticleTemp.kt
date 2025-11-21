package com.kota.Bahamut.dataModels

import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class ArticleTemp {
    @JvmField
    var content: String? = ""
    @JvmField
    var header: String? = ""
    @JvmField
    var title: String? = ""

    @Throws(JSONException::class)
    fun exportToJSON(): JSONObject {
        val obj = JSONObject()
        obj.put("header", this.header)
        obj.put("title", this.title)
        obj.put("content", this.content)
        return obj
    }

    @Throws(JSONException::class)
    fun importFromJSON(obj: JSONObject) {
        this.header = obj.getString("header")
        this.title = obj.getString("title")
        this.content = obj.getString("content")
    }

    @Throws(IOException::class)
    fun exportToStream(aStream: ObjectOutputStream) {
        aStream.write(1)
        aStream.writeUTF(this.header)
        aStream.writeUTF(this.title)
        aStream.writeUTF(this.content)
    }

    @Throws(IOException::class)
    fun importFromStream(aStream: ObjectInputStream) {
        aStream.readInt()
        this.header = aStream.readUTF()
        this.title = aStream.readUTF()
        this.content = aStream.readUTF()
    }

    companion object {
        const val VERSION: Int = 1
    }
}
