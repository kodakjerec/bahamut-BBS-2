package com.kota.Telnet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class PropertiesOperator {
    private Properties _property = new Properties();
    private String _save_path = "";

    public PropertiesOperator(String aSavePath) {
        this._save_path = aSavePath;
    }

    public String store() {
        if (this._save_path == null) {
            return "save path is null";
        }
        try {
            File file = new File(this._save_path);
            if (file.exists()) {
                file.delete();
            }
            OutputStream fos = new FileOutputStream(file);
            this._property.storeToXML(fos, "UserSettings");
            fos.close();
            return null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public String load() {
        if (this._save_path == null) {
            return "save path is null";
        }
        try {
            File file = new File(this._save_path);
            if (!file.exists()) {
                return null;
            }
            InputStream fis = new FileInputStream(file);
            this._property.loadFromXML(fis);
            fis.close();
            return null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public String getPropertiesString(String key) {
        String str = this._property.getProperty(key);
        if (str == null) {
            return "";
        }
        return str;
    }

    public int getPropertiesInteger(String key) {
        String value = this._property.getProperty(key);
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean getPropertiesBoolean(String key) {
        String value = this._property.getProperty(key);
        if (value == null) {
            return false;
        }
        try {
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public float getPropertiesFloat(String key) {
        String value = this._property.getProperty(key);
        if (value == null) {
            return 0.0f;
        }
        try {
            return Float.parseFloat(value);
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0f;
        }
    }

    public double getPropertiesDouble(String key) {
        String value = this._property.getProperty(key);
        if (value == null) {
            return 0.0d;
        }
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0d;
        }
    }

    public void setProperties(String key, String value) {
        this._property.setProperty(key, value);
    }

    public void setProperties(String key, int value) {
        this._property.setProperty(key, String.valueOf(value));
    }

    public void setProperties(String key, boolean value) {
        this._property.setProperty(key, String.valueOf(value));
    }

    public void setProperties(String key, float value) {
        this._property.setProperty(key, String.valueOf(value));
    }

    public void setProperties(String key, double value) {
        this._property.setProperty(key, String.valueOf(value));
    }
}
