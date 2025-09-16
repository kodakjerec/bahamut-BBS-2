package com.kota.Bahamut.pages

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import com.kota.Bahamut.service.CloudBackupListener
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

class SystemSettingsPage : TelnetPage() {
    var mainLayout: LinearLayout? = null
    var _auto_to_chat_enable_listener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            propertiesAutoToChat = isChecked
        }
    var _gesture_on_board_enable_listener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            propertiesGestureOnBoardEnable = isChecked
        }
    var _animation_enable_listener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            propertiesAnimationEnable = isChecked
            ASNavigationController.getCurrentController()
                .setAnimationEnable(propertiesAnimationEnable)
        }
    var _article_move_enable_board_listener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            setPropertiesBoardMoveDisable(if (isChecked) 1 else 0)
        }

    /** 開啟或關閉文章首篇/末篇, checkbox  */
    var _article_move_enable_listener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            setPropertiesArticleMoveDisable(isChecked)
        }

    /** 開啟或關閉黑名單, checkbox  */
    var _block_list_enable_listener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            propertiesBlockListEnable = isChecked
        }

    /** 開啟或關閉黑名單套用至標題, checkbox  */
    var _block_list_for_title_listener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            propertiesBlockListForTitle = isChecked
        }

    /** 開啟或關閉雲端備份, checkbox  */
    var cloud_save_enable_listener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            setCloudSave(isChecked)
            if (getCloudSave()) {
                // 詢問雲端
                val cloudBackup = CloudBackup()
                cloudBackup.setListener(CloudBackupListener {
                    showProcessingDialog("設定套用中\n請重新進入設定")
                    onBackPressed()

                    val timer = Timer()
                    val task: TimerTask = object : TimerTask() {
                        override fun run() {
                            dismissProcessingDialog()
                        }
                    }
                    timer.schedule(task, 3000)
                })
                cloudBackup.askCloudSave()
            }
        }

    /** 切換到黑名單設定  */
    var _block_list_setting_listener: View.OnClickListener =
        View.OnClickListener { v: View? -> navigationController!!.pushViewController(BlockListPage()) }

    /** 切換到發文標題設定  */
    var _article_header_setting_listener: View.OnClickListener = View.OnClickListener { v: View? ->
        navigationController!!.pushViewController(ArticleHeaderListPage())
    }

    /** 切換到發文表情符號設定  */
    var _article_expression_setting_listener: View.OnClickListener =
        View.OnClickListener { v: View? ->
            navigationController!!.pushViewController(ArticleExpressionListPage())
        }

    /** 防止Wifi斷線  */
    var _keep_wifi_listener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            propertiesKeepWifi = isChecked
            if (isChecked) navigationController!!.deviceController!!.lockWifi()
            else navigationController!!.deviceController!!.unlockWifi()
        }

    /** 不受電池最佳化限制  */
    @SuppressLint("BatteryLife")
    var _ignore_battery_listener: View.OnClickListener = View.OnClickListener { view: View? ->
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val packageName: String? = context.getPackageName()
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        if (powerManager.isIgnoringBatteryOptimizations(packageName)) {
//            intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            showLongToast(getContextString(R.string.ignoreBattery_msg02))
        } else {
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.setData(Uri.parse("package:" + packageName))
            startActivity(intent)
        }
    }

    /** 開啟贊助葉面  */
    var billingPageListener: View.OnClickListener = View.OnClickListener { v: View? ->
        val page = PageContainer.getInstance().getBillingPage()
        navigationController!!.pushViewController(page)
    }

    /** 開啟外觀管理  */
    var themeManagerPageListener: View.OnClickListener = View.OnClickListener { v: View? ->
        val page = PageContainer.getInstance().getThemeManagerPage()
        navigationController!!.pushViewController(page)
    }

    /** 開啟BBS個人資料  */
    var bbsUserInfoListener: View.OnClickListener = View.OnClickListener { v: View? ->
        TelnetClient.getClient().sendStringToServerInBackground("u\ni")
        val page = PageContainer.getInstance().getUserInfoPage()
        navigationController!!.pushViewController(page)
    }
    var bbsUserConfigListener: View.OnClickListener = View.OnClickListener { v: View? ->
        TelnetClient.getClient().sendStringToServerInBackground("u\nc")
        val page = PageContainer.getInstance().getUserConfigPage()
        navigationController!!.pushViewController(page)
    }

    /** 畫面旋轉  */
    var _screen_orientation_listener: AdapterView.OnItemSelectedListener =
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
    var _drawer_location_listener: AdapterView.OnItemSelectedListener =
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
    var _toolbar_location_listener: AdapterView.OnItemSelectedListener =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                i: Int,
                l: Long
            ) {
                if (i <= 2) {
                    mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_toolbar_order_item_1)
                        .setVisibility(
                            View.VISIBLE
                        )
                    mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_toolbar_order_item_2)
                        .setVisibility(
                            View.GONE
                        )
                    mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_toolbar_order_item_3)
                        .setVisibility(
                            View.GONE
                        )
                } else {
                    mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_toolbar_order_item_1)
                        .setVisibility(
                            View.GONE
                        )
                    mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_toolbar_order_item_2)
                        .setVisibility(
                            View.VISIBLE
                        )
                    mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_toolbar_order_item_3)
                        .setVisibility(
                            View.VISIBLE
                        )
                }
                propertiesToolbarLocation = i
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                propertiesToolbarLocation = 0
            }
        }

    /** 工具列排序  */
    var _toolbar_order_listener: AdapterView.OnItemSelectedListener =
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
    var _link_auto_show_listener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            setPropertiesLinkAutoShow(isChecked)
            changeLinkAutoShowStatus(isChecked)
        }

    fun changeLinkAutoShowStatus(enable: Boolean) {
        if (enable) {
            mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_enableLinkShowThumbnail)
                .setVisibility(
                    View.VISIBLE
                )
            (mainLayout!!.findViewById<View?>(R.id.SystemSettings_enableLinkShowThumbnail) as CheckBox).setChecked(
                linkShowThumbnail
            )
            changeLinkOnlyWifiStatus(linkShowThumbnail)
        } else {
            linkShowThumbnail = false
            mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_enableLinkShowThumbnail)
                .setVisibility(
                    View.GONE
                )
            changeLinkOnlyWifiStatus(false)
        }
    }

    /** 顯示預覽圖  */
    var _link_show_thumbnail_listener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            linkShowThumbnail = isChecked
            changeLinkOnlyWifiStatus(isChecked)
        }

    fun changeLinkOnlyWifiStatus(enable: Boolean) {
        if (enable) {
            mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_enableLinkShowOnlyWifi)
                .setVisibility(
                    View.VISIBLE
                )
            (mainLayout!!.findViewById<View?>(R.id.SystemSettings_enableLinkShowOnlyWifi) as CheckBox).setChecked(
                linkShowOnlyWifi
            )
        } else {
            linkShowOnlyWifi = false
            mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_enableLinkShowOnlyWifi)
                .setVisibility(
                    View.GONE
                )
        }
    }

    /** 只在Wifi下預覽  */
    var _link_show_only_wifi_listener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            linkShowOnlyWifi = isChecked
        }

    val pageLayout: Int
        get() = R.layout.system_settings_page

    val pageType: Int
        get() = BahamutPage.BAHAMUT_SYSTEM_SETTINGS

    val isPopupPage: Boolean
        get() = true

    @SuppressLint("SetTextI18n")
    public override fun onPageDidLoad() {
        mainLayout = findViewById(R.id.content_view) as LinearLayout?

        // 黑名單
        mainLayout!!.findViewById<View?>(R.id.SystemSettings_blockListSetting)
            .setOnClickListener(_block_list_setting_listener)
        val block_list_enable_box =
            mainLayout!!.findViewById<CheckBox>(R.id.SystemSettings_blockListEnable)
        block_list_enable_box.setChecked(propertiesBlockListEnable)
        block_list_enable_box.setOnCheckedChangeListener(_block_list_enable_listener)
        mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_blockListEnable)
            .setOnClickListener(
                View.OnClickListener { view: View? -> block_list_enable_box.setChecked(!block_list_enable_box.isChecked()) })
        val block_list_for_title_box =
            mainLayout!!.findViewById<CheckBox>(R.id.SystemSettings_blockListForTitle)
        block_list_for_title_box.setChecked(propertiesBlockListForTitle)
        block_list_for_title_box.setOnCheckedChangeListener(_block_list_for_title_listener)
        mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_blockListForTitle)
            .setOnClickListener(
                View.OnClickListener { view: View? -> block_list_for_title_box.setChecked(!block_list_for_title_box.isChecked()) })

        // keep-wifi
        val keep_wifi_box = mainLayout!!.findViewById<CheckBox>(R.id.SystemSettings_keepWifi)
        keep_wifi_box.setChecked(propertiesKeepWifi)
        keep_wifi_box.setOnCheckedChangeListener(_keep_wifi_listener)
        mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_keepWifi)
            .setOnClickListener(View.OnClickListener { view: View? -> keep_wifi_box.setChecked(!keep_wifi_box.isChecked()) })


        // 換頁動畫
        val animation_enable_box =
            mainLayout!!.findViewById<CheckBox>(R.id.SystemSettings_animationEnable)
        animation_enable_box.setChecked(propertiesAnimationEnable)
        animation_enable_box.setOnCheckedChangeListener(_animation_enable_listener)
        mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_animationEnable)
            .setOnClickListener(
                View.OnClickListener { view: View? -> animation_enable_box.setChecked(!animation_enable_box.isChecked()) })

        // 看板上一頁/下一頁
        val article_move_enable_board_box =
            mainLayout!!.findViewById<CheckBox>(R.id.SystemSettings_enableBoardMove)
        article_move_enable_board_box.setChecked((propertiesBoardMoveEnable > 0))
        article_move_enable_board_box.setOnCheckedChangeListener(_article_move_enable_board_listener)
        mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_enableBoardMove)
            .setOnClickListener(
                View.OnClickListener { view: View? -> article_move_enable_board_box.setChecked(!article_move_enable_board_box.isChecked()) })


        // 文章首篇/末篇
        val article_move_enable_box =
            mainLayout!!.findViewById<CheckBox>(R.id.SystemSettings_enableArticleMove)
        article_move_enable_box.setChecked(propertiesArticleMoveEnable)
        article_move_enable_box.setOnCheckedChangeListener(_article_move_enable_listener)
        mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_enableArticleMove)
            .setOnClickListener(
                View.OnClickListener { view: View? -> article_move_enable_box.setChecked(!article_move_enable_box.isChecked()) })

        // 螢幕方向
        val adapter_screen_orientation: ArrayAdapter<String?> = ArrayAdapter<Any?>(
            context,
            R.layout.simple_spinner_item,
            resource.getStringArray(R.array.system_setting_page_screen_orientation_items)
        )
        adapter_screen_orientation.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val spinner_screen_orientation =
            mainLayout!!.findViewById<Spinner>(R.id.SystemSettings_screen_orientation_spinner)
        spinner_screen_orientation.setAdapter(adapter_screen_orientation)
        spinner_screen_orientation.setSelection(propertiesScreenOrientation)
        spinner_screen_orientation.setOnItemSelectedListener(_screen_orientation_listener)

        // 不受電池最佳化限制
        mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_IgnoreBatteryOptimizations)
            .setOnClickListener(_ignore_battery_listener)

        // 連結自動預覽
        val link_auto_show_box =
            mainLayout!!.findViewById<CheckBox>(R.id.SystemSettings_enableLinkAutoShow)
        link_auto_show_box.setChecked(linkAutoShow)
        link_auto_show_box.setOnCheckedChangeListener(_link_auto_show_listener)
        mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_enableLinkAutoShow)
            .setOnClickListener(
                View.OnClickListener { view: View? -> link_auto_show_box.setChecked(!link_auto_show_box.isChecked()) })
        changeLinkAutoShowStatus(linkAutoShow)

        // 顯示預覽圖
        val link_show_thumbnail =
            mainLayout!!.findViewById<CheckBox>(R.id.SystemSettings_enableLinkShowThumbnail)
        link_show_thumbnail.setChecked(linkShowThumbnail)
        link_show_thumbnail.setOnCheckedChangeListener(_link_show_thumbnail_listener)
        mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_enableLinkShowThumbnail)
            .setOnClickListener(
                View.OnClickListener { view: View? -> link_show_thumbnail.setChecked(!link_show_thumbnail.isChecked()) })

        // 只在wifi下自動開啟
        val link_show_only_wifi =
            mainLayout!!.findViewById<CheckBox>(R.id.SystemSettings_enableLinkShowOnlyWifi)
        link_show_only_wifi.setChecked(linkShowOnlyWifi)
        link_show_only_wifi.setOnCheckedChangeListener(_link_show_only_wifi_listener)
        mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_enableLinkShowOnlyWifi)
            .setOnClickListener(
                View.OnClickListener { view: View? -> link_show_only_wifi.setChecked(!link_show_only_wifi.isChecked()) })

        // VIP
        if (propertiesVIP) {
            // 使用手勢在看板/文章
            mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_enableGestureOnBoard)
                .setVisibility(
                    View.VISIBLE
                )
            val gesture_on_board_enable_box =
                mainLayout!!.findViewById<CheckBox>(R.id.SystemSettings_enableGestureOnBoard)
            gesture_on_board_enable_box.setChecked(propertiesGestureOnBoardEnable)
            gesture_on_board_enable_box.setOnCheckedChangeListener(_gesture_on_board_enable_listener)
            mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_enableGestureOnBoard)
                .setOnClickListener(
                    View.OnClickListener { view: View? -> gesture_on_board_enable_box.setChecked(!gesture_on_board_enable_box.isChecked()) })

            // 自動登入洽特
            mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_enableAutoToChat)
                .setVisibility(
                    View.VISIBLE
                )
            val auto_to_chat_enable_box =
                mainLayout!!.findViewById<CheckBox>(R.id.SystemSettings_enableAutoToChat)
            auto_to_chat_enable_box.setChecked(propertiesAutoToChat)
            auto_to_chat_enable_box.setOnCheckedChangeListener(_auto_to_chat_enable_listener)
            mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_enableAutoToChat)
                .setOnClickListener(
                    View.OnClickListener { view: View? -> auto_to_chat_enable_box.setChecked(!auto_to_chat_enable_box.isChecked()) })

            // 工具列位置
            val adapter_toolbar_location: ArrayAdapter<String?> = ArrayAdapter<Any?>(
                context,
                R.layout.simple_spinner_item,
                resource.getStringArray(R.array.system_setting_page_toolbar_location_items)
            )
            adapter_toolbar_location.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            val spinner_toolbar_location =
                mainLayout!!.findViewById<Spinner>(R.id.SystemSettings_toolbar_location_spinner)
            spinner_toolbar_location.setAdapter(adapter_toolbar_location)
            spinner_toolbar_location.setSelection(propertiesToolbarLocation)
            spinner_toolbar_location.setOnItemSelectedListener(_toolbar_location_listener)

            // 工具列順序
            val adapter_toolbar_order: ArrayAdapter<String?> = ArrayAdapter<Any?>(
                context,
                R.layout.simple_spinner_item,
                resource.getStringArray(R.array.system_setting_page_toolbar_order_items)
            )
            adapter_toolbar_order.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            val spinner_toolbar_order =
                mainLayout!!.findViewById<Spinner>(R.id.SystemSettings_toolbar_order_spinner)
            spinner_toolbar_order.setAdapter(adapter_toolbar_order)
            spinner_toolbar_order.setSelection(propertiesToolbarOrder)
            spinner_toolbar_order.setOnItemSelectedListener(_toolbar_order_listener)
            if (propertiesToolbarLocation <= 2) { // 底部工具列
                mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_toolbar_order_item_1)
                    .setVisibility(
                        View.VISIBLE
                    )
                mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_toolbar_order_item_2)
                    .setVisibility(
                        View.GONE
                    )
                mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_toolbar_order_item_3)
                    .setVisibility(
                        View.GONE
                    )
            } else { // 浮動
                mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_toolbar_order_item_1)
                    .setVisibility(
                        View.GONE
                    )
                mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_toolbar_order_item_2)
                    .setVisibility(
                        View.VISIBLE
                    )
                mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_toolbar_order_item_3)
                    .setVisibility(
                        View.VISIBLE
                    )
            }
            val textSmall_idle =
                mainLayout!!.findViewById<TelnetTextViewSmall>(R.id.system_setting_page_toolbar_idle_text)
            val slider_idle =
                mainLayout!!.findViewById<Slider>(R.id.system_setting_page_toolbar_idle)
            slider_idle.setValue(toolbarIdle)
            textSmall_idle.setText(slider_idle.getValue().toString() + "s")
            slider_idle.addOnChangeListener(Slider.OnChangeListener { slider: Slider?, value: Float, fromUser: Boolean ->
                toolbarIdle = value
                textSmall_idle.setText(value.toString() + "s")
            })
            val textSmall_alpha =
                mainLayout!!.findViewById<TelnetTextViewSmall>(R.id.system_setting_page_toolbar_alpha_text)
            val slider_alpha =
                mainLayout!!.findViewById<Slider>(R.id.system_setting_page_toolbar_alpha)
            slider_alpha.setValue(toolbarAlpha)
            textSmall_alpha.setText(slider_alpha.getValue().toInt().toString() + "%")
            slider_alpha.addOnChangeListener(Slider.OnChangeListener { slider: Slider?, value: Float, fromUser: Boolean ->
                toolbarAlpha = value
                textSmall_alpha.setText(value.toInt().toString() + "%")
            })
            // 側滑選單位置
            val adapter_drawer_location: ArrayAdapter<String?> = ArrayAdapter<Any?>(
                context,
                R.layout.simple_spinner_item,
                resource.getStringArray(R.array.system_setting_page_drawer_location_items)
            )
            adapter_drawer_location.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            val spinner_drawer_location =
                mainLayout!!.findViewById<Spinner>(R.id.SystemSettings_drawer_location_spinner)
            spinner_drawer_location.setAdapter(adapter_drawer_location)
            spinner_drawer_location.setSelection(propertiesDrawerLocation)
            spinner_drawer_location.setOnItemSelectedListener(_drawer_location_listener)

            // 表情符號設定
            mainLayout!!.findViewById<View?>(R.id.SystemSettings_ArticleHeaderSetting)
                .setOnClickListener(_article_header_setting_listener)
            mainLayout!!.findViewById<View?>(R.id.SystemSettings_ArticleExpressionSetting)
                .setOnClickListener(_article_expression_setting_listener)

            // 雲端設定
            val cloud_save_enable_box =
                mainLayout!!.findViewById<CheckBox>(R.id.SystemSettings_cloudSaveEnable)
            cloud_save_enable_box.setChecked(getCloudSave())
            cloud_save_enable_box.setOnCheckedChangeListener(cloud_save_enable_listener)
            mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_cloudSaveEnable)
                .setOnClickListener(
                    View.OnClickListener { view: View? -> cloud_save_enable_box.setChecked(!cloud_save_enable_box.isChecked()) })
            val cloud_save_last_time =
                mainLayout!!.findViewById<TextView>(R.id.SystemSettings_cloudSaveLastTime)
            val lastTime = TempSettings.cloudSaveLastTime
            if (lastTime.isEmpty()) {
                cloud_save_last_time.setVisibility(View.GONE)
            } else {
                cloud_save_last_time.setText(lastTime)
            }
        } else {
            mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_enableGestureOnBoard)
                .setVisibility(
                    View.GONE
                )
            mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_enableAutoToChat)
                .setVisibility(
                    View.GONE
                )
            mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_toolbar_location)
                .setVisibility(
                    View.GONE
                )
            mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_toolbar_order).setVisibility(
                View.GONE
            )
            mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_drawer_location)
                .setVisibility(
                    View.GONE
                )
            mainLayout!!.findViewById<View?>(R.id.SystemSettings_ArticleHeaderSetting)
                .setVisibility(
                    View.GONE
                )
            mainLayout!!.findViewById<View?>(R.id.SystemSettings_ArticleExpressionSetting)
                .setVisibility(
                    View.GONE
                )
            mainLayout!!.findViewById<View?>(R.id.SystemSettings_item_cloud_save_layout)
                .setVisibility(
                    View.GONE
                )
        }

        // billing-page
        mainLayout!!.findViewById<View?>(R.id.SystemSettings_goBillingPage)
            .setOnClickListener(billingPageListener)

        // theme-manager-page
        mainLayout!!.findViewById<View?>(R.id.SystemSettings_goThemeManagerPage)
            .setOnClickListener(themeManagerPageListener)

        // bbs-user-info-page
        mainLayout!!.findViewById<View?>(R.id.SystemSettings_goBBSUserInfo)
            .setOnClickListener(bbsUserInfoListener)

        // bbs-user-page
        mainLayout!!.findViewById<View?>(R.id.SystemSettings_goBBSUserConfig)
            .setOnClickListener(bbsUserConfigListener)
    }

    val name: String
        get() = "TelnetSystemSettingsDialog"

    public override fun onBackPressed(): Boolean {
        notifyDataUpdated()
        return super.onBackPressed()
    }

    val isKeepOnOffline: Boolean
        get() = true

    public override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        showShortToast("返回")
        return true
    }
}
