package com.kota.Bahamut.service

import android.app.Activity
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * 此處存放app第一次使用的變數, 或者要存檔但不做雲端的參數
 * */
object NotificationSettings {
    private var perf: SharedPreferences? = null
    private const val PERF_NAME:String = "notification"
    private const val SHOW_TOP_BOTTOM_FUNCTION:String = "show_top_bottom_function" // 第一次進入文章頁面的提示訊息
    private const val SHOW_BLOCK_LIST:String = "show_block_list" // 第一次進入黑名單頁面的提示訊息
    private const val SHOW_HEADER:String = "show_header" // 第一次進入標題頁面的提示訊息
    private const val SHOW_EXPRESSION:String = "show_expression" // 第一次進入表情符號頁面的提示訊息
    private const val SHOW_CLOUD_SAVE: String = "show_cloud_save" // 第一次詢問雲端備份, false-未使用 true-已問過
    private const val CLOUD_SAVE: String = "cloud_save" // 雲端備份, false-不啟用 true-啟用
    private const val CONNECT_IP_ADDRESS = "connectIpAddress" // 連線IP
    private const val CONNECT_METHOD = "connectMethod" // 連線方式
    private const val SHOW_HERO_STEP = "showHeroStep" // 顯示勇者足跡
    private const val NOT_ALARM_IGNORE_BATTERY_OPTIMIZATIONS = "notAlarmIgnoreBatteryOptimizations" // 跳出掛網提醒視窗
    private const val SHOW_MESSAGE_FLOATING = "showMessageFloating" // 顯示訊息浮動按紐
    private const val DIALOG_REFERENCE_AUTHOR_0_REMOVE_BLANK = "dialogReferenceAuthor0RemoveBlank"
    private const val DIALOG_REFERENCE_AUTHOR_0_RESERVED_TYPE = "dialogReferenceAuthor0ReservedType"
    private const val DIALOG_REFERENCE_AUTHOR_1_REMOVE_BLANK = "dialogReferenceAuthor1RemoveBlank"
    private const val DIALOG_REFERENCE_AUTHOR_1_RESERVED_TYPE = "dialogReferenceAuthor1ReservedType"

    @JvmStatic
    fun upgrade(activity: Activity) {
        perf = activity.getSharedPreferences(PERF_NAME, 0)
    }

    @JvmStatic
    fun getShowTopBottomButton(): Boolean {
        return perf!!.getBoolean(SHOW_TOP_BOTTOM_FUNCTION, false)
    }

    @JvmStatic
    fun setShowTopBottomButton(isEnable: Boolean) {
        perf!!.edit { putBoolean(SHOW_TOP_BOTTOM_FUNCTION, isEnable) }
    }

    @JvmStatic
    fun getShowBlockList(): Boolean {
        return perf!!.getBoolean(SHOW_BLOCK_LIST, false)
    }

    @JvmStatic
    fun setShowBlockList(isEnable: Boolean) {
        perf!!.edit { putBoolean(SHOW_BLOCK_LIST, isEnable) }
    }

    @JvmStatic
    fun getShowHeader(): Boolean {
        return perf!!.getBoolean(SHOW_HEADER, false)
    }

    @JvmStatic
    fun setShowHeader(isEnable: Boolean) {
        perf!!.edit { putBoolean(SHOW_HEADER, isEnable) }
    }

    @JvmStatic
    fun getShowCloudSave(): Boolean {
        return perf!!.getBoolean(SHOW_CLOUD_SAVE, true)
    }

    @JvmStatic
    fun setShowCloudSave(status: Boolean) {
        perf!!.edit { putBoolean(SHOW_CLOUD_SAVE, status) }
    }

    @JvmStatic
    fun getShowExpression(): Boolean {
        return perf!!.getBoolean(SHOW_EXPRESSION, false)
    }

    @JvmStatic
    fun setShowExpression(isEnable: Boolean) {
        perf!!.edit { putBoolean(SHOW_EXPRESSION, isEnable) }
    }

