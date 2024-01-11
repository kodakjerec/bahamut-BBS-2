package com.kumi.Bahamut.DataModels;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.Iterator;
import java.util.Vector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ArticleTempStore {
  public static final int version = 1;
  
  private Context _context;
  
  private String _file_path = null;
  
  public Vector<ArticleTemp> articles = new Vector<ArticleTemp>();
  
  public ArticleTempStore(Context paramContext) {
    this._context = paramContext;
    this.articles.add(new ArticleTemp());
    this.articles.add(new ArticleTemp());
    this.articles.add(new ArticleTemp());
    this.articles.add(new ArticleTemp());
    this.articles.add(new ArticleTemp());
    this.articles.add(new ArticleTemp());
    this.articles.add(new ArticleTemp());
    this.articles.add(new ArticleTemp());
    this.articles.add(new ArticleTemp());
    this.articles.add(new ArticleTemp());
    this.articles.add(new ArticleTemp());
    load();
  }
  
  public ArticleTempStore(Context paramContext, String paramString) {
    this._context = paramContext;
    this._file_path = paramString;
    this.articles.add(new ArticleTemp());
    this.articles.add(new ArticleTemp());
    this.articles.add(new ArticleTemp());
    this.articles.add(new ArticleTemp());
    this.articles.add(new ArticleTemp());
    this.articles.add(new ArticleTemp());
    this.articles.add(new ArticleTemp());
    this.articles.add(new ArticleTemp());
    this.articles.add(new ArticleTemp());
    this.articles.add(new ArticleTemp());
    this.articles.add(new ArticleTemp());
  }
  
  public static void upgrade(Context paramContext, String paramString) {
    (new ArticleTempStore(paramContext, paramString)).load();
  }
  
  public JSONObject exportToJSON() throws JSONException {
    JSONObject jSONObject = new JSONObject();
    JSONArray jSONArray = new JSONArray();
    Iterator<ArticleTemp> iterator = this.articles.iterator();
    while (iterator.hasNext())
      jSONArray.put(((ArticleTemp)iterator.next()).exportToJSON()); 
    jSONObject.put("data", jSONArray);
    return jSONObject;
  }
  
  public void importFromJSON(JSONObject paramJSONObject) throws JSONException {
    JSONArray jSONArray = paramJSONObject.getJSONArray("data");
    this.articles.clear();
    for (byte b = 0; b < jSONArray.length(); b++) {
      JSONObject jSONObject = jSONArray.getJSONObject(b);
      ArticleTemp articleTemp = new ArticleTemp();
      articleTemp.importFromJSON(jSONObject);
      this.articles.add(articleTemp);
    } 
  }
  
  public void importFromStream(ObjectInputStream paramObjectInputStream) throws StreamCorruptedException, IOException {
    paramObjectInputStream.readInt();
    int i = paramObjectInputStream.readInt();
    for (byte b = 0; b < i; b++) {
      ArticleTemp articleTemp = new ArticleTemp();
      articleTemp.importFromStream(paramObjectInputStream);
      this.articles.add(articleTemp);
    } 
  }
  
  public void load() {
    System.out.println("load article store from file");
    boolean bool2 = false;
    boolean bool1 = bool2;
    if (this._file_path != null) {
      bool1 = bool2;
      if (this._file_path.length() > 0) {
        File file = new File(this._file_path);
        bool1 = bool2;
        if (file.exists())
          try {
            FileInputStream fileInputStream = new FileInputStream();
            this(file);
            ObjectInputStream objectInputStream = new ObjectInputStream();
            this(fileInputStream);
            importFromStream(objectInputStream);
            objectInputStream.close();
            fileInputStream.close();
            file.delete();
            bool1 = true;
          } catch (IOException iOException) {
            iOException.printStackTrace();
            bool1 = bool2;
          }  
      } 
    } 
    if (bool1)
      store(); 
    if (!bool1 && this._context != null)
      try {
        String str = this._context.getSharedPreferences("article_temp", 0).getString("save_data", "");
        if (str != null && str.length() > 0) {
          JSONObject jSONObject = new JSONObject();
          this(str);
          importFromJSON(jSONObject);
        } 
      } catch (Exception exception) {
        exception.printStackTrace();
      }  
  }
  
  public void store() {
    if (this._context != null)
      try {
        SharedPreferences sharedPreferences = this._context.getSharedPreferences("article_temp", 0);
        String str = exportToJSON().toString();
        sharedPreferences.edit().putString("save_data", str).commit();
      } catch (Exception exception) {
        exception.printStackTrace();
      }  
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\DataModels\ArticleTempStore.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */