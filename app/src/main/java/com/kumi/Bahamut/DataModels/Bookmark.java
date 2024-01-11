package com.kumi.Bahamut.DataModels;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import org.json.JSONException;
import org.json.JSONObject;

public class Bookmark {
  public static final String OPTIONAL_BOOKMARK = "0";
  
  public static final String OPTIONAL_HOSTORY = "1";
  
  public static final int version = 1;
  
  private String _author = "";
  
  private String _board = "";
  
  private String _detail = "";
  
  private byte[] _ext_data = new byte[0];
  
  private String _gy = "";
  
  private String _keyword = "";
  
  private String _mark = "n";
  
  private String _title = "";
  
  public int index = 0;
  
  public String optional = "0";
  
  public int weight = 0;
  
  public Bookmark() {}
  
  public Bookmark(ObjectInputStream paramObjectInputStream) throws StreamCorruptedException, IOException {
    importFromStream(paramObjectInputStream);
  }
  
  public Bookmark(JSONObject paramJSONObject) throws JSONException {
    importFromJSON(paramJSONObject);
  }
  
  public JSONObject exportToJSON() throws Exception {
    JSONObject jSONObject = new JSONObject();
    jSONObject.put("version", 1);
    jSONObject.put("index", this.index);
    jSONObject.put("optional", this.optional);
    jSONObject.put("board", this._board);
    jSONObject.put("title", this._title);
    jSONObject.put("weight", this.weight);
    jSONObject.put("detail", this._detail);
    jSONObject.put("keyword", this._keyword);
    jSONObject.put("author", this._author);
    jSONObject.put("mark", this._mark);
    jSONObject.put("gy", this._gy);
    return jSONObject;
  }
  
  public String generateTitle() {
    String str1 = "";
    if (this._keyword.trim().length() > 0)
      str1 = "標題:" + this._keyword; 
    String str2 = str1;
    if (str1.length() == 0) {
      str2 = str1;
      if (this._author.trim().length() > 0)
        str2 = "作者:" + this._author; 
    } 
    str1 = str2;
    if (str2.length() == 0) {
      str1 = str2;
      if (this._gy.trim().length() > 0)
        str1 = "推薦:" + this._gy; 
    } 
    str2 = str1;
    if (this._mark.equals("y"))
      str2 = "M " + str1; 
    str1 = str2;
    if (str2.length() == 0)
      str1 = "未指定"; 
    return str1;
  }
  
  public String getAuthor() {
    return this._author;
  }
  
  public String getBoard() {
    return this._board;
  }
  
  public String getGy() {
    return this._gy;
  }
  
  public String getKeyword() {
    return this._keyword;
  }
  
  public String getMark() {
    return this._mark;
  }
  
  public String getTitle() {
    return this._title;
  }
  
  public void importFromJSON(JSONObject paramJSONObject) throws JSONException {
    int i;
    if (paramJSONObject.isNull("index")) {
      i = 0;
    } else {
      i = paramJSONObject.getInt("index");
    } 
    this.index = i;
    if (this.index == 0)
      System.out.print("index:0"); 
    this.optional = paramJSONObject.getString("optional");
    this._board = paramJSONObject.getString("board");
    this._title = paramJSONObject.getString("title");
    this.weight = paramJSONObject.getInt("weight");
    this._detail = paramJSONObject.getString("detail");
    this._keyword = paramJSONObject.getString("keyword");
    this._author = paramJSONObject.getString("author");
    this._mark = paramJSONObject.getString("mark");
    this._gy = paramJSONObject.getString("gy");
  }
  
  public void importFromStream(ObjectInputStream paramObjectInputStream) throws StreamCorruptedException, IOException {
    paramObjectInputStream.readInt();
    this.index = 0;
    this.optional = paramObjectInputStream.readUTF();
    this._board = paramObjectInputStream.readUTF();
    this._title = paramObjectInputStream.readUTF();
    this.weight = paramObjectInputStream.readInt();
    this._detail = paramObjectInputStream.readUTF();
    this._keyword = paramObjectInputStream.readUTF();
    this._author = paramObjectInputStream.readUTF();
    this._mark = paramObjectInputStream.readUTF();
    this._gy = paramObjectInputStream.readUTF();
    this._ext_data = new byte[paramObjectInputStream.readInt()];
    paramObjectInputStream.read(this._ext_data);
  }
  
  public void setAuthor(String paramString) {
    if (paramString == null) {
      this._author = "";
      return;
    } 
    this._author = paramString;
  }
  
  public void setBoard(String paramString) {
    if (paramString == null) {
      this._board = "";
      return;
    } 
    this._board = paramString;
  }
  
  public void setGy(String paramString) {
    if (paramString == null) {
      this._gy = "";
      return;
    } 
    this._gy = paramString;
  }
  
  public void setKeyword(String paramString) {
    if (paramString == null) {
      this._keyword = "";
      return;
    } 
    this._keyword = paramString;
  }
  
  public void setMark(String paramString) {
    if (paramString == null || !paramString.equals("y")) {
      this._mark = "n";
      return;
    } 
    this._mark = paramString;
  }
  
  public void setTitle(String paramString) {
    if (paramString == null) {
      this._title = "";
      return;
    } 
    this._title = paramString;
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\DataModels\Bookmark.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */