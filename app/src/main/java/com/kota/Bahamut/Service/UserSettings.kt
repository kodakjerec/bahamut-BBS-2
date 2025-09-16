package com.kota.Bahamut.Service

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.kota.ASFramework.UI.ASToast.showLongToast
import com.kota.Bahamut.Service.NotificationSettings.getCloudSave
import com.kota.Telnet.PropertiesOperator
import java.io.File
import java.util.Set

class UserSettings(var _context: Context) {
    fun upgrade() {
        val settings_file_path = _context.getFilesDir().getPath() + "/default_login.properties"
        _sharedPref = _context.getSharedPreferences(PERF_NAME, 0)
        _editor = _sharedPref!!.edit()
        if (File(settings_file_path).exists()) {
            if (_sharedPref!!.getInt("upgrade", 0) != 1) {
                val prep = PropertiesOperator(settings_file_path)
                prep.load()
                val username = prep.getPropertiesString(PROPERTIES_USERNAME)
                val password = prep.getPropertiesString(PROPERTIES_PASSWORD)
                val save_logon_user = prep.getPropertiesBoolean(PROPERTIES_SAVE_LOGON_USER)
                val article_view_mode = prep.getPropertiesInteger(PROPERTIES_ARTICLE_VIEW_MODE)
                val block_list = prep.getPropertiesString(PROPERTIES_BLOCK_LIST)
                val block_list_enable = prep.getPropertiesBoolean(PROPERTIES_BLOCK_LIST_ENABLE)
                val block_list_for_title =
                    prep.getPropertiesBoolean(PROPERTIES_BLOCK_LIST_FOR_TITLE)
                val keep_wifi = prep.getPropertiesBoolean(PROPERTIES_KEEP_WIFI_ENABLE)
                val animation_disable = prep.getPropertiesBoolean(PROPERTIES_ANIMATION_DISABLE)
                val extra_toolbar = prep.getPropertiesBoolean(PROPERTIES_EXTRA_TOOLBAR_ENABLE)
                val gesture_on_board = prep.getPropertiesBoolean(PROPERTIES_GESTURE_ON_BOARD)
                val auto_to_chat = prep.getPropertiesBoolean(PROPERTIES_AUTO_TO_CHAT)
                val is_vip = prep.getPropertiesBoolean(PROPERTIES_VIP)
                val linkAutoShow = prep.getPropertiesBoolean(PROPERTIES_LINK_AUTO_SHOW)
                val linkShowThumbnail = prep.getPropertiesBoolean(PROPERTIES_LINK_SHOW_THUMBNAIL)
                val linkShowOnlyWifi = prep.getPropertiesBoolean(PROPERTIES_LINK_SHOW_ONLY_WIFI)
                val toolbar_location = prep.getPropertiesInteger(PROPERTIES_TOOLBAR_LOCATION)
                val toolbar_order = prep.getPropertiesInteger(PROPERTIES_TOOLBAR_ORDER)
                val drawer_location = prep.getPropertiesInteger(PROPERTIES_DRAWER_LOCATION)
                val toolbar_idle = prep.getPropertiesFloat(PROPERTIES_TOOLBAR_IDLE)
                val toolbar_alpha = prep.getPropertiesFloat(PROPERTIES_TOOLBAR_ALPHA)
                val article_headers = prep.getPropertiesString(PROPERTIES_ARTICLE_HEADS)
                val shortUrlNonId = prep.getPropertiesBoolean(PROPERTIES_SHORT_URL_NON_ID)
                val floating_location_x = prep.getPropertiesFloat(floatingLocationX)
                val floating_location_y = prep.getPropertiesFloat(floatingLocationY)
                val varNoVipShortenTimes = prep.getPropertiesInteger(noVipShortenTimes)
                val webSignIn = prep.getPropertiesInteger(PROPERTIES_WEB_SIGN_IN)

                _editor!!.putString(PROPERTIES_USERNAME, username)
                _editor!!.putString(PROPERTIES_PASSWORD, password)
                _editor!!.putBoolean(PROPERTIES_SAVE_LOGON_USER, save_logon_user)
                _editor!!.putInt(PROPERTIES_ARTICLE_VIEW_MODE, article_view_mode)
                _editor!!.putString(PROPERTIES_BLOCK_LIST, block_list)
                _editor!!.putBoolean(PROPERTIES_BLOCK_LIST_ENABLE, block_list_enable)
                _editor!!.putBoolean(PROPERTIES_BLOCK_LIST_FOR_TITLE, block_list_for_title)
                _editor!!.putBoolean(PROPERTIES_KEEP_WIFI_ENABLE, keep_wifi)
                _editor!!.putBoolean(PROPERTIES_ANIMATION_DISABLE, animation_disable)
                _editor!!.putBoolean(PROPERTIES_EXTRA_TOOLBAR_ENABLE, extra_toolbar)
                _editor!!.putBoolean(PROPERTIES_GESTURE_ON_BOARD, gesture_on_board)
                _editor!!.putBoolean(PROPERTIES_AUTO_TO_CHAT, auto_to_chat)
                _editor!!.putBoolean(PROPERTIES_VIP, is_vip)
                _editor!!.putInt(PROPERTIES_TOOLBAR_LOCATION, toolbar_location)
                _editor!!.putInt(PROPERTIES_TOOLBAR_ORDER, toolbar_order)
                _editor!!.putInt(PROPERTIES_DRAWER_LOCATION, drawer_location)
                _editor!!.putFloat(PROPERTIES_TOOLBAR_IDLE, toolbar_idle)
                _editor!!.putFloat(PROPERTIES_TOOLBAR_ALPHA, toolbar_alpha)
                _editor!!.putBoolean(PROPERTIES_LINK_AUTO_SHOW, linkAutoShow)
                _editor!!.putBoolean(PROPERTIES_LINK_SHOW_THUMBNAIL, linkShowThumbnail)
                _editor!!.putBoolean(PROPERTIES_LINK_SHOW_ONLY_WIFI, linkShowOnlyWifi)
                _editor!!.putString(PROPERTIES_ARTICLE_HEADS, article_headers)
                _editor!!.putBoolean(PROPERTIES_SHORT_URL_NON_ID, shortUrlNonId)
                _editor!!.putFloat(floatingLocationX, floating_location_x)
                _editor!!.putFloat(floatingLocationY, floating_location_y)
                _editor!!.putInt(noVipShortenTimes, varNoVipShortenTimes)
                _editor!!.putInt(PROPERTIES_WEB_SIGN_IN, webSignIn)
                _editor!!.putInt("upgrade", 1)
                _editor!!.commit()
            }
        }
    }

