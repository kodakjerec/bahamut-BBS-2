package com.kumi.Bahamut.DataModels;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import org.json.JSONException;
import org.json.JSONObject;

public class ArticleTemp {
  public static final int version = 1;
  
  public String content = "";
  
  public String header = "";
  
  public String title = "";
  
  public JSONObject exportToJSON() throws JSONException {
    JSONObject jSONObject = new JSONObject();
    jSONObject.put("header", this.header);
    jSONObject.put("title", this.title);
    jSONObject.put("content", this.content);
    return jSONObject;
  }
  
  public void exportToStream(ObjectOutputStream paramObjectOutputStream) throws IOException {
    paramObjectOutputStream.write(1);
    paramObjectOutputStream.writeUTF(this.header);
    paramObjectOutputStream.writeUTF(this.title);
    paramObjectOutputStream.writeUTF(this.content);
  }
  
  public void importFromJSON(JSONObject paramJSONObject) throws JSONException {
    this.header = paramJSONObject.getString("header");
    this.title = paramJSONObject.getString("title");
    this.content = paramJSONObject.getString("content");
  }
  
  public void importFromStream(ObjectInputStream paramObjectInputStream) throws StreamCorruptedException, IOException {
    paramObjectInputStream.readInt();
    this.header = paramObjectInputStream.readUTF();
    this.title = paramObjectInputStream.readUTF();
    this.content = paramObjectInputStream.readUTF();
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\DataModels\ArticleTemp.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */