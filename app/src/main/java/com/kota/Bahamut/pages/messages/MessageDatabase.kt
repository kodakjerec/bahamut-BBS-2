package com.kota.Bahamut.pages.messages

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.kota.Bahamut.service.TempSettings.setNotReadMessageCount
import com.kota.telnet.TelnetClient
import java.util.Date
import java.util.Locale

class MessageDatabase(context: Context?) :
    SQLiteOpenHelper(context, TelnetClient.client?.username?.lowercase(
        Locale.getDefault()
    )?.trim { it <= ' ' } + "_database_msg", null, 1) {
    override fun onCreate(aDatabase: SQLiteDatabase) {
        try {
            val createTableQuery = "CREATE TABLE IF NOT EXISTS messages (" +
                    "message_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "sender_name TEXT NOT NULL, " +
                    "message TEXT NOT NULL, " +
                    "received_date INTEGER, " +
                    "read_date INTEGER," +
                    "type INTEGER," +
                    "status INTEGER"+
                    ")"
            aDatabase.execSQL(createTableQuery)
        } catch (e: SQLException) {
            Log.e(javaClass.simpleName, e.message.toString())
        }
    }

    override fun onUpgrade(aDatabase: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    override fun onOpen(db: SQLiteDatabase) {
        onCreate(db)
    }

    private fun stringToStatus(fromString: String): MessageStatus {
        return when(fromString) {
            "Default" -> MessageStatus.Default
            "Success" -> MessageStatus.Success
            "CloseBBCall" -> MessageStatus.CloseBBCall
            "Escape" -> MessageStatus.Escape
            "Offline" -> MessageStatus.Offline
            else -> MessageStatus.Unknown
        }
    }

    /** 收到訊息  */
    fun receiveMessage(aSenderName: String, aMessage: String, iType: Int): BahaMessage {
        val values = ContentValues()
        values.put("sender_name", aSenderName)
        values.put("message", aMessage)
        values.put("received_date", Date().time)
        values.put("type", iType)
        values.put("status", MessageStatus.Success.toString() )

        try {
            val db = writableDatabase
            db.insert("messages", null, values)
            db.close()
        } catch (_: Exception) { }

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
        values.put("status", MessageStatus.Success.toString())

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
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, e.message.toString())
        }
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
            Log.e(javaClass.simpleName, e.message.toString())
        }
    }

    /** 送出訊息  */
    fun sendMessage(aSenderName: String, aMessage: String): BahaMessage? {
        val values = ContentValues()
        values.put("sender_name", aSenderName)
        values.put("message", aMessage)
        values.put("received_date", Date().time)
        values.put("read_date", Date().time)
        values.put("type", 1)

        val messageObj = BahaMessage()
        messageObj.senderName = aSenderName
        messageObj.message = aMessage
        messageObj.receivedDate = Date().time
        messageObj.type = 1

        try {
            val db = writableDatabase
            messageObj.id = db.insert("messages", null, values)
            db.close()
        } catch (_: Exception) { return null }

        return messageObj
    }
    /** 更新送出後的結果 */
    fun updateSendMessage(aMessage: BahaMessage) {
        try {
            val db = writableDatabase
            db.execSQL(
                "UPDATE messages SET status = ? WHERE message_id = ?",
                arrayOf<Any>(
                    aMessage.status, aMessage.id
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** 列出各ID最新的訊息  */
    @SuppressLint("Range")
    fun getAllAndNewestMessage(): MutableList<BahaMessageSummarize> {
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
        } catch (_: Exception) { }
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
            Log.e(javaClass.simpleName, e.message.toString())
        }
        return returnObject
    }
    /** 列出指定ID最新的訊息  */
    @SuppressLint("Range")
    fun getIdMessage(aSenderName: String): MutableList<BahaMessage> {
        return try {
            val db = readableDatabase
            val columns = arrayOf("message_id","sender_name", "message", "received_date", "read_date", "type", "status")
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
                    data.status = stringToStatus(cursor.getString(cursor.getColumnIndex("status")))
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
        } catch (_: Exception) {
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
        } catch (_: Exception) {
        }
    }
}
