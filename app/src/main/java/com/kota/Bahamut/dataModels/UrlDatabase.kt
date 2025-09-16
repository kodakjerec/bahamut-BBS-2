package com.kota.Bahamut.dataModels

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.kota.telnet.TelnetClient
import java.util.Vector

class UrlDatabase(context: Context?) : SQLiteOpenHelper(
    context,
    TelnetClient.getClient().getUsername().toLowerCase().trim() + "_database",
    null,
    1
) {
    override fun onCreate(aDatabase: SQLiteDatabase) {
        try {
            var CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS urls (" +
                    "url TEXT PRIMARY KEY, " +
                    "title TEXT, " +
                    "description TEXT, " +
                    "imageUrl TEXT, " +
                    "isPic TEXT" +
                    ")"

            aDatabase.execSQL(CREATE_TABLE_QUERY)
            CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS shorten_urls (" +
                    "shorten_url TEXT PRIMARY KEY, " +
                    "title TEXT, " +
                    "description TEXT, " +
                    "url TEXT)"

            aDatabase.execSQL(CREATE_TABLE_QUERY)
        } catch (e: SQLException) {
            Log.e(javaClass.getSimpleName(), (if (e.message != null) e.message else "")!!)
        }
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase?, i: Int, i1: Int) {
    }

    override fun onOpen(db: SQLiteDatabase) {
        onCreate(db)
    }

    fun addUrl(
        url: String?,
        title: String,
        description: String,
        imageUrl: String?,
        isPic: Boolean
    ) {
        if (title.isEmpty() && description.isEmpty()) return
        val values = ContentValues()
        values.put("url", url)
        values.put("title", title)
        values.put("description", description)
        values.put("imageUrl", imageUrl)
        values.put("isPic", isPic)
        try {
            val db = getWritableDatabase()
            db.insertWithOnConflict("urls", null, values, SQLiteDatabase.CONFLICT_IGNORE)
            db.close()
        } catch (ignored: Exception) {
        }
    }

    @SuppressLint("Range")
    fun getUrl(url: String?): Vector<String?>? {
        try {
            val db = getReadableDatabase()
            val columns = arrayOf<String?>("url", "title", "description", "imageUrl", "isPic")
            val selection = "url = ?"
            val selectionArgs = arrayOf<String?>(url)

            val cursor = db.query("urls", columns, selection, selectionArgs, null, null, null)

            if (cursor.moveToFirst()) {
                // 新資料
                val data = Vector<String?>()
                data.add(cursor.getString(cursor.getColumnIndex("url")))
                data.add(cursor.getString(cursor.getColumnIndex("title")))
                data.add(cursor.getString(cursor.getColumnIndex("description")))
                data.add(cursor.getString(cursor.getColumnIndex("imageUrl")))
                data.add(cursor.getString(cursor.getColumnIndex("isPic")))

                cursor.close()
                db.close()

                return data
            } else {
                // 已有
                cursor.close()
                db.close()

                return null
            }
        } catch (ignored: Exception) {
            return null
        }
    }

    fun addShortenUrl(url: String?, title: String?, description: String?, shortenUrl: String?) {
        try {
            val db = getWritableDatabase()

            val values = ContentValues()
            values.put("shorten_url", shortenUrl)
            values.put("title", title)
            values.put("description", description)
            values.put("url", url)
            db.delete("shorten_urls", "shorten_url=?", arrayOf<String?>(shortenUrl))
            db.insertWithOnConflict("shorten_urls", null, values, SQLiteDatabase.CONFLICT_IGNORE)
            db.close()
        } catch (ignored: Exception) {
        }
    }

    @get:SuppressLint("Range")
    val shortenUrls: Vector<ShortenUrl?>
        get() {
            try {
                val db = getReadableDatabase()
                val columns =
                    arrayOf<String?>("shorten_url", "title", "description", "url")
                val selection = ""
                val selectionArgs = arrayOf<String?>("")

                val cursor =
                    db.query("shorten_urls", columns, selection, null, null, null, "rowid DESC")

                if (cursor.moveToFirst()) {
                    val returnList = Vector<ShortenUrl?>()
                    do {
                        val data = ShortenUrl()
                        data.setShorten_url(cursor.getString(cursor.getColumnIndex("shorten_url")))
                        data.setTitle(cursor.getString(cursor.getColumnIndex("title")))
                        data.setDescription(cursor.getString(cursor.getColumnIndex("description")))
                        data.setUrl(cursor.getString(cursor.getColumnIndex("url")))
                        returnList.add(data)
                    } while (cursor.moveToNext())

                    cursor.close()
                    db.close()

                    return returnList
                } else {
                    cursor.close()
                    db.close()

                    return Vector<ShortenUrl?>()
                }
            } catch (ignored: Exception) {
                return Vector<ShortenUrl?>()
            }
        }

    @SuppressLint("Range")
    fun getShortenUrl(url: String?): Vector<ShortenUrl?> {
        try {
            val db = getReadableDatabase()
            val columns = arrayOf<String?>("shorten_url", "title", "description", "url")
            val selection = "url = ?"
            val selectionArgs = arrayOf<String?>(url)

            val cursor = db.query(
                "shorten_urls",
                columns,
                selection,
                selectionArgs,
                null,
                null,
                "rowid DESC"
            )

            if (cursor.moveToFirst()) {
                val returnList = Vector<ShortenUrl?>()
                do {
                    val data = ShortenUrl()
                    data.setShorten_url(cursor.getString(cursor.getColumnIndex("shorten_url")))
                    data.setTitle(cursor.getString(cursor.getColumnIndex("title")))
                    data.setDescription(cursor.getString(cursor.getColumnIndex("description")))
                    data.setUrl(cursor.getString(cursor.getColumnIndex("url")))
                    returnList.add(data)
                } while (cursor.moveToNext())

                cursor.close()
                db.close()

                return returnList
            } else {
                cursor.close()
                db.close()

                return Vector<ShortenUrl?>()
            }
        } catch (ignored: Exception) {
            return Vector<ShortenUrl?>()
        }
    }

    fun clearDb() {
        try {
            val db = getWritableDatabase()
            db.execSQL("DELETE FROM urls WHERE rowid NOT IN (SELECT rowid FROM urls ORDER BY rowid DESC LIMIT 100) ")
            db.execSQL("DELETE FROM shorten_urls WHERE rowid NOT IN (SELECT rowid FROM shorten_urls ORDER BY rowid DESC LIMIT 100) ")
            onCreate(db)
        } catch (ignored: Exception) {
        }
    }
}
