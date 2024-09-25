package com.kota.Bahamut.Service

import android.app.Activity
import android.content.SharedPreferences

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
        private const val showHeroStep = "showHeroStep" // 顯示勇者足跡


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
            UserSettings._editor.putString(connectIpAddress, ipLocation).apply()
        }

        @JvmStatic
        fun getConnectIpAddress(): String? {
            return UserSettings._sharedPref.getString(connectIpAddress, "bbs.gamer.com.tw")
        }

        @JvmStatic
        fun setShowHeroStep(show: Boolean) {
            UserSettings._editor.putBoolean(showHeroStep, show).apply()
        }

        @JvmStatic
        fun getShowHeroStep(): Boolean {
            return UserSettings._sharedPref.getBoolean(showHeroStep, true)
        }
}