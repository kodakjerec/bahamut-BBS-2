package com.kota.Bahamut.pages

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import com.google.android.material.slider.Slider
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.dismissProcessingDialog
import com.kota.asFramework.dialog.ASProcessingDialog.Companion.showProcessingDialog
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.asFramework.ui.ASToast.showLongToast
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.pages.blockListPage.ArticleExpressionListPage
import com.kota.Bahamut.pages.blockListPage.ArticleHeaderListPage
import com.kota.Bahamut.pages.blockListPage.BlockListPage
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CloudBackup
import com.kota.Bahamut.service.CommonFunctions.changeScreenOrientation
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.NotificationSettings.getCloudSave
import com.kota.Bahamut.service.NotificationSettings.setCloudSave
import com.kota.Bahamut.service.TempSettings
import com.kota.Bahamut.service.UserSettings.Companion.linkAutoShow
import com.kota.Bahamut.service.UserSettings.Companion.linkShowOnlyWifi
import com.kota.Bahamut.service.UserSettings.Companion.linkShowThumbnail
import com.kota.Bahamut.service.UserSettings.Companion.notifyDataUpdated
import com.kota.Bahamut.service.UserSettings.Companion.propertiesAnimationEnable
import com.kota.Bahamut.service.UserSettings.Companion.propertiesArticleMoveEnable
import com.kota.Bahamut.service.UserSettings.Companion.propertiesAutoToChat
import com.kota.Bahamut.service.UserSettings.Companion.propertiesBlockListEnable
import com.kota.Bahamut.service.UserSettings.Companion.propertiesBlockListForTitle
import com.kota.Bahamut.service.UserSettings.Companion.propertiesBoardMoveEnable
import com.kota.Bahamut.service.UserSettings.Companion.propertiesDrawerLocation
import com.kota.Bahamut.service.UserSettings.Companion.propertiesGestureOnBoardEnable
import com.kota.Bahamut.service.UserSettings.Companion.propertiesKeepWifi
import com.kota.Bahamut.service.UserSettings.Companion.propertiesScreenOrientation
import com.kota.Bahamut.service.UserSettings.Companion.propertiesToolbarLocation
import com.kota.Bahamut.service.UserSettings.Companion.propertiesToolbarOrder
import com.kota.Bahamut.service.UserSettings.Companion.propertiesVIP
import com.kota.Bahamut.service.UserSettings.Companion.setPropertiesArticleMoveDisable
import com.kota.Bahamut.service.UserSettings.Companion.setPropertiesBoardMoveDisable
import com.kota.Bahamut.service.UserSettings.Companion.setPropertiesLinkAutoShow
import com.kota.Bahamut.service.UserSettings.Companion.toolbarAlpha
import com.kota.Bahamut.service.UserSettings.Companion.toolbarIdle
import com.kota.telnet.TelnetClient
import com.kota.telnetUI.TelnetPage
import com.kota.telnetUI.textView.TelnetTextViewSmall
import java.util.Timer
import java.util.TimerTask
import androidx.core.net.toUri
import com.kota.Bahamut.service.TempSettings.myContext

class SystemSettingsPage : TelnetPage() {
    var mainLayout: LinearLayout? = null
    var autoToChatEnableListener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            propertiesAutoToChat = isChecked
        }
    var gestureOnBoardEnableListener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            propertiesGestureOnBoardEnable = isChecked
        }
    var animationEnableListener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            propertiesAnimationEnable = isChecked
            ASNavigationController.currentController?.isAnimationEnable = propertiesAnimationEnable
        }
    var articleMoveBoardEnableListener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            setPropertiesBoardMoveDisable(if (isChecked) 1 else 0)
        }

    /** 開啟或關閉文章首篇/末篇, checkbox  */
    var articleMoveEnableListener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            setPropertiesArticleMoveDisable(isChecked)
        }

    /** 開啟或關閉黑名單, checkbox  */
    var blockListEnableListener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            propertiesBlockListEnable = isChecked
        }

    /** 開啟或關閉黑名單套用至標題, checkbox  */
    var blockListForTitleListener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            propertiesBlockListForTitle = isChecked
        }

    /** 開啟或關閉雲端備份, checkbox  */
    var cloudSaveEnableListener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            setCloudSave(isChecked)
            if (getCloudSave()) {
                // 詢問雲端
                val cloudBackup = CloudBackup()
                cloudBackup.setListener {
                    showProcessingDialog("設定套用中\n請重新進入設定")
                    onBackPressed()

                    val timer = Timer()
                    val task: TimerTask = object : TimerTask() {
                        override fun run() {
                            dismissProcessingDialog()
                        }
                    }
                    timer.schedule(task, 3000)
                }
                cloudBackup.askCloudSave()
            }
        }

    /** 切換到黑名單設定  */
    var blockListSettingClickListener: View.OnClickListener =
        View.OnClickListener { v: View? -> navigationController.pushViewController(BlockListPage()) }

    /** 切換到發文標題設定  */
    var articleHeaderSettingListener: View.OnClickListener = View.OnClickListener { v: View? ->
        navigationController.pushViewController(ArticleHeaderListPage())
    }

    /** 切換到發文表情符號設定  */
    var articleExpressionSettingListener: View.OnClickListener =
        View.OnClickListener { v: View? ->
            navigationController.pushViewController(ArticleExpressionListPage())
        }

    /** 防止Wifi斷線  */
    var keepWifiListener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            propertiesKeepWifi = isChecked
            if (isChecked) navigationController.deviceController?.lockWifi()
            else navigationController.deviceController?.unlockWifi()
        }

    /** 不受電池最佳化限制  */
    @SuppressLint("BatteryLife")
    var ignoreBatteryListener: View.OnClickListener = View.OnClickListener { view: View? ->
        val powerManager = context?.getSystemService(Context.POWER_SERVICE) as PowerManager
        val packageName: String? = context?.packageName
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        if (powerManager.isIgnoringBatteryOptimizations(packageName)) {
//            intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            showLongToast(getContextString(R.string.ignoreBattery_msg02))
        } else {
            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = ("package:$packageName").toUri()
            startActivity(intent)
        }
    }

    /** 開啟贊助葉面  */
    var billingPageListener: View.OnClickListener = View.OnClickListener { v: View? ->
        val page = PageContainer.instance!!.billingPage
        navigationController.pushViewController(page)
    }

    /** 開啟外觀管理  */
    var themeManagerPageListener: View.OnClickListener = View.OnClickListener { v: View? ->
        val page = PageContainer.instance!!.getThemeManagerPage()
        navigationController.pushViewController(page)
    }

    /** 開啟BBS個人資料  */
    var bbsUserInfoListener: View.OnClickListener = View.OnClickListener { v: View? ->
        TelnetClient.myInstance!!.sendStringToServerInBackground("u\ni")
        val page = PageContainer.instance!!.getUserInfoPage()
        navigationController.pushViewController(page)
    }
    var bbsUserConfigListener: View.OnClickListener = View.OnClickListener { v: View? ->
        TelnetClient.myInstance!!.sendStringToServerInBackground("u\nc")
        val page = PageContainer.instance!!.getUserConfigPage()
        navigationController.pushViewController(page)
    }

    /** 畫面旋轉  */
    var screenOrientationListener: AdapterView.OnItemSelectedListener =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                i: Int,
                l: Long
            ) {
                propertiesScreenOrientation = i
                changeScreenOrientation()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                propertiesScreenOrientation = 0
            }
        }

    /** 側邊選單位置  */
    var drawerLocationListener: AdapterView.OnItemSelectedListener =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                i: Int,
                l: Long
            ) {
                propertiesDrawerLocation = i
                changeScreenOrientation()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                propertiesDrawerLocation = 0
            }
        }

    /** 工具列位置  */
    var toolbarLocationListener: AdapterView.OnItemSelectedListener =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                i: Int,
                l: Long
            ) {
                if (i <= 2) {
                    mainLayout?.findViewById<View>(R.id.SystemSettings_item_toolbar_order_item_1)!!.visibility =
                        View.VISIBLE
                    mainLayout?.findViewById<View>(R.id.SystemSettings_item_toolbar_order_item_2)!!.visibility =
                        View.GONE
                    mainLayout?.findViewById<View>(R.id.SystemSettings_item_toolbar_order_item_3)!!.visibility =
                        View.GONE
                } else {
                    mainLayout?.findViewById<View>(R.id.SystemSettings_item_toolbar_order_item_1)!!.visibility =
                        View.GONE
                    mainLayout?.findViewById<View>(R.id.SystemSettings_item_toolbar_order_item_2)!!.visibility =
                        View.VISIBLE
                    mainLayout?.findViewById<View>(R.id.SystemSettings_item_toolbar_order_item_3)!!.visibility =
                        View.VISIBLE
                }
                propertiesToolbarLocation = i
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                propertiesToolbarLocation = 0
            }
        }

    /** 工具列排序  */
    var toolbarOrderListener: AdapterView.OnItemSelectedListener =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                i: Int,
                l: Long
            ) {
                propertiesToolbarOrder = i
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                propertiesToolbarOrder = 0
            }
        }

    /** 連結自動預覽  */
    var linkAutoShowListener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            setPropertiesLinkAutoShow(isChecked)
            changeLinkAutoShowStatus(isChecked)
        }

    fun changeLinkAutoShowStatus(enable: Boolean) {
        if (enable) {
            mainLayout?.findViewById<View>(R.id.SystemSettings_item_enableLinkShowThumbnail)!!.visibility =
                View.VISIBLE
            (mainLayout?.findViewById<View>(R.id.SystemSettings_enableLinkShowThumbnail) as CheckBox).isChecked =
                linkShowThumbnail
            changeLinkOnlyWifiStatus(linkShowThumbnail)
        } else {
            linkShowThumbnail = false
            mainLayout?.findViewById<View>(R.id.SystemSettings_item_enableLinkShowThumbnail)!!.visibility =
                View.GONE
            changeLinkOnlyWifiStatus(false)
        }
    }

    /** 顯示預覽圖  */
    var linkShowThumbnailListener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            linkShowThumbnail = isChecked
            changeLinkOnlyWifiStatus(isChecked)
        }

    fun changeLinkOnlyWifiStatus(enable: Boolean) {
        if (enable) {
            mainLayout?.findViewById<View>(R.id.SystemSettings_item_enableLinkShowOnlyWifi)!!.visibility =
                View.VISIBLE
            (mainLayout?.findViewById<View>(R.id.SystemSettings_enableLinkShowOnlyWifi) as CheckBox).isChecked =
                linkShowOnlyWifi
        } else {
            linkShowOnlyWifi = false
            mainLayout?.findViewById<View>(R.id.SystemSettings_item_enableLinkShowOnlyWifi)!!.visibility =
                View.GONE
        }
    }

    /** 只在Wifi下預覽  */
    var linkShowOnlyWifiListener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            linkShowOnlyWifi = isChecked
        }

    override val pageLayout: Int
        get() = R.layout.system_settings_page

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_SYSTEM_SETTINGS

    override val isPopupPage: Boolean
        get() = true

    @SuppressLint("SetTextI18n")
    override fun onPageDidLoad() {
        mainLayout = findViewById(R.id.content_view) as LinearLayout?

        // 黑名單
        mainLayout?.findViewById<View>(R.id.SystemSettings_blockListSetting)!!
            .setOnClickListener(blockListSettingClickListener)
        val blockListEnableBox =
            mainLayout?.findViewById<CheckBox>(R.id.SystemSettings_blockListEnable)!!
        blockListEnableBox.isChecked = propertiesBlockListEnable
        blockListEnableBox.setOnCheckedChangeListener(blockListEnableListener)
        mainLayout?.findViewById<View>(R.id.SystemSettings_item_blockListEnable)!!
            .setOnClickListener { view: View? -> blockListEnableBox.isChecked = !blockListEnableBox.isChecked }
        val blockListForTitleBox =
            mainLayout?.findViewById<CheckBox>(R.id.SystemSettings_blockListForTitle)!!
        blockListForTitleBox.isChecked = propertiesBlockListForTitle
        blockListForTitleBox.setOnCheckedChangeListener(blockListForTitleListener)
        mainLayout?.findViewById<View>(R.id.SystemSettings_item_blockListForTitle)!!
            .setOnClickListener { view: View? -> blockListForTitleBox.isChecked = !blockListForTitleBox.isChecked }

        // keep-wifi
        val keepWifiBox = mainLayout?.findViewById<CheckBox>(R.id.SystemSettings_keepWifi)!!
        keepWifiBox.isChecked = propertiesKeepWifi
        keepWifiBox.setOnCheckedChangeListener(keepWifiListener)
        mainLayout?.findViewById<View>(R.id.SystemSettings_item_keepWifi)!!
            .setOnClickListener { view: View? -> keepWifiBox.isChecked = !keepWifiBox.isChecked }


        // 換頁動畫
        val animationEnableBox =
            mainLayout?.findViewById<CheckBox>(R.id.SystemSettings_animationEnable)!!
        animationEnableBox.isChecked = propertiesAnimationEnable
        animationEnableBox.setOnCheckedChangeListener(animationEnableListener)
        mainLayout?.findViewById<View>(R.id.SystemSettings_item_animationEnable)!!
            .setOnClickListener { view: View? -> animationEnableBox.isChecked = !animationEnableBox.isChecked }

        // 看板上一頁/下一頁
        val articleMoveEnableBoardBox =
            mainLayout?.findViewById<CheckBox>(R.id.SystemSettings_enableBoardMove)!!
        articleMoveEnableBoardBox.setChecked((propertiesBoardMoveEnable > 0))
        articleMoveEnableBoardBox.setOnCheckedChangeListener(articleMoveBoardEnableListener)
        mainLayout?.findViewById<View>(R.id.SystemSettings_item_enableBoardMove)!!
            .setOnClickListener { view: View? -> articleMoveEnableBoardBox.isChecked = !articleMoveEnableBoardBox.isChecked }


        // 文章首篇/末篇
        val articleMoveEnableBox =
            mainLayout?.findViewById<CheckBox>(R.id.SystemSettings_enableArticleMove)!!
        articleMoveEnableBox.isChecked = propertiesArticleMoveEnable
        articleMoveEnableBox.setOnCheckedChangeListener(articleMoveEnableListener)
        mainLayout?.findViewById<View>(R.id.SystemSettings_item_enableArticleMove)!!
            .setOnClickListener { view: View? -> articleMoveEnableBox.isChecked = !articleMoveEnableBox.isChecked }

        // 螢幕方向
        val adapterScreenOrientation: ArrayAdapter<Any> = ArrayAdapter<Any>(
            myContext!!,
            R.layout.simple_spinner_item,
            resource?.getStringArray(R.array.system_setting_page_screen_orientation_items) ?: arrayOf()
        )
        adapterScreenOrientation.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val spinnerScreenOrientation =
            mainLayout?.findViewById<Spinner>(R.id.SystemSettings_screen_orientation_spinner)!!
        spinnerScreenOrientation.adapter = adapterScreenOrientation
        spinnerScreenOrientation.setSelection(propertiesScreenOrientation)
        spinnerScreenOrientation.onItemSelectedListener = screenOrientationListener

        // 不受電池最佳化限制
        mainLayout?.findViewById<View>(R.id.SystemSettings_item_IgnoreBatteryOptimizations)!!
            .setOnClickListener(ignoreBatteryListener)

        // 連結自動預覽
        val linkAutoShowBox =
            mainLayout?.findViewById<CheckBox>(R.id.SystemSettings_enableLinkAutoShow)!!
        linkAutoShowBox.isChecked = linkAutoShow
        linkAutoShowBox.setOnCheckedChangeListener(linkAutoShowListener)
        mainLayout?.findViewById<View>(R.id.SystemSettings_item_enableLinkAutoShow)!!
            .setOnClickListener { view: View? -> linkAutoShowBox.isChecked = !linkAutoShowBox.isChecked }
        changeLinkAutoShowStatus(linkAutoShow)

        // 顯示預覽圖
        val linkShowThumbnail1 =
            mainLayout?.findViewById<CheckBox>(R.id.SystemSettings_enableLinkShowThumbnail)!!
        linkShowThumbnail1.isChecked = linkShowThumbnail
        linkShowThumbnail1.setOnCheckedChangeListener(linkShowThumbnailListener)
        mainLayout?.findViewById<View>(R.id.SystemSettings_item_enableLinkShowThumbnail)!!
            .setOnClickListener { view: View? -> linkShowThumbnail1.isChecked = !linkShowThumbnail1.isChecked }

        // 只在wifi下自動開啟
        val linkShowOnlyWifi1 =
            mainLayout?.findViewById<CheckBox>(R.id.SystemSettings_enableLinkShowOnlyWifi)!!
        linkShowOnlyWifi1.isChecked = linkShowOnlyWifi
        linkShowOnlyWifi1.setOnCheckedChangeListener(linkShowOnlyWifiListener)
        mainLayout?.findViewById<View>(R.id.SystemSettings_item_enableLinkShowOnlyWifi)!!
            .setOnClickListener { view: View? -> linkShowOnlyWifi1.isChecked = !linkShowOnlyWifi1.isChecked }

        // VIP
        if (propertiesVIP) {
            // 使用手勢在看板/文章
            mainLayout?.findViewById<View>(R.id.SystemSettings_item_enableGestureOnBoard)!!.visibility =
                View.VISIBLE
            val gestureOnBoardEnableBox =
                mainLayout?.findViewById<CheckBox>(R.id.SystemSettings_enableGestureOnBoard)!!
            gestureOnBoardEnableBox.isChecked = propertiesGestureOnBoardEnable
            gestureOnBoardEnableBox.setOnCheckedChangeListener(gestureOnBoardEnableListener)
            mainLayout?.findViewById<View>(R.id.SystemSettings_item_enableGestureOnBoard)!!
                .setOnClickListener { view: View? -> gestureOnBoardEnableBox.isChecked = !gestureOnBoardEnableBox.isChecked }

            // 自動登入洽特
            mainLayout?.findViewById<View>(R.id.SystemSettings_item_enableAutoToChat)!!.visibility =
                View.VISIBLE
            val autoToChatEnableBox =
                mainLayout?.findViewById<CheckBox>(R.id.SystemSettings_enableAutoToChat)!!
            autoToChatEnableBox.isChecked = propertiesAutoToChat
            autoToChatEnableBox.setOnCheckedChangeListener(autoToChatEnableListener)
            mainLayout?.findViewById<View>(R.id.SystemSettings_item_enableAutoToChat)!!
                .setOnClickListener { view: View? -> autoToChatEnableBox.isChecked = !autoToChatEnableBox.isChecked }

            // 工具列位置
            val adapterToolbarLocation: ArrayAdapter<Any> = ArrayAdapter<Any>(
                myContext!!,
                R.layout.simple_spinner_item,
                resource?.getStringArray(R.array.system_setting_page_toolbar_location_items) ?: arrayOf()
            )
            adapterToolbarLocation.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            val spinnerToolbarLocation =
                mainLayout?.findViewById<Spinner>(R.id.SystemSettings_toolbar_location_spinner)!!
            spinnerToolbarLocation.adapter = adapterToolbarLocation
            spinnerToolbarLocation.setSelection(propertiesToolbarLocation)
            spinnerToolbarLocation.onItemSelectedListener = toolbarLocationListener

            // 工具列順序
            val adapterToolbarOrder: ArrayAdapter<Any> = ArrayAdapter<Any>(
                myContext!!,
                R.layout.simple_spinner_item,
                resource?.getStringArray(R.array.system_setting_page_toolbar_order_items) ?: arrayOf()
            )
            adapterToolbarOrder.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            val spinnerToolbarOrder =
                mainLayout?.findViewById<Spinner>(R.id.SystemSettings_toolbar_order_spinner)!!
            spinnerToolbarOrder.adapter = adapterToolbarOrder
            spinnerToolbarOrder.setSelection(propertiesToolbarOrder)
            spinnerToolbarOrder.onItemSelectedListener = toolbarOrderListener
            if (propertiesToolbarLocation <= 2) { // 底部工具列
                mainLayout?.findViewById<View>(R.id.SystemSettings_item_toolbar_order_item_1)!!.visibility =
                    View.VISIBLE
                mainLayout?.findViewById<View>(R.id.SystemSettings_item_toolbar_order_item_2)!!.visibility =
                    View.GONE
                mainLayout?.findViewById<View>(R.id.SystemSettings_item_toolbar_order_item_3)!!.visibility =
                    View.GONE
            } else { // 浮動
                mainLayout?.findViewById<View>(R.id.SystemSettings_item_toolbar_order_item_1)!!.visibility =
                    View.GONE
                mainLayout?.findViewById<View>(R.id.SystemSettings_item_toolbar_order_item_2)!!.visibility =
                    View.VISIBLE
                mainLayout?.findViewById<View>(R.id.SystemSettings_item_toolbar_order_item_3)!!.visibility =
                    View.VISIBLE
            }
            val textSmallIdle =
                mainLayout?.findViewById<TelnetTextViewSmall>(R.id.system_setting_page_toolbar_idle_text)!!
            val sliderIdle =
                mainLayout?.findViewById<Slider>(R.id.system_setting_page_toolbar_idle)!!
            sliderIdle.value = toolbarIdle
            textSmallIdle.text = sliderIdle.value.toString() + "s"
            sliderIdle.addOnChangeListener { slider: Slider?, value: Float, fromUser: Boolean ->
                toolbarIdle = value
                textSmallIdle.text = value.toString() + "s"
            }
            val textSmallAlpha =
                mainLayout?.findViewById<TelnetTextViewSmall>(R.id.system_setting_page_toolbar_alpha_text)!!
            val sliderAlpha =
                mainLayout?.findViewById<Slider>(R.id.system_setting_page_toolbar_alpha)!!
            sliderAlpha.value = toolbarAlpha
            textSmallAlpha.text = sliderAlpha.value.toInt().toString() + "%"
            sliderAlpha.addOnChangeListener { slider: Slider?, value: Float, fromUser: Boolean ->
                toolbarAlpha = value
                textSmallAlpha.text = value.toInt().toString() + "%"
            }
            // 側滑選單位置
            val adapterDrawerLocation: ArrayAdapter<Any> = ArrayAdapter<Any>(
                myContext!!,
                R.layout.simple_spinner_item,
                resource?.getStringArray(R.array.system_setting_page_drawer_location_items) ?: arrayOf()
            )
            adapterDrawerLocation.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            val spinnerDrawerLocation =
                mainLayout?.findViewById<Spinner>(R.id.SystemSettings_drawer_location_spinner)!!
            spinnerDrawerLocation.adapter = adapterDrawerLocation
            spinnerDrawerLocation.setSelection(propertiesDrawerLocation)
            spinnerDrawerLocation.onItemSelectedListener = drawerLocationListener

            // 表情符號設定
            mainLayout?.findViewById<View>(R.id.SystemSettings_ArticleHeaderSetting)!!
                .setOnClickListener(articleHeaderSettingListener)
            mainLayout?.findViewById<View>(R.id.SystemSettings_ArticleExpressionSetting)!!
                .setOnClickListener(articleExpressionSettingListener)

            // 雲端設定
            val cloudSaveEnableBox =
                mainLayout?.findViewById<CheckBox>(R.id.SystemSettings_cloudSaveEnable)!!
            cloudSaveEnableBox.isChecked = getCloudSave()
            cloudSaveEnableBox.setOnCheckedChangeListener(cloudSaveEnableListener)
            mainLayout?.findViewById<View>(R.id.SystemSettings_item_cloudSaveEnable)!!
                .setOnClickListener { view: View? -> cloudSaveEnableBox.isChecked = !cloudSaveEnableBox.isChecked }
            val cloudSaveLastTime =
                mainLayout?.findViewById<TextView>(R.id.SystemSettings_cloudSaveLastTime)!!
            val lastTime = TempSettings.cloudSaveLastTime
            if (lastTime.isEmpty()) {
                cloudSaveLastTime.visibility = View.GONE
            } else {
                cloudSaveLastTime.text = lastTime
            }
        } else {
            mainLayout?.findViewById<View>(R.id.SystemSettings_item_enableGestureOnBoard)!!.visibility =
                View.GONE
            mainLayout?.findViewById<View>(R.id.SystemSettings_item_enableAutoToChat)!!.visibility =
                View.GONE
            mainLayout?.findViewById<View>(R.id.SystemSettings_item_toolbar_location)!!.visibility =
                View.GONE
            mainLayout?.findViewById<View>(R.id.SystemSettings_item_toolbar_order)!!.visibility =
                View.GONE
            mainLayout?.findViewById<View>(R.id.SystemSettings_item_drawer_location)!!.visibility =
                View.GONE
            mainLayout?.findViewById<View>(R.id.SystemSettings_ArticleHeaderSetting)!!.visibility =
                View.GONE
            mainLayout?.findViewById<View>(R.id.SystemSettings_ArticleExpressionSetting)!!.visibility =
                View.GONE
            mainLayout?.findViewById<View>(R.id.SystemSettings_item_cloud_save_layout)!!.visibility =
                View.GONE
        }

        // billing-page
        mainLayout?.findViewById<View>(R.id.SystemSettings_goBillingPage)!!
            .setOnClickListener(billingPageListener)

        // theme-manager-page
        mainLayout?.findViewById<View>(R.id.SystemSettings_goThemeManagerPage)!!
            .setOnClickListener(themeManagerPageListener)

        // bbs-user-info-page
        mainLayout?.findViewById<View>(R.id.SystemSettings_goBBSUserInfo)!!
            .setOnClickListener(bbsUserInfoListener)

        // bbs-user-page
        mainLayout?.findViewById<View>(R.id.SystemSettings_goBBSUserConfig)!!
            .setOnClickListener(bbsUserConfigListener)
    }

    val name: String
        get() = "TelnetSystemSettingsDialog"

    override fun onBackPressed(): Boolean {
        notifyDataUpdated()
        return super.onBackPressed()
    }

    override val isKeepOnOffline: Boolean
        get() = true

    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        showShortToast("返回")
        return true
    }
}