    init {
        upgrade()
    }

    companion object {
        const val PERF_NAME: String = "user_setting"
        const val PROPERTIES_ANIMATION_DISABLE: String = "AnimationDisable" // 換頁動畫
        const val PROPERTIES_BOARD_MOVE_DISABLE: String = "BoardMoveDisable" // 看板上一頁/下一頁
        const val PROPERTIES_ARTICLE_MOVE_DISABLE: String = "ArticleModeDisable" // 文章首篇/末篇
        const val PROPERTIES_ARTICLE_VIEW_MODE: String = "ArticleViewMode" // 0-文字模式 1-telnet模式
        const val PROPERTIES_GESTURE_ON_BOARD: String = "GestureOnBoard" // 滑動手勢
        const val PROPERTIES_BLOCK_LIST: String = "BlockList" // 黑名單 list, 字串, ex: aaa,bbb,ccc
        const val PROPERTIES_BLOCK_LIST_ENABLE: String = "BlockListEnable" // 啟用黑名單
        const val PROPERTIES_BLOCK_LIST_FOR_TITLE: String = "BlockListForTitle" // 黑名單套用至標題
        const val PROPERTIES_EXTRA_TOOLBAR_ENABLE: String = "ExtraToolbarEnable" // 開啟工具列
        const val PROPERTIES_KEEP_WIFI_ENABLE: String = "KeepWifiEnable" // 防止Wifi因為待機而中斷
        const val PROPERTIES_PASSWORD: String = "Password"
        const val PROPERTIES_USERNAME: String = "Username"
        const val PROPERTIES_SAVE_LOGON_USER: String = "SaveLogonUser" // 記住我的資料
        const val PROPERTIES_AUTO_TO_CHAT: String = "AutoToChat" // 使用自動登入
        const val PROPERTIES_VIP: String = "VIP" // VIP 權限
        const val PROPERTIES_SCREEN_ORIENTATION: String = "ScreenOrientation" // 螢幕方向
        const val PROPERTIES_TOOLBAR_LOCATION: String = "ToolBarLocation" // 工具列位置
        const val PROPERTIES_TOOLBAR_ORDER: String = "ToolBarOrder" // 工具列順序
        const val PROPERTIES_DRAWER_LOCATION: String = "DrawerLocation" // 側滑選單
        const val PROPERTIES_TOOLBAR_IDLE: String = "ToolBarIdle" // 浮動工具列閒置多久隱藏
        const val PROPERTIES_TOOLBAR_ALPHA: String = "ToolBarAlpha" // 浮動工具列閒置多久隱藏不透明度
        const val PROPERTIES_LINK_AUTO_SHOW: String = "LinkAutoShow" // 連結自動預覽
        const val PROPERTIES_LINK_SHOW_THUMBNAIL: String = "LinkShowThumbnail" // 顯示預覽圖
        const val PROPERTIES_LINK_SHOW_ONLY_WIFI: String = "LinkShowOnlyWifi" // 只在Wifi下顯示預覽圖
        const val PROPERTIES_ARTICLE_HEADS: String = "ArticleHeaders" // 文章標題清單
        const val PROPERTIES_ARTICLE_EXPRESSIONS: String = "ArticleExpressions" // 表情符號清單
        const val PROPERTIES_SHORT_URL_NON_ID: String = "ShortUrlNonId" // 短網址,開啟去識別化
        const val PROPERTIES_WEB_SIGN_IN: String = "WebSignIn" // 開啟網頁登入

        // 執行階段比較不重要的設定
        const val floatingLocationX: String = "floatingLocationX" // 浮動工具列位置 X
        const val floatingLocationY: String = "floatingLocationY" // 浮動工具列位置 Y
        const val noVipShortenTimes: String = "noVipShortenTimes" // 非VIP轉檔限制

        // 其他設定
        var _blockListDefault: String = "guest" // 黑名單 list, 必定小寫, 字串, ex: aaa,bbb,ccc
        var _sharedPref: SharedPreferences? = null
        var _editor: SharedPreferences.Editor? = null
        const val _articleHeadersDefault: String =
            "不加 ▼,[問題],[情報],[心得],[討論],[攻略],[秘技],[閒聊],[程設],[職場],[推廣],[手機],[平板],[新番],[電影],[新聞],[其它]"
        const val _articleExpressions: String =
            "( >_0)b,( ;-w-)a,( -3-)y-~,ˋ(°▽ ° )ノˋ( ° ▽° )ノ,#/-_-)/~╨──╨,(||￣▽￣)a,o( -_-)=0))-3-)/,(#‵′)o,O(‵皿′)o,( T_T),(o_O ),_ψ(._. ),v(￣︶￣)y,ㄟ(￣▽￣ㄟ)...,(っ´▽`)っ,m(_ _)m,ˋ(°ω ° )ノ,◢▆▅▄▃崩╰(〒皿〒)╯潰▃▄▅▇◣,( O口O)!?, ☆━━━(ﾟ∀ﾟ)━━━, *[1;33m洽特*[m"

        // 通知更新
        @JvmStatic
        fun notifyDataUpdated() {
            // 雲端備份
            if (getCloudSave()) {
                val cloudBackup = CloudBackup()
                cloudBackup.backup()
            }
        }

        @JvmStatic
        var propertiesDrawerLocation: Int
            get() = _sharedPref!!.getInt(
                PROPERTIES_DRAWER_LOCATION,
                0
            )
            set(choice) {
                _editor!!.putInt(
                    PROPERTIES_DRAWER_LOCATION,
                    choice
                ).apply()
            }
        @JvmStatic
        var propertiesToolbarLocation: Int
            get() = _sharedPref!!.getInt(
                PROPERTIES_TOOLBAR_LOCATION,
                0
            )
            set(choice) {
                _editor!!.putInt(
                    PROPERTIES_TOOLBAR_LOCATION,
                    choice
                ).apply()
            }

        @JvmStatic
        var propertiesToolbarOrder: Int
            get() = _sharedPref!!.getInt(
                PROPERTIES_TOOLBAR_ORDER,
                0
            )
            set(choice) {
                _editor!!.putInt(
                    PROPERTIES_TOOLBAR_ORDER,
                    choice
                ).apply()
            }
        @JvmStatic
        var propertiesScreenOrientation: Int
            get() = _sharedPref!!.getInt(
                PROPERTIES_SCREEN_ORIENTATION,
                0
            )
            set(choice) {
                _editor!!.putInt(
                    PROPERTIES_SCREEN_ORIENTATION,
                    choice
                ).apply()
            }
        @JvmStatic
        var propertiesVIP: Boolean
            get() = _sharedPref!!.getBoolean(
                PROPERTIES_VIP,
                false
            )
            set(isEnable) {
                _editor!!.putBoolean(
                    PROPERTIES_VIP,
                    isEnable
                ).apply()
            }

        @JvmStatic
        var propertiesAutoToChat: Boolean
            get() = _sharedPref!!.getBoolean(
                PROPERTIES_AUTO_TO_CHAT,
                false
            )
            set(isEnable) {
                _editor!!.putBoolean(
                    PROPERTIES_AUTO_TO_CHAT,
                    isEnable
                ).apply()
            }

        @JvmStatic
        var propertiesGestureOnBoardEnable: Boolean
            get() = _sharedPref!!.getBoolean(
                PROPERTIES_GESTURE_ON_BOARD,
                true
            )
            set(isEnable) {
                _editor!!.putBoolean(
                    PROPERTIES_GESTURE_ON_BOARD,
                    isEnable
                ).apply()
            }

        @JvmStatic
        var propertiesExternalToolbarEnable: Boolean
            get() = _sharedPref!!.getBoolean(
                PROPERTIES_EXTRA_TOOLBAR_ENABLE,
                false
            )
            set(isEnable) {
                _editor!!.putBoolean(
                    PROPERTIES_EXTRA_TOOLBAR_ENABLE,
                    isEnable
                ).apply()
            }

        @JvmStatic
        var propertiesUsername: String?
            get() = _sharedPref!!.getString(
                PROPERTIES_USERNAME,
                ""
            )
            set(username) {
                _editor!!.putString(
                    PROPERTIES_USERNAME,
                    username
                ).apply()
            }

        @JvmStatic
        var propertiesPassword: String?
            get() = _sharedPref!!.getString(
                PROPERTIES_PASSWORD,
                ""
            )
            set(password) {
                _editor!!.putString(
                    PROPERTIES_PASSWORD,
                    password
                ).apply()
            }

        @JvmStatic
        var propertiesSaveLogonUser: Boolean
            get() = _sharedPref!!.getBoolean(
                PROPERTIES_SAVE_LOGON_USER,
                false
            )
            set(save) {
                _editor!!.putBoolean(
                    PROPERTIES_SAVE_LOGON_USER,
                    save
                ).apply()
            }

        @JvmStatic
        var propertiesAnimationEnable: Boolean
            get() = _sharedPref!!.getBoolean(
                PROPERTIES_ANIMATION_DISABLE,
                true
            )
            set(enable) {
                _editor!!.putBoolean(
                    PROPERTIES_ANIMATION_DISABLE,
                    enable
                ).apply()
            }

        /** 看板上一頁/下一頁  */
        @JvmStatic
        fun setPropertiesBoardMoveDisable(isDisable: Int) {
            _editor!!.putInt(PROPERTIES_BOARD_MOVE_DISABLE, isDisable).apply()
        }

        @JvmStatic
        val propertiesBoardMoveEnable: Int
            get() = _sharedPref!!.getInt(
                PROPERTIES_BOARD_MOVE_DISABLE,
                0
            )

        /** 文章首篇/末篇  */
        @JvmStatic
        fun setPropertiesArticleMoveDisable(isDisable: Boolean) {
            _editor!!.putBoolean(PROPERTIES_ARTICLE_MOVE_DISABLE, isDisable).apply()
        }

        @JvmStatic
        val propertiesArticleMoveEnable: Boolean
            get() = _sharedPref!!.getBoolean(
                PROPERTIES_ARTICLE_MOVE_DISABLE,
                true
            )

        fun setPropertiesArticleViewState(state: Int) {
            _editor!!.putInt(PROPERTIES_ARTICLE_VIEW_MODE, state).apply()
        }

        @JvmStatic
        val propertiesArticleViewMode: Int
            get() = _sharedPref!!.getInt(
                PROPERTIES_ARTICLE_VIEW_MODE,
                0
            )

        @JvmStatic
        fun exchangeArticleViewMode() {
            setPropertiesArticleViewState(1 - propertiesArticleViewMode)
        }

        @JvmStatic
        val articleHeaders: Array<String>
            // 取出所有符號
            get() {
                val _source: String = _sharedPref.getString(
                    UserSettings.Companion.PROPERTIES_ARTICLE_HEADS,
                    UserSettings.Companion._articleHeadersDefault
                )!!
                return _source.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            }

        @JvmStatic
        fun resetArticleHeaders() {
            _editor!!.putString(PROPERTIES_ARTICLE_HEADS, _articleHeadersDefault).apply()
        }

        fun setArticleHeaders(_stringList: MutableList<String?>) {
            val _saveString = java.lang.String.join(",", _stringList)
            _editor!!.putString(PROPERTIES_ARTICLE_HEADS, _saveString).apply()
        }


        @JvmStatic
        val articleExpressions: Array<String>
            // 取出所有表情
            get() {
                val _source: String = _sharedPref.getString(
                    UserSettings.Companion.PROPERTIES_ARTICLE_EXPRESSIONS,
                    UserSettings.Companion._articleExpressions
                )!!
                return _source.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            }

        @JvmStatic
        fun resetArticleExpressions() {
            _editor!!.putString(PROPERTIES_ARTICLE_EXPRESSIONS, _articleExpressions).apply()
        }

        fun setArticleExpressions(_stringList: MutableList<String?>) {
            val _saveString = java.lang.String.join(",", _stringList)
            _editor!!.putString(PROPERTIES_ARTICLE_EXPRESSIONS, _saveString).apply()
        }

        @JvmStatic
        var blockList: MutableList<String>?
            // 取出黑名單(格式化後)
            get() {
                val blockListString: String = _sharedPref.getString(
                    UserSettings.Companion.PROPERTIES_BLOCK_LIST,
                    UserSettings.Companion._blockListDefault
                )!!

                val _block_list: MutableList<String> =
                    ArrayList<String>()
                if (blockListString.length > 0) {
                    for (block_name in blockListString.split(",".toRegex())
                        .dropLastWhile { it.isEmpty() }.toTypedArray()) {
                        if (block_name.length > 0) {
                            _block_list.add(block_name)
                        }
                    }
                }

                return _block_list
            }
            // 更新黑名單時同時更新緩存
            set(aList) {
                var list_string = StringBuilder()
                if (aList == null || aList.size == 0) {
                    list_string = StringBuilder("guest")
                    showLongToast("黑名單至少保留guest，為了政策")
                } else {
                    for (s in aList) {
                        list_string.append(s.trim { it <= ' ' }).append(",")
                    }
                }

                _editor!!.putString(
                    PROPERTIES_BLOCK_LIST,
                    list_string.toString()
                ).apply()


                // 更新緩存
                updateBlockListCache()
            }

        // 重置黑名單
        @JvmStatic
        fun resetBlockList() {
            _editor!!.putString(PROPERTIES_BLOCK_LIST, _blockListDefault).apply()
            blockList = blockList
        }

        // 更新緩存的輔助方法
        private fun updateBlockListCache() {
            val blockListString: String = _sharedPref.getString(
                UserSettings.Companion.PROPERTIES_BLOCK_LIST,
                UserSettings.Companion._blockListDefault
            )!!
            val blockStrings =
                blockListString.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            val acSets = Set.of<String?>(*blockStrings)
            ac = AhoCorasick(acSets)

            blockListCache = HashSet<String?>()
            for (s in blockStrings) {
                if (!s.isEmpty()) {
                    blockListCache!!.add(s)
                }
            }
        }

        // 加入黑名單緩存
        private var blockListCache: HashSet<String?>? = null
        private var ac: AhoCorasick? = null

        // 檢查是否在黑名單中, 精確比對
        @JvmStatic
        @SuppressLint("DefaultLocale")
        fun isBlockListContains(aName: String?): Boolean {
            if (aName == null || aName.isEmpty()) return false

            // 初始化緩存
            if (blockListCache == null) {
                updateBlockListCache()
            }

            // 比對
            return blockListCache!!.contains(aName)
        }

        // 模糊比對
        @JvmStatic
        @SuppressLint("DefaultLocale")
        fun isBlockListContainsFuzzy(aName: String?): Boolean {
            if (aName == null || aName.isEmpty()) return false

            // 初始化緩存
            if (ac == null) {
                updateBlockListCache()
            }

            // 比對
            val matches: MutableList<String?> = ac!!.search(aName)
            return matches.size > 0
        }

        @JvmStatic
        var propertiesBlockListEnable: Boolean
            get() = _sharedPref!!.getBoolean(
                PROPERTIES_BLOCK_LIST_ENABLE,
                false
            )
            set(enable) {
                _editor!!.putBoolean(
                    PROPERTIES_BLOCK_LIST_ENABLE,
                    enable
                ).apply()
            }

        @JvmStatic
        var propertiesBlockListForTitle: Boolean
            get() = _sharedPref!!.getBoolean(
                PROPERTIES_BLOCK_LIST_FOR_TITLE,
                false
            )
            set(enable) {
                _editor!!.putBoolean(
                    PROPERTIES_BLOCK_LIST_FOR_TITLE,
                    enable
                ).apply()
            }

        @JvmStatic
        var propertiesKeepWifi: Boolean
            get() = _sharedPref!!.getBoolean(
                PROPERTIES_KEEP_WIFI_ENABLE,
                true
            )
            set(enable) {
                _editor!!.putBoolean(
                    PROPERTIES_KEEP_WIFI_ENABLE,
                    enable
                ).apply()
            }

        @JvmStatic
        val floatingLocation: MutableList<Float?>
            get() {
                val pref: SharedPreferences = _sharedPref!!
                val list: MutableList<Float?> =
                    ArrayList<Float?>()
                try {
                    list.add(pref.getFloat(floatingLocationX, -1f))
                    list.add(pref.getFloat(floatingLocationY, -1f))
                } catch (ignored: Exception) {
                    val tempX = pref.getInt(floatingLocationX, -1)
                    list.add(tempX.toFloat())
                    val tempY = pref.getInt(floatingLocationY, -1)
                    list.add(tempY.toFloat())
                }
                return list
            }

        @JvmStatic
        fun setFloatingLocation(x: Float, y: Float) {
            _editor!!.putFloat(floatingLocationX, x)
            _editor!!.putFloat(floatingLocationY, y).apply()
        }

        @JvmStatic
        var toolbarIdle: Float
            get() {
                try {
                    return _sharedPref!!.getFloat(
                        PROPERTIES_TOOLBAR_IDLE,
                        2.0f
                    )
                } catch (e: ClassCastException) {
                    val value: Int = _sharedPref!!.getInt(
                        PROPERTIES_TOOLBAR_IDLE,
                        2
                    )
                    return value.toFloat()
                }
            }
            set(idle) {
                _editor!!.putFloat(
                    PROPERTIES_TOOLBAR_IDLE,
                    idle
                ).apply()
            }
        @JvmStatic
        var toolbarAlpha: Float
            get() {
                try {
                    return _sharedPref!!.getFloat(
                        PROPERTIES_TOOLBAR_ALPHA,
                        20.0f
                    )
                } catch (e: ClassCastException) {
                    val value: Int = _sharedPref!!.getInt(
                        PROPERTIES_TOOLBAR_ALPHA,
                        20
                    )
                    return value.toFloat()
                }
            }
            set(alpha) {
                _editor!!.putFloat(
                    PROPERTIES_TOOLBAR_ALPHA,
                    alpha
                ).apply()
            }

        @JvmStatic
        fun setPropertiesLinkAutoShow(enable: Boolean) {
            _editor!!.putBoolean(PROPERTIES_LINK_AUTO_SHOW, enable).apply()
        }

        @JvmStatic
        val linkAutoShow: Boolean
            get() = _sharedPref!!.getBoolean(
                PROPERTIES_LINK_AUTO_SHOW,
                true
            )
        @JvmStatic
        var linkShowThumbnail: Boolean
            get() = _sharedPref!!.getBoolean(
                PROPERTIES_LINK_SHOW_THUMBNAIL,
                false
            )
            set(enable) {
                _editor!!.putBoolean(
                    PROPERTIES_LINK_SHOW_THUMBNAIL,
                    enable
                ).apply()
            }
        @JvmStatic
        var linkShowOnlyWifi: Boolean
            get() = _sharedPref!!.getBoolean(
                PROPERTIES_LINK_SHOW_ONLY_WIFI,
                false
            )
            set(enable) {
                _editor!!.putBoolean(
                    PROPERTIES_LINK_SHOW_ONLY_WIFI,
                    enable
                ).apply()
            }

        @JvmStatic
        var propertiesNoVipShortenTimes: Int
            get() = _sharedPref!!.getInt(
                noVipShortenTimes,
                0
            )
            set(times) {
                _editor!!.putInt(
                    noVipShortenTimes,
                    times
                ).apply()
            }

        fun setPropertiesShortUrlNonId(enable: Boolean) {
            _editor!!.putBoolean(PROPERTIES_SHORT_URL_NON_ID, enable).apply()
        }

        val shortUrlNonId: Boolean
            get() = _sharedPref!!.getBoolean(
                PROPERTIES_SHORT_URL_NON_ID,
                true
            )

        @JvmStatic
        var propertiesWebSignIn: Boolean
            get() = _sharedPref!!.getBoolean(
                PROPERTIES_WEB_SIGN_IN,
                false
            )
            set(enable) {
                _editor!!.putBoolean(
                    PROPERTIES_WEB_SIGN_IN,
                    enable
                ).apply()
            }
    }
}
