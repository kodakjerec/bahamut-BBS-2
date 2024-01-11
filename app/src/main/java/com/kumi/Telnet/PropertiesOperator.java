package com.kumi.Telnet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesOperator {
  private Properties _property = new Properties();
  
  private String _save_path = "";
  
  public PropertiesOperator(String paramString) {
    this._save_path = paramString;
  }
  
  public boolean getPropertiesBoolean(String paramString) {
    boolean bool2 = false;
    paramString = this._property.getProperty(paramString);
    boolean bool1 = bool2;
    if (paramString != null)
      try {
        bool1 = Boolean.parseBoolean(paramString);
      } catch (Exception exception) {
        exception.printStackTrace();
        bool1 = bool2;
      }  
    return bool1;
  }
  
  public double getPropertiesDouble(String paramString) {
    double d2 = 0.0D;
    paramString = this._property.getProperty(paramString);
    double d1 = d2;
    if (paramString != null)
      try {
        d1 = Double.parseDouble(paramString);
      } catch (Exception exception) {
        exception.printStackTrace();
        d1 = d2;
      }  
    return d1;
  }
  
  public float getPropertiesFloat(String paramString) {
    float f2 = 0.0F;
    paramString = this._property.getProperty(paramString);
    float f1 = f2;
    if (paramString != null)
      try {
        f1 = Float.parseFloat(paramString);
      } catch (Exception exception) {
        exception.printStackTrace();
        f1 = f2;
      }  
    return f1;
  }
  
  public int getPropertiesInteger(String paramString) {
    byte b = 0;
    paramString = this._property.getProperty(paramString);
    int i = b;
    if (paramString != null)
      try {
        i = Integer.parseInt(paramString);
      } catch (Exception exception) {
        exception.printStackTrace();
        i = b;
      }  
    return i;
  }
  
  public String getPropertiesString(String paramString) {
    String str = this._property.getProperty(paramString);
    paramString = str;
    if (str == null)
      paramString = ""; 
    return paramString;
  }
  
  public String load() {
    FileInputStream fileInputStream = null;
    if (this._save_path != null) {
      FileInputStream fileInputStream1;
      try {
        File file = new File();
        this(this._save_path);
        fileInputStream1 = fileInputStream;
        if (file.exists()) {
          fileInputStream1 = new FileInputStream();
          this(file);
          this._property.loadFromXML(fileInputStream1);
          fileInputStream1.close();
          fileInputStream1 = fileInputStream;
        } 
      } catch (FileNotFoundException fileNotFoundException) {
        fileNotFoundException.printStackTrace();
        fileInputStream1 = fileInputStream;
      } catch (IOException iOException) {
        iOException.printStackTrace();
        fileInputStream1 = fileInputStream;
      } 
      return (String)fileInputStream1;
    } 
    return "save path is null";
  }
  
  public void setProperties(String paramString, double paramDouble) {
    this._property.setProperty(paramString, String.valueOf(paramDouble));
  }
  
  public void setProperties(String paramString, float paramFloat) {
    this._property.setProperty(paramString, String.valueOf(paramFloat));
  }
  
  public void setProperties(String paramString, int paramInt) {
    this._property.setProperty(paramString, String.valueOf(paramInt));
  }
  
  public void setProperties(String paramString1, String paramString2) {
    this._property.setProperty(paramString1, paramString2);
  }
  
  public void setProperties(String paramString, boolean paramBoolean) {
    this._property.setProperty(paramString, String.valueOf(paramBoolean));
  }
  
  public String store() {
    null = null;
    if (this._save_path != null) {
      try {
        File file = new File();
        this(this._save_path);
        if (file.exists())
          file.delete(); 
        FileOutputStream fileOutputStream = new FileOutputStream();
        this(file);
        this._property.storeToXML(fileOutputStream, "UserSettings");
        fileOutputStream.close();
      } catch (FileNotFoundException fileNotFoundException) {
        fileNotFoundException.printStackTrace();
      } catch (IOException iOException) {
        iOException.printStackTrace();
      } 
      return null;
    } 
    return "save path is null";
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\PropertiesOperator.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */