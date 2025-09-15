package com.kota.Telnet

import java.io.*
import java.util.Properties

class PropertiesOperator(private val savePath: String?) {
    private val property = Properties()

    fun store(): String? {
        if (savePath == null) {
            return "save path is null"
        }
        return try {
            val file = File(savePath)
            if (file.exists()) {
                file.delete()
            }
            FileOutputStream(file).use { fos ->
                property.storeToXML(fos, "UserSettings")
            }
            null
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun load(): String? {
        if (savePath == null) {
            return "save path is null"
        }
        return try {
            val file = File(savePath)
            if (!file.exists()) {
                return null
            }
            FileInputStream(file).use { fis ->
                property.loadFromXML(fis)
            }
            null
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun getPropertiesString(key: String): String {
        return property.getProperty(key) ?: ""
    }

    fun getPropertiesInteger(key: String): Int {
        val value = property.getProperty(key) ?: return 0
        return try {
            value.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    fun getPropertiesBoolean(key: String): Boolean {
        val value = property.getProperty(key) ?: return false
        return try {
            value.toBoolean()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getPropertiesFloat(key: String): Float {
        val value = property.getProperty(key) ?: return 0.0f
        return try {
            value.toFloat()
        } catch (e: Exception) {
            e.printStackTrace()
            0.0f
        }
    }

    fun getPropertiesDouble(key: String): Double {
        val value = property.getProperty(key) ?: return 0.0
        return try {
            value.toDouble()
        } catch (e: Exception) {
            e.printStackTrace()
            0.0
        }
    }

    fun setProperties(key: String, value: String) {
        property.setProperty(key, value)
    }

    fun setProperties(key: String, value: Int) {
        property.setProperty(key, value.toString())
    }

    fun setProperties(key: String, value: Boolean) {
        property.setProperty(key, value.toString())
    }

    fun setProperties(key: String, value: Float) {
        property.setProperty(key, value.toString())
    }

    fun setProperties(key: String, value: Double) {
        property.setProperty(key, value.toString())
    }
}