    @JvmStatic
    fun setCloudSave(enable: Boolean) {
        perf!!.edit { putBoolean(CLOUD_SAVE, enable) }
    }

    @JvmStatic
    fun getCloudSave(): Boolean {
        return perf!!.getBoolean(CLOUD_SAVE, false)
    }

    @JvmStatic
    fun setConnectIpAddress(ipLocation: String) {
        perf!!.edit { putString(CONNECT_IP_ADDRESS, ipLocation) }
    }

    @JvmStatic
    fun getConnectIpAddress(): String? {
        return perf!!.getString(CONNECT_IP_ADDRESS, "bbs.gamer.com.tw")
    }

    @JvmStatic
    fun setConnectMethod(method: String) {
        perf!!.edit { putString(CONNECT_METHOD, method) }
    }

    @JvmStatic
    fun getConnectMethod(): String? {
        return perf!!.getString(CONNECT_METHOD, "telnet")
    }
    @JvmStatic
    fun setShowHeroStep(show: Boolean) {
        perf!!.edit { putBoolean(SHOW_HERO_STEP, show) }
    }

    @JvmStatic
    fun getShowHeroStep(): Boolean {
        return perf!!.getBoolean(SHOW_HERO_STEP, true)
    }

    @JvmStatic
    fun setShowMessageFloating(show: Boolean) {
        perf!!.edit { putBoolean(SHOW_MESSAGE_FLOATING, show) }
    }

    @JvmStatic
    fun getShowMessageFloating(): Boolean {
        return perf!!.getBoolean(SHOW_MESSAGE_FLOATING, true)
    }

    @JvmStatic
    fun setAlarmIgnoreBatteryOptimizations(show: Boolean) {
        perf!!.edit { putBoolean(NOT_ALARM_IGNORE_BATTERY_OPTIMIZATIONS, show) }
    }

    @JvmStatic
    fun getAlarmIgnoreBatteryOptimizations(): Boolean {
        return perf!!.getBoolean(NOT_ALARM_IGNORE_BATTERY_OPTIMIZATIONS, false)
    }

    @JvmStatic
    fun getDialogReferenceAuthor0RemoveBlank(): Boolean {
        return perf!!.getBoolean(DIALOG_REFERENCE_AUTHOR_0_REMOVE_BLANK, true)
    }

    @JvmStatic
    fun setDialogReferenceAuthor0RemoveBlank(isEnable: Boolean) {
        perf!!.edit { putBoolean(DIALOG_REFERENCE_AUTHOR_0_REMOVE_BLANK, isEnable) }
    }

    @JvmStatic
    fun getDialogReferenceAuthor0ReservedType(): Int {
        return perf!!.getInt(DIALOG_REFERENCE_AUTHOR_0_RESERVED_TYPE, 0)
    }

    @JvmStatic
    fun setDialogReferenceAuthor0ReservedType(index: Int) {
        perf!!.edit { putInt(DIALOG_REFERENCE_AUTHOR_0_RESERVED_TYPE, index) }
    }

    @JvmStatic
    fun getDialogReferenceAuthor1RemoveBlank(): Boolean {
        return perf!!.getBoolean(DIALOG_REFERENCE_AUTHOR_1_REMOVE_BLANK, true)
    }

    @JvmStatic
    fun setDialogReferenceAuthor1RemoveBlank(isEnable: Boolean) {
        perf!!.edit { putBoolean(DIALOG_REFERENCE_AUTHOR_1_REMOVE_BLANK, isEnable) }
    }

    @JvmStatic
    fun getDialogReferenceAuthor1ReservedType(): Int {
        return perf!!.getInt(DIALOG_REFERENCE_AUTHOR_1_RESERVED_TYPE, 0)
    }

    @JvmStatic
    fun setDialogReferenceAuthor1ReservedType(index: Int) {
        perf!!.edit { putInt(DIALOG_REFERENCE_AUTHOR_1_RESERVED_TYPE, index) }
    }
}