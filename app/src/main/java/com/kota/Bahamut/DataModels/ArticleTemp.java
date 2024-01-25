package com.kota.Bahamut.DataModels;

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
        JSONObject obj = new JSONObject();
        obj.put("header", this.header);
        obj.put("title", this.title);
        obj.put("content", this.content);
        return obj;
    }

    public void importFromJSON(JSONObject obj) throws JSONException {
        this.header = obj.getString("header");
        this.title = obj.getString("title");
        this.content = obj.getString("content");
    }

    public void exportToStream(ObjectOutputStream aStream) throws IOException {
        aStream.write(1);
        aStream.writeUTF(this.header);
        aStream.writeUTF(this.title);
        aStream.writeUTF(this.content);
    }

    public void importFromStream(ObjectInputStream aStream) throws IOException {
        aStream.readInt();
        this.header = aStream.readUTF();
        this.title = aStream.readUTF();
        this.content = aStream.readUTF();
    }
}
