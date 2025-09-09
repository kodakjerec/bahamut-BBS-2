package com.kota.Bahamut.Service

import android.app.Activity
import android.content.SharedPreferences

/**
 * 此處存放app第一次使用的變數, 或者要存檔但不做雲端的參數
 * */
object NotificationSettings {
    private var perf: SharedPreferences? = null
    private const val perfName:String = "notification"
    private const val propertiesShowTopBottomFunction:String = "show_top_bottom_function" // 第一次進入文章頁面的提示訊息
    private const val propertiesShowBlockList:String = "show_block_list" // 第一次進入黑名單頁面的提示訊息
    private const val propertiesShowHeader:String = "show_header" // 第一次進入標題頁面的提示訊息
    private const val propertiesShowExpression:String = "show_expression" // 第一次進入表情符號頁面的提示訊息
    private const val propertiesShowCloudSave: String = "show_cloud_save" // 第一次詢問雲端備份, false-未使用 true-已問過
    private const val propertiesCloudSave: String = "cloud_save" // 雲端備份, false-不啟用 true-啟用
    private const val connectIpAddress = "connectIpAddress" // 連線IP
    private const val connectMethod = "connectMethod" // 連線方式
    private const val showHeroStep = "showHeroStep" // 顯示勇者足跡
    private const val notAlarmIgnoreBatteryOptimizations = "notAlarmIgnoreBatteryOptimizations" // 跳出掛網提醒視窗
    private const val showMessageFloating = "showMessageFloating" // 顯示訊息浮動按紐
    private const val dialogReferenceAuthor0RemoveBlank = "dialogReferenceAuthor0RemoveBlank"
    private const val dialogReferenceAuthor0ReservedType = "dialogReferenceAuthor0ReservedType"
    private const val dialogReferenceAuthor1RemoveBlank = "dialogReferenceAuthor1RemoveBlank"
    private const val dialogReferenceAuthor1ReservedType = "dialogReferenceAuthor1ReservedType"

    @JvmStatic
    fun upgrade(activity: Activity) {
        perf = activity.getSharedPreferences(perfName, 0)
    }

    @JvmStatic
    fun getShowTopBottomButton(): Boolean {
        return perf!!.getBoolean(propertiesShowTopBottomFunction, false)
    }

    @JvmStatic
    fun setShowTopBottomButton(isEnable: Boolean) {
        perf!!.edit().putBoolean(propertiesShowTopBottomFunction, isEnable).apply()
    }

    @JvmStatic
    fun getShowBlockList(): Boolean {
        return perf!!.getBoolean(propertiesShowBlockList, false)
    }

    @JvmStatic
    fun setShowBlockList(isEnable: Boolean) {
        perf!!.edit().putBoolean(propertiesShowBlockList, isEnable).apply()
    }

    @JvmStatic
    fun getShowHeader(): Boolean {
        return perf!!.getBoolean(propertiesShowHeader, false)
    }

    @JvmStatic
    fun setShowHeader(isEnable: Boolean) {
        perf!!.edit().putBoolean(propertiesShowHeader, isEnable).apply()
    }

    @JvmStatic
    fun getShowCloudSave(): Boolean {
        return perf!!.getBoolean(propertiesShowCloudSave, true)
    }

    @JvmStatic
    fun setShowCloudSave(status: Boolean) {
        perf!!.edit().putBoolean(propertiesShowCloudSave, status).apply()
    }

    @JvmStatic
    fun getShowExpression(): Boolean {
        return perf!!.getBoolean(propertiesShowExpression, false)
    }

    @JvmStatic
    fun setShowExpression(isEnable: Boolean) {
        perf!!.edit().putBoolean(propertiesShowExpression, isEnable).apply()
    }

    @JvmStatic
    fun setCloudSave(enable: Boolean) {
        perf!!.edit().putBoolean(propertiesCloudSave , enable).apply()
    }

    @JvmStatic
    fun getCloudSave(): Boolean {
        return perf!!.getBoolean(propertiesCloudSave, false)
    }

    @JvmStatic
    fun setConnectIpAddress(ipLocation: String) {
        perf!!.edit().putString(connectIpAddress, ipLocation).apply()
    }

    @JvmStatic
    fun getConnectIpAddress(): String? {
        return perf!!.getString(connectIpAddress, "bbs.gamer.com.tw")
    }

    @JvmStatic
    fun setConnectMethod(method: String) {
        perf!!.edit().putString(connectMethod, method).apply()
    }

    @JvmStatic
    fun getConnectMethod(): String? {
        return perf!!.getString(connectMethod, "telnet")
    }
    @JvmStatic
    fun setShowHeroStep(show: Boolean) {
        perf!!.edit().putBoolean(showHeroStep, show).apply()
    }

    @JvmStatic
    fun getShowHeroStep(): Boolean {
        return perf!!.getBoolean(showHeroStep, true)
    }

    @JvmStatic
    fun setShowMessageFloating(show: Boolean) {
        perf!!.edit().putBoolean(showMessageFloating, show).apply()
    }

    @JvmStatic
    fun getShowMessageFloating(): Boolean {
        return perf!!.getBoolean(showMessageFloating, true)
    }

    @JvmStatic
    fun setAlarmIgnoreBatteryOptimizations(show: Boolean) {
        perf!!.edit().putBoolean(notAlarmIgnoreBatteryOptimizations, show).apply()
    }

    @JvmStatic
    fun getAlarmIgnoreBatteryOptimizations(): Boolean {
        return perf!!.getBoolean(notAlarmIgnoreBatteryOptimizations, false)
    }

    @JvmStatic
    fun getDialogReferenceAuthor0RemoveBlank(): Boolean {
        return perf!!.getBoolean(dialogReferenceAuthor0RemoveBlank, true)
    }

    @JvmStatic
    fun setDialogReferenceAuthor0RemoveBlank(isEnable: Boolean) {
        perf!!.edit().putBoolean(dialogReferenceAuthor0RemoveBlank, isEnable).apply()
    }

    @JvmStatic
    fun getDialogReferenceAuthor0ReservedType(): Int {
        return perf!!.getInt(dialogReferenceAuthor0ReservedType, 0)
    }

    @JvmStatic
    fun setDialogReferenceAuthor0ReservedType(index: Int) {
        perf!!.edit().putInt(dialogReferenceAuthor0ReservedType, index).apply()
    }

    @JvmStatic
    fun getDialogReferenceAuthor1RemoveBlank(): Boolean {
        return perf!!.getBoolean(dialogReferenceAuthor1RemoveBlank, true)
    }

    @JvmStatic
    fun setDialogReferenceAuthor1RemoveBlank(isEnable: Boolean) {
        perf!!.edit().putBoolean(dialogReferenceAuthor1RemoveBlank, isEnable).apply()
    }

    @JvmStatic
    fun getDialogReferenceAuthor1ReservedType(): Int {
        return perf!!.getInt(dialogReferenceAuthor1ReservedType, 0)
    }

    @JvmStatic
    fun setDialogReferenceAuthor1ReservedType(index: Int) {
        perf!!.edit().putInt(dialogReferenceAuthor1ReservedType, index).apply()
    }
}