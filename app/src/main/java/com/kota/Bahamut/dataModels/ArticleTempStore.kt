package com.kota.Bahamut.dataModels

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.ObjectInputStream
import java.util.Vector
import androidx.core.content.edit

/*
    戰巴哈專用暫存檔
    0~4 文章使用
    8  mail
    9  發文
 */
class ArticleTempStore {
    private val context: Context?
    private var filePath: String? = null
    @JvmField
    var articles: Vector<ArticleTemp> = Vector<ArticleTemp>()

    constructor(context: Context?, aFilePath: String?) {
        this.context = context
        this.filePath = aFilePath
        for (i in 0..9) {
            this.articles.add(ArticleTemp())
        }
    }

    constructor(context: Context?) {
        this.context = context
        for (i in 0..9) {
            this.articles.add(ArticleTemp())
        }
        load()
    }

    fun load() {
        println("load article store from file")
        var loadFromFile = false
        if (this.filePath != null && this.filePath?.isNotEmpty()) {
            val file = File(this.filePath!!)
            if (file.exists()) {
                try {
                    val fileInputStream: InputStream = FileInputStream(file)
                    val inputStream = ObjectInputStream(fileInputStream)
                    importFromStream(inputStream)
                    inputStream.close()
                    fileInputStream.close()
                    file.delete()
                    loadFromFile = true
                } catch (e: IOException) {
                    Log.e(javaClass.simpleName, (if (e.message != null) e.message else "")!!)
                }
            }
        }
        if (loadFromFile) {
            store()
        }
        if (!loadFromFile && this.context != null) {
            try {
                val saveData: String = this.context.getSharedPreferences("article_temp", 0)
                    .getString("save_data", "")!!
                if (saveData.isNotEmpty()) {
                    importFromJSON(JSONObject(saveData))
                }
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    fun store() {
        if (this.context != null) {
            try {
                val perf = this.context.getSharedPreferences("article_temp", 0)
                perf.edit(commit = true) { putString("save_data", exportToJSON().toString()) }
            } catch (e: Exception) {
                Log.e(javaClass.simpleName, (if (e.message != null) e.message else "")!!)
            }
        }
    }

    @Throws(JSONException::class)
    fun importFromJSON(obj: JSONObject) {
        val data = obj.getJSONArray("data")
        this.articles.clear()
        for (i in 0..<data.length()) {
            val itemData = data.getJSONObject(i)
            val temp = ArticleTemp()
            temp.importFromJSON(itemData)
            this.articles.add(temp)
        }
    }

    @Throws(JSONException::class)
    fun exportToJSON(): JSONObject {
        val obj = JSONObject()
        val saveData = JSONArray()
        for (article in this.articles) {
            saveData.put(article.exportToJSON())
        }
        obj.put("data", saveData)
        return obj
    }

    @Throws(IOException::class)
    fun importFromStream(aStream: ObjectInputStream) {
        aStream.readInt()
        val size = aStream.readInt()
        for (i in 0..<size) {
            val article = ArticleTemp()
            article.importFromStream(aStream)
            this.articles.add(article)
        }
    }

    companion object {
        const val VERSION: Int = 1
        @JvmStatic
        fun upgrade(context: Context?, aFilePath: String?) {
            ArticleTempStore(context, aFilePath).load()
        }
    }
}
