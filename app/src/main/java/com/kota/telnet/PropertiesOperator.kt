package com.kota.telnet

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Properties

class PropertiesOperator(aSavePath: String?) {
    val properties = Properties()
    private var savePath: String = ""

    init {
        if (aSavePath != null) {
            this.savePath = aSavePath
        }
    }

    fun store(): Boolean {
        return try {
            val file = File(this.savePath)
            if (file.exists()) {
                file.delete()
            }
            val fos: OutputStream = FileOutputStream(file)
            this.properties.storeToXML(fos, "UserSettings")
            fos.close()
            true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            false
        } catch (e2: IOException) {
            e2.printStackTrace()
            false
        }
    }

    fun load(): Boolean {
        return try {
            val file = File(this.savePath)
            if (!file.exists()) {
                return false
            }
            val fis: InputStream = FileInputStream(file)
            this.properties.loadFromXML(fis)
            fis.close()
            true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            false
        } catch (e2: IOException) {
            e2.printStackTrace()
            false
        }
    }

    // 泛型 getter 方法，統一處理不同類型
    inline fun <reified T> getProperty(key: String?, defaultValue: T): T {
        val value = this.properties.getProperty(key) ?: return defaultValue
        
        return try {
            when (T::class) {
                String::class -> value as T
                Int::class -> value.toInt() as T
                Boolean::class -> value.toBoolean() as T
                Float::class -> value.toFloat() as T
                Double::class -> value.toDouble() as T
                else -> defaultValue
            }
        } catch (e: Exception) {
            e.printStackTrace()
            defaultValue
        }
    }

    // 泛型 setter 方法，統一處理不同類型
    fun <T> setProperty(key: String?, value: T) {
        this.properties.setProperty(key, value.toString())
    }
}
