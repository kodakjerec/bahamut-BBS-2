package com.kota.Telnet

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Properties

class PropertiesOperator(aSavePath: String?) {
    private val _property = Properties()
    private var _save_path: String? = ""

    init {
        this._save_path = aSavePath
    }

    fun store(): String? {
        if (this._save_path == null) {
            return "save path is null"
        }
        try {
            val file = File(this._save_path)
            if (file.exists()) {
                file.delete()
            }
            val fos: OutputStream = FileOutputStream(file)
            this._property.storeToXML(fos, "UserSettings")
            fos.close()
            return null
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return null
        } catch (e2: IOException) {
            e2.printStackTrace()
            return null
        }
    }

    fun load(): String? {
        if (this._save_path == null) {
            return "save path is null"
        }
        try {
            val file = File(this._save_path)
            if (!file.exists()) {
                return null
            }
            val fis: InputStream = FileInputStream(file)
            this._property.loadFromXML(fis)
            fis.close()
            return null
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return null
        } catch (e2: IOException) {
            e2.printStackTrace()
            return null
        }
    }

    fun getPropertiesString(key: String?): String {
        val str = this._property.getProperty(key)
        if (str == null) {
            return ""
        }
        return str
    }

    fun getPropertiesInteger(key: String?): Int {
        val value = this._property.getProperty(key)
        if (value == null) {
            return 0
        }
        try {
            return value.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }
    }

    fun getPropertiesBoolean(key: String?): Boolean {
        val value = this._property.getProperty(key)
        if (value == null) {
            return false
        }
        try {
            return value.toBoolean()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun getPropertiesFloat(key: String?): Float {
        val value = this._property.getProperty(key)
        if (value == null) {
            return 0.0f
        }
        try {
            return value.toFloat()
        } catch (e: Exception) {
            e.printStackTrace()
            return 0.0f
        }
    }

    fun getPropertiesDouble(key: String?): Double {
        val value = this._property.getProperty(key)
        if (value == null) {
            return 0.0
        }
        try {
            return value.toDouble()
        } catch (e: Exception) {
            e.printStackTrace()
            return 0.0
        }
    }

    fun setProperties(key: String?, value: String?) {
        this._property.setProperty(key, value)
    }

    fun setProperties(key: String?, value: Int) {
        this._property.setProperty(key, value.toString())
    }

    fun setProperties(key: String?, value: Boolean) {
        this._property.setProperty(key, value.toString())
    }

    fun setProperties(key: String?, value: Float) {
        this._property.setProperty(key, value.toString())
    }

    fun setProperties(key: String?, value: Double) {
        this._property.setProperty(key, value.toString())
    }
}
