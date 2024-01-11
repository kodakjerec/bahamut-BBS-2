package com.kumi.Bahamut.DataModels;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BookmarkStore {
  private Map<String, BookmarkList> _bookmarks = new HashMap<String, BookmarkList>();
  
  private Context _context = null;
  
  private byte[] _ext_data = new byte[0];
  
  private String _file_path = "";
  
  private BookmarkList _global_bookmarks = new BookmarkList("");
  
  private String _owner = "";
  
  private int _version = 1;
  
  public BookmarkStore(Context paramContext) {
    this._context = paramContext;
    if (this._context == null)
      System.out.println("Bookmark context can't null."); 
    load();
  }
  
  public BookmarkStore(Context paramContext, String paramString) {
    this._file_path = paramString;
    this._context = paramContext;
  }
  
  private void load() {
    System.out.println("load bookmark store from file");
    boolean bool = false;
    if (this._file_path != null && this._file_path.length() > 0) {
      File file = new File(this._file_path);
      if (file.exists()) {
        System.out.println("bookmark file exists");
        try {
          FileInputStream fileInputStream = new FileInputStream();
          this(file);
          ObjectInputStream objectInputStream = new ObjectInputStream();
          this(fileInputStream);
          importFromStream(objectInputStream);
          objectInputStream.close();
          fileInputStream.close();
        } catch (IOException iOException) {
          iOException.printStackTrace();
        } 
        file.delete();
        bool = true;
      } 
    } else {
      System.out.println("bookmark file not exists");
    } 
    if (bool)
      store(); 
    if (!bool && this._context != null) {
      SharedPreferences sharedPreferences = this._context.getSharedPreferences("bookmark", 0);
      if (sharedPreferences != null) {
        String str = sharedPreferences.getString("save_data", "");
        if (str != null && str.length() > 0)
          try {
            JSONObject jSONObject = new JSONObject();
            this(str);
            importFromJSON(jSONObject);
          } catch (JSONException jSONException) {
            jSONException.printStackTrace();
          }  
      } 
    } 
  }
  
  public static void upgrade(Context paramContext, String paramString) {
    (new BookmarkStore(paramContext, paramString)).load();
  }
  
  public void addBookmark(Bookmark paramBookmark) {
    String str = paramBookmark.getBoard().trim();
    if (str.length() == 0) {
      this._global_bookmarks.addBookmark(paramBookmark);
      return;
    } 
    if (paramBookmark.optional.equals("1")) {
      getBookmarkList(str).addHistoryBookmark(paramBookmark);
      return;
    } 
    getBookmarkList(str).addBookmark(paramBookmark);
  }
  
  public void cleanBookmark() {
    this._bookmarks.clear();
    this._global_bookmarks.clear();
  }
  
  public JSONObject exportToJSON() {
    JSONObject jSONObject = new JSONObject();
    try {
      jSONObject.put("version", this._version);
      jSONObject.put("owner", this._owner);
      JSONArray jSONArray = new JSONArray();
      this();
      Iterator<Bookmark> iterator = getTotalBookmarkList().iterator();
      while (iterator.hasNext())
        jSONArray.put(((Bookmark)iterator.next()).exportToJSON()); 
    } catch (Exception exception) {
      exception.printStackTrace();
      return jSONObject;
    } 
    jSONObject.put("data", exception);
    return jSONObject;
  }
  
  public BookmarkList getBookmarkList(String paramString) {
    if (!this._bookmarks.containsKey(paramString)) {
      BookmarkList bookmarkList = new BookmarkList(paramString);
      this._bookmarks.put(paramString, bookmarkList);
      return bookmarkList;
    } 
    return this._bookmarks.get(paramString);
  }
  
  public Vector<Bookmark> getTotalBookmarkList() {
    Vector<Bookmark> vector = new Vector();
    Iterator<String> iterator = this._bookmarks.keySet().iterator();
    while (iterator.hasNext()) {
      BookmarkList bookmarkList = getBookmarkList(iterator.next());
      byte b1;
      for (b1 = 0; b1 < bookmarkList.getBookmarkSize(); b1++) {
        Bookmark bookmark = bookmarkList.getBookmark(b1);
        bookmark.index = b1;
        bookmark.optional = "0";
        vector.add(bookmark);
      } 
      for (b1 = 0; b1 < bookmarkList.getHistoryBookmarkSize(); b1++) {
        Bookmark bookmark = bookmarkList.getHistoryBookmark(b1);
        bookmark.index = b1;
        bookmark.optional = "1";
        vector.add(bookmark);
      } 
    } 
    for (byte b = 0; b < this._global_bookmarks.getBookmarkSize(); b++) {
      Bookmark bookmark = this._global_bookmarks.getBookmark(b);
      bookmark.index = b;
      vector.add(bookmark);
    } 
    return vector;
  }
  
  public void importFromJSON(JSONObject paramJSONObject) {
    cleanBookmark();
    try {
      this._version = paramJSONObject.getInt("version");
      this._owner = paramJSONObject.getString("owner");
      JSONArray jSONArray = paramJSONObject.getJSONArray("data");
      for (byte b = 0; b < jSONArray.length(); b++) {
        JSONObject jSONObject = jSONArray.getJSONObject(b);
        Bookmark bookmark = new Bookmark();
        this(jSONObject);
        addBookmark(bookmark);
      } 
    } catch (Exception exception) {
      exception.printStackTrace();
    } 
    sortBookmarks();
    Vector<Bookmark> vector = getTotalBookmarkList();
    System.out.println("load " + vector.size() + " bookmarks.");
  }
  
  public void importFromStream(ObjectInputStream paramObjectInputStream) throws StreamCorruptedException, IOException {
    cleanBookmark();
    this._version = paramObjectInputStream.readInt();
    this._owner = paramObjectInputStream.readUTF();
    int i = paramObjectInputStream.readInt();
    for (byte b = 0; b < i; b++)
      addBookmark(new Bookmark(paramObjectInputStream)); 
    this._ext_data = new byte[paramObjectInputStream.readInt()];
    paramObjectInputStream.read(this._ext_data);
  }
  
  public void notifyDataUpdated() {}
  
  void sortBookmarks() {
    Iterator<String> iterator = this._bookmarks.keySet().iterator();
    while (iterator.hasNext())
      getBookmarkList(iterator.next()).sort(); 
    this._global_bookmarks.sort();
  }
  
  public void store() {
    System.out.println("save bookmark store to file");
    if (this._context != null) {
      JSONObject jSONObject = exportToJSON();
      if (jSONObject != null) {
        String str = jSONObject.toString();
        this._context.getSharedPreferences("bookmark", 0).edit().putString("save_data", str).commit();
      } 
    } 
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\DataModels\BookmarkStore.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */