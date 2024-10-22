package com.kota.Bahamut.Pages.Messages

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.kota.Bahamut.Service.TempSettings.setNotReadMessageCount
import com.kota.Telnet.TelnetClient
import java.util.Date
import java.util.Locale

class MessageDatabase(context: Context?) :
    SQLiteOpenHelper(context, TelnetClient.getClient().username.lowercase(
        Locale.getDefault()
    ).trim { it <= ' ' } + "_database_msg", null, 1) {
    override fun onCreate(aDatabase: SQLiteDatabase) {
        try {
            val createTableQuery = "CREATE TABLE IF NOT EXISTS messages (" +
                    "message_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "sender_name TEXT NOT NULL, " +
                    "message TEXT NOT NULL, " +
                    "received_date INTEGER, " +
                    "read_date INTEGER," +
                    "type INTEGER" +
                    ")"
            aDatabase.execSQL(createTableQuery)
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun onUpgrade(aDatabase: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    override fun onOpen(db: SQLiteDatabase) {
        onCreate(db)
    }

    /** 收到訊息  */
    fun receiveMessage(aSenderName: String, aMessage: String, iType: Int): BahaMessage {
        val values = ContentValues()
        values.put("sender_name", aSenderName)
        values.put("message", aMessage)
        values.put("received_date", Date().time)
        values.put("type", iType)

        try {
            val db = writableDatabase
            db.insert("messages", null, values)
            db.close()
        } catch (ignored: Exception) { }

        val messageObj = BahaMessage()
        messageObj.senderName = aSenderName
        messageObj.message = aMessage
        messageObj.receivedDate = Date().time
        messageObj.type = iType
        return messageObj
    }

    /** 更新訊息 */
    fun syncMessage(aSenderName: String, aMessage: String, iType: Int) {
        val values = ContentValues()
        values.put("sender_name", aSenderName)
        values.put("message", aMessage)
        values.put("received_date", Date().time)
        values.put("type", iType)

        try {
            val db = writableDatabase
            val selection = "sender_name = ? AND message = ?"
            val selectionArgs = arrayOf(aSenderName, aMessage)
            // 查詢是否存在
            val cursor = db.query("messages", null, selection, selectionArgs, null, null, null)

            if (cursor.moveToFirst()) {
                // 存在，跳過
            } else {
                // 不存在，執行插入
                db.insert("messages", null, values)
            }

            cursor.close()
            db.close()
        } catch (ignored: Exception) { }
    }

    /** 更新讀取日期  */
    fun updateReceiveMessage(aSenderName: String) {
        try {
            val db = writableDatabase
            db.execSQL(
                "UPDATE messages SET read_date = ? WHERE sender_name = ? AND read_date is null",
                arrayOf<Any>(
                    Date().time, aSenderName
                )
            )
            db.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** 送出訊息  */
    fun sendMessage(aSenderName: String, aMessage: String): BahaMessage {
        val values = ContentValues()
        values.put("sender_name", aSenderName)
        values.put("message", aMessage)
        values.put("received_date", Date().time)
        values.put("read_date", Date().time)
        values.put("type", 1)
        try {
            val db = writableDatabase
            db.insert("messages", null, values)
            db.close()
        } catch (ignored: Exception) { }

        val messageObj = BahaMessage()
        messageObj.senderName = aSenderName
        messageObj.message = aMessage
        messageObj.receivedDate = Date().time
        messageObj.type = 1
        return messageObj
    }

    /** 列出各ID最新的訊息  */
    @SuppressLint("Range")
    fun getAllAndNewestMessage(): List<BahaMessageSummarize> {
        var totalUnreadCount = 0
        val returnList: MutableList<BahaMessageSummarize> = ArrayList()
        try {
            val db = readableDatabase
            val subQuery =
                "(SELECT message FROM messages m2 WHERE m2.sender_name = m1.sender_name ORDER BY message_id DESC LIMIT 1)"
            val columns = arrayOf(
                "sender_name",
                "MAX(received_date) AS latest_received_date",
                "$subQuery AS latest_message",
                "COUNT(CASE WHEN read_date IS NULL THEN 1 END) AS unread_count"
            )
            val selection = ""
            val groupBy = "sender_name"
            val orderBy = "MAX(message_id) DESC"
            val cursor =
                db.query("messages AS m1", columns, selection, null, groupBy, null, orderBy)
            if (cursor.moveToFirst()) {
                do {
                    val data = BahaMessageSummarize()
                    data.senderName = cursor.getString(cursor.getColumnIndex("sender_name"))
                    data.message = cursor.getString(cursor.getColumnIndex("latest_message"))
                    data.receivedDate =
                        cursor.getLong(cursor.getColumnIndex("latest_received_date"))
                    val unreadCount = cursor.getInt(cursor.getColumnIndex("unread_count"))
                    data.unReadCount = unreadCount
                    totalUnreadCount += unreadCount
                    returnList.add(data)
                } while (cursor.moveToNext())
                cursor.close()
                db.close()
            } else {
                cursor.close()
                db.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        setNotReadMessageCount(totalUnreadCount)
        return returnList
    }
    @SuppressLint("Range")
    fun getIdNewestMessage(aSenderName: String): BahaMessageSummarize {
        val returnObject = BahaMessageSummarize()
        try {
            val db = readableDatabase
            val subQuery =
                "(SELECT message FROM messages m2 WHERE m2.sender_name = m1.sender_name ORDER BY message_id DESC LIMIT 1)"
            val columns = arrayOf(
                "sender_name",
                "MAX(received_date) AS latest_received_date",
                "$subQuery AS latest_message",
                "COUNT(CASE WHEN read_date IS NULL THEN 1 END) AS unread_count"
            )
            val selection = "sender_name=?"
            val selectionArgs = arrayOf(aSenderName)
            val groupBy = "sender_name"
            val orderBy = "MAX(message_id) DESC"
            val cursor =
                db.query("messages AS m1", columns, selection, selectionArgs, groupBy, null, orderBy)
            if (cursor.moveToFirst()) {
                do {
                    returnObject.senderName = cursor.getString(cursor.getColumnIndex("sender_name"))
                    returnObject.message = cursor.getString(cursor.getColumnIndex("latest_message"))
                    returnObject.receivedDate =
                        cursor.getLong(cursor.getColumnIndex("latest_received_date"))
                    val unreadCount = cursor.getInt(cursor.getColumnIndex("unread_count"))
                    returnObject.unReadCount = unreadCount
                } while (cursor.moveToNext())
                cursor.close()
                db.close()
            } else {
                cursor.close()
                db.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return returnObject
    }
    /** 列出指定ID最新的訊息  */
    @SuppressLint("Range")
    fun getIdMessage(aSenderName: String): List<BahaMessage> {
        return try {
            val db = readableDatabase
            val columns = arrayOf("message_id","sender_name", "message", "received_date", "read_date", "type")
            val selection = "sender_name=?"
            val selectionArgs = arrayOf(aSenderName)
            val orderBy = "message_id ASC"
            val cursor =
                db.query("messages", columns, selection, selectionArgs, null, null, orderBy)
            if (cursor.moveToFirst()) {
                val returnList: MutableList<BahaMessage> = ArrayList()
                do {
                    val data = BahaMessage()
                    data.senderName = cursor.getString(cursor.getColumnIndex("sender_name"))
                    data.message = cursor.getString(cursor.getColumnIndex("message"))
                    data.receivedDate = cursor.getLong(cursor.getColumnIndex("received_date"))
                    data.readDate = cursor.getLong(cursor.getColumnIndex("read_date"))
                    data.type = cursor.getInt(cursor.getColumnIndex("type"))
                    returnList.add(data)
                } while (cursor.moveToNext())
                cursor.close()
                db.close()
                updateReceiveMessage(aSenderName)
                returnList
            } else {
                cursor.close()
                db.close()
                ArrayList()
            }
        } catch (ignored: Exception) {
            ArrayList()
        }
    }

    /** 清除所有紀錄  */
    fun clearDb() {
        try {
            setNotReadMessageCount(0)
            val db = writableDatabase
            db.execSQL("DELETE FROM messages ")
            onCreate(db)
        } catch (ignored: Exception) {
        }
    }
}
