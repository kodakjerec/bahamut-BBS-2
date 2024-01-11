package com.kumi.Bahamut.DataModels;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.kumi.Telnet.TelnetClient;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppDatabase extends SQLiteOpenHelper {
  public AppDatabase(Context paramContext) {
    super(paramContext, TelnetClient.getClient().getUsername().toLowerCase().trim() + "_database", null, 1);
  }
  
  public List<Map<String, Object>> loadMessages() {
    SQLiteDatabase sQLiteDatabase = getReadableDatabase();
    Cursor cursor = sQLiteDatabase.query("messages", null, null, null, null, null, "received_date DESC", "0,100");
    ArrayList<HashMap<Object, Object>> arrayList = new ArrayList();
    if (cursor.moveToFirst())
      while (true) {
        long l1;
        long l2 = cursor.getLong(cursor.getColumnIndex("message_id"));
        String str1 = cursor.getString(cursor.getColumnIndex("sender_name"));
        String str2 = cursor.getString(cursor.getColumnIndex("message"));
        long l3 = cursor.getLong(cursor.getColumnIndex("received_date"));
        if (cursor.isNull(cursor.getColumnIndex("read_date"))) {
          l1 = 0L;
        } else {
          l1 = cursor.getLong(cursor.getColumnIndex("read_date"));
        } 
        HashMap<Object, Object> hashMap = new HashMap<Object, Object>();
        hashMap.put("message_id", Long.valueOf(l2));
        hashMap.put("sender_name", str1);
        hashMap.put("message", str2);
        hashMap.put("received_date", new Date(l3));
        if (l1 > 0L)
          hashMap.put("read_date", new Date(l1)); 
        arrayList.add(hashMap);
        if (!cursor.moveToNext()) {
          sQLiteDatabase.close();
          return (List)arrayList;
        } 
      }  
    sQLiteDatabase.close();
    return (List)arrayList;
  }
  
  public void onCreate(SQLiteDatabase paramSQLiteDatabase) {
    try {
      paramSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS messages ( message_id    INTEGER PRIMARY KEY AUTOINCREMENT ,  sender_name   TEXT  NOT NULL ,  message       TEXT  NOT NULL ,  received_date INTEGER ,  read_date     INTEGER );");
    } catch (SQLException sQLException) {
      sQLException.printStackTrace();
    } 
  }
  
  public void onUpgrade(SQLiteDatabase paramSQLiteDatabase, int paramInt1, int paramInt2) {}
  
  public void saveMessage(String paramString1, String paramString2) {
    ContentValues contentValues = new ContentValues();
    contentValues.put("sender_name", paramString1);
    contentValues.put("message", paramString2);
    contentValues.put("received_date", Long.valueOf((new Date()).getTime()));
    SQLiteDatabase sQLiteDatabase = getWritableDatabase();
    sQLiteDatabase.insert("messages", null, contentValues);
    sQLiteDatabase.close();
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\DataModels\AppDatabase.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */