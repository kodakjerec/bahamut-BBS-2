package com.kota.Bahamut.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.kota.Bahamut.service.NotificationSettings.getCloudSave
import com.kota.asFramework.ui.ASToast.showLongToast
import com.kota.telnet.PropertiesOperator
import java.io.File

class UserSettings(var myContext: Context) {
    fun upgrade() {
        val settingsFilePath = myContext.filesDir.path + "/default_login.properties"
        mySharedPref = myContext.getSharedPreferences(PERF_NAME, 0)
        myEditor = mySharedPref!!.edit()
        if (File(settingsFilePath).exists()) {
            if (mySharedPref!!.getInt("upgrade", 0) != 1) {
                val prep = PropertiesOperator(settingsFilePath)
                if (prep.load()) {
                    // 使用新的泛型方法讀取設定值
                    val username = prep.getProperty(PROPERTIES_USERNAME, "")
                    val password = prep.getProperty(PROPERTIES_PASSWORD, "")
                    val saveLogonUser = prep.getProperty(PROPERTIES_SAVE_LOGON_USER, false)
                    val articleViewMode = prep.getProperty(PROPERTIES_ARTICLE_VIEW_MODE, 0)
                    val blockList = prep.getProperty(PROPERTIES_BLOCK_LIST, "")
                    val blockListEnable = prep.getProperty(PROPERTIES_BLOCK_LIST_ENABLE, false)
                    val blockListForTitle = prep.getProperty(PROPERTIES_BLOCK_LIST_FOR_TITLE, false)
                    val keepWifi = prep.getProperty(PROPERTIES_KEEP_WIFI_ENABLE, false)
                    val animationDisable = prep.getProperty(PROPERTIES_ANIMATION_DISABLE, false)
                    val extraToolbar = prep.getProperty(PROPERTIES_EXTRA_TOOLBAR_ENABLE, false)
                    val gestureOnBoard = prep.getProperty(PROPERTIES_GESTURE_ON_BOARD, false)
                    val autoToChat = prep.getProperty(PROPERTIES_AUTO_TO_CHAT, false)
                    val isVip = prep.getProperty(PROPERTIES_VIP, false)
                    val linkAutoShow = prep.getProperty(PROPERTIES_LINK_AUTO_SHOW, false)
                    val linkShowThumbnail = prep.getProperty(PROPERTIES_LINK_SHOW_THUMBNAIL, false)
                    val linkShowOnlyWifi = prep.getProperty(PROPERTIES_LINK_SHOW_ONLY_WIFI, false)
                    val toolbarLocation = prep.getProperty(PROPERTIES_TOOLBAR_LOCATION, 0)
                    val toolbarOrder = prep.getProperty(PROPERTIES_TOOLBAR_ORDER, 0)
                    val drawerLocation = prep.getProperty(PROPERTIES_DRAWER_LOCATION, 0)
                    val toolbarIdle = prep.getProperty(PROPERTIES_TOOLBAR_IDLE, 0.0f)
                    val toolbarAlpha = prep.getProperty(PROPERTIES_TOOLBAR_ALPHA, 0.0f)
                    val articleHeaders = prep.getProperty(PROPERTIES_ARTICLE_HEADS, "")
                    val shortUrlNonId = prep.getProperty(PROPERTIES_SHORT_URL_NON_ID, false)
                    val floatingLocationX = prep.getProperty(FLOATING_LOCATION_X, 0.0f)
                    val floatingLocationY = prep.getProperty(FLOATING_LOCATION_Y, 0.0f)
                    val varNoVipShortenTimes = prep.getProperty(NON_VIP_SHORTEN_TIMES_LIMIT, 0)
                    val webSignIn = prep.getProperty(PROPERTIES_WEB_SIGN_IN, 0)

                    // 將讀取到的設定值遷移到 SharedPreferences
                    myEditor!!.putString(PROPERTIES_USERNAME, username)
                    myEditor!!.putString(PROPERTIES_PASSWORD, password)
                    myEditor!!.putBoolean(PROPERTIES_SAVE_LOGON_USER, saveLogonUser)
                    myEditor!!.putInt(PROPERTIES_ARTICLE_VIEW_MODE, articleViewMode)
                    myEditor!!.putString(PROPERTIES_BLOCK_LIST, blockList)
                    myEditor!!.putBoolean(PROPERTIES_BLOCK_LIST_ENABLE, blockListEnable)
                    myEditor!!.putBoolean(PROPERTIES_BLOCK_LIST_FOR_TITLE, blockListForTitle)
                    myEditor!!.putBoolean(PROPERTIES_KEEP_WIFI_ENABLE, keepWifi)
                    myEditor!!.putBoolean(PROPERTIES_ANIMATION_DISABLE, animationDisable)
                    myEditor!!.putBoolean(PROPERTIES_EXTRA_TOOLBAR_ENABLE, extraToolbar)
                    myEditor!!.putBoolean(PROPERTIES_GESTURE_ON_BOARD, gestureOnBoard)
                    myEditor!!.putBoolean(PROPERTIES_AUTO_TO_CHAT, autoToChat)
                    myEditor!!.putBoolean(PROPERTIES_VIP, isVip)
                    myEditor!!.putInt(PROPERTIES_TOOLBAR_LOCATION, toolbarLocation)
                    myEditor!!.putInt(PROPERTIES_TOOLBAR_ORDER, toolbarOrder)
                    myEditor!!.putInt(PROPERTIES_DRAWER_LOCATION, drawerLocation)
                    myEditor!!.putFloat(PROPERTIES_TOOLBAR_IDLE, toolbarIdle)
                    myEditor!!.putFloat(PROPERTIES_TOOLBAR_ALPHA, toolbarAlpha)
                    myEditor!!.putBoolean(PROPERTIES_LINK_AUTO_SHOW, linkAutoShow)
                    myEditor!!.putBoolean(PROPERTIES_LINK_SHOW_THUMBNAIL, linkShowThumbnail)
                    myEditor!!.putBoolean(PROPERTIES_LINK_SHOW_ONLY_WIFI, linkShowOnlyWifi)
                    myEditor!!.putString(PROPERTIES_ARTICLE_HEADS, articleHeaders)
                    myEditor!!.putBoolean(PROPERTIES_SHORT_URL_NON_ID, shortUrlNonId)
                    myEditor!!.putFloat(FLOATING_LOCATION_X, floatingLocationX)
                    myEditor!!.putFloat(FLOATING_LOCATION_Y, floatingLocationY)
                    myEditor!!.putInt(NON_VIP_SHORTEN_TIMES_LIMIT, varNoVipShortenTimes)
                    myEditor!!.putInt(PROPERTIES_WEB_SIGN_IN, webSignIn)
                    myEditor!!.putInt("upgrade", 1)
                    myEditor!!.commit()
                }
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
        const val FLOATING_LOCATION_X: String = "floatingLocationX" // 浮動工具列位置 X
        const val FLOATING_LOCATION_Y: String = "floatingLocationY" // 浮動工具列位置 Y
        const val NON_VIP_SHORTEN_TIMES_LIMIT: String = "noVipShortenTimes" // 非VIP轉檔限制

        // 其他設定
        var blockListDefault: String = "guest" // 黑名單 list, 必定小寫, 字串, ex: aaa,bbb,ccc
        var mySharedPref: SharedPreferences? = null
        var myEditor: SharedPreferences.Editor? = null
        const val HEADERS_DEFAULT: String =
            "不加 ▼,[問題],[情報],[心得],[討論],[攻略],[秘技],[閒聊],[程設],[職場],[推廣],[手機],[平板],[新番],[電影],[新聞],[其它]"
        const val EXPRESSIONS_DEFAULT: String =
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
            get() = mySharedPref!!.getInt(
                PROPERTIES_DRAWER_LOCATION,
                0
            )
            set(choice) {
                myEditor!!.putInt(
                    PROPERTIES_DRAWER_LOCATION,
                    choice
                )?.commit()
            }
        @JvmStatic
        var propertiesToolbarLocation: Int
            get() = mySharedPref!!.getInt(
                PROPERTIES_TOOLBAR_LOCATION,
                0
            )
            set(choice) {
                myEditor!!.putInt(
                    PROPERTIES_TOOLBAR_LOCATION,
                    choice
                )?.commit()
            }

        @JvmStatic
        var propertiesToolbarOrder: Int
            get() = mySharedPref!!.getInt(
                PROPERTIES_TOOLBAR_ORDER,
                0
            )
            set(choice) {
                myEditor!!.putInt(
                    PROPERTIES_TOOLBAR_ORDER,
                    choice
                )?.commit()
            }
        @JvmStatic
        var propertiesScreenOrientation: Int
            get() = mySharedPref!!.getInt(
                PROPERTIES_SCREEN_ORIENTATION,
                0
            )
            set(choice) {
                myEditor!!.putInt(
                    PROPERTIES_SCREEN_ORIENTATION,
                    choice
                ).commit()
            }
        @JvmStatic
        var propertiesVIP: Boolean
            get() = mySharedPref!!.getBoolean(
                PROPERTIES_VIP,
                false
            )
            set(isEnable) {
                myEditor!!.putBoolean(
                    PROPERTIES_VIP,
                    isEnable
                ).commit()
            }

        @JvmStatic
        var propertiesAutoToChat: Boolean
            get() = mySharedPref!!.getBoolean(
                PROPERTIES_AUTO_TO_CHAT,
                false
            )
            set(isEnable) {
                myEditor!!.putBoolean(
                    PROPERTIES_AUTO_TO_CHAT,
                    isEnable
                ).commit()
            }

        @JvmStatic
        var propertiesGestureOnBoardEnable: Boolean
            get() = mySharedPref!!.getBoolean(
                PROPERTIES_GESTURE_ON_BOARD,
                true
            )
            set(isEnable) {
                myEditor!!.putBoolean(
                    PROPERTIES_GESTURE_ON_BOARD,
                    isEnable
                ).commit()
            }

        @JvmStatic
        var propertiesExternalToolbarEnable: Boolean
            get() = mySharedPref!!.getBoolean(
                PROPERTIES_EXTRA_TOOLBAR_ENABLE,
                false
            )
            set(isEnable) {
                myEditor!!.putBoolean(
                    PROPERTIES_EXTRA_TOOLBAR_ENABLE,
                    isEnable
                ).commit()
            }

        @JvmStatic
        var propertiesUsername: String
            get() = mySharedPref!!.getString(
                PROPERTIES_USERNAME,
                ""
            ).toString()
            set(username) {
                myEditor!!.putString(
                    PROPERTIES_USERNAME,
                    username
                ).commit()
            }

        @JvmStatic
        var propertiesPassword: String
            get() = mySharedPref!!.getString(
                PROPERTIES_PASSWORD,
                ""
            ).toString()
            set(password) {
                myEditor!!.putString(
                    PROPERTIES_PASSWORD,
                    password
                ).commit()
            }

        @JvmStatic
        var propertiesSaveLogonUser: Boolean
            get() = mySharedPref!!.getBoolean(
                PROPERTIES_SAVE_LOGON_USER,
                false
            )
            set(save) {
                myEditor!!.putBoolean(
                    PROPERTIES_SAVE_LOGON_USER,
                    save
                ).commit()
            }

        @JvmStatic
        var propertiesAnimationEnable: Boolean
            get() = mySharedPref!!.getBoolean(
                PROPERTIES_ANIMATION_DISABLE,
                true
            )
            set(enable) {
                myEditor!!.putBoolean(
                    PROPERTIES_ANIMATION_DISABLE,
                    enable
                ).commit()
            }

        /** 看板上一頁/下一頁  */
        @JvmStatic
        fun setPropertiesBoardMoveDisable(isDisable: Int) {
            myEditor!!.putInt(PROPERTIES_BOARD_MOVE_DISABLE, isDisable).commit()
        }

        @JvmStatic
        val propertiesBoardMoveEnable: Int
            get() = mySharedPref!!.getInt(
                PROPERTIES_BOARD_MOVE_DISABLE,
                0
            )

        /** 文章首篇/末篇  */
        @JvmStatic
        fun setPropertiesArticleMoveDisable(isDisable: Boolean) {
            myEditor!!.putBoolean(PROPERTIES_ARTICLE_MOVE_DISABLE, isDisable).commit()
        }

        @JvmStatic
        val propertiesArticleMoveEnable: Boolean
            get() = mySharedPref!!.getBoolean(
                PROPERTIES_ARTICLE_MOVE_DISABLE,
                true
            )

        fun setPropertiesArticleViewState(state: Int) {
            myEditor!!.putInt(PROPERTIES_ARTICLE_VIEW_MODE, state).commit()
        }

        @JvmStatic
        val propertiesArticleViewMode: Int
            get() = mySharedPref!!.getInt(
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
                val source: String = mySharedPref!!.getString(
                    PROPERTIES_ARTICLE_HEADS,
                    HEADERS_DEFAULT
                )!!
                return source.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            }

        @JvmStatic
        fun resetArticleHeaders() {
            myEditor!!.putString(PROPERTIES_ARTICLE_HEADS, HEADERS_DEFAULT).commit()
        }

        fun setArticleHeaders(stringList: MutableList<String>) {
            val saveString = java.lang.String.join(",", stringList)
            myEditor!!.putString(PROPERTIES_ARTICLE_HEADS, saveString).commit()
        }


        @JvmStatic
        val articleExpressions: Array<String>
            // 取出所有表情
            get() {
                val source: String = mySharedPref!!.getString(
                    PROPERTIES_ARTICLE_EXPRESSIONS,
                    EXPRESSIONS_DEFAULT
                )!!
                return source.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            }

        @JvmStatic
        fun resetArticleExpressions() {
            myEditor!!.putString(PROPERTIES_ARTICLE_EXPRESSIONS, EXPRESSIONS_DEFAULT).commit()
        }

        fun setArticleExpressions(stringList: MutableList<String>) {
            val saveString = java.lang.String.join(",", stringList)
            myEditor!!.putString(PROPERTIES_ARTICLE_EXPRESSIONS, saveString).commit()
        }

        @JvmStatic
        var blockList: MutableList<String>
            // 取出黑名單(格式化後)
            get() {
                val blockListString: String = mySharedPref!!.getString(
                    PROPERTIES_BLOCK_LIST,
                    blockListDefault
                )!!

                val blockList: MutableList<String> =
                    ArrayList()
                if (blockListString.isNotEmpty()) {
                    for (blockName in blockListString.split(",".toRegex())
                        .dropLastWhile { it.isEmpty() }.toTypedArray()) {
                        if (blockName.isNotEmpty()) {
                            blockList.add(blockName)
                        }
                    }
                }

                return blockList
            }
            // 更新黑名單時同時更新緩存
            set(aList) {
                var listString = StringBuilder()
                if (aList.isEmpty()) {
                    listString = StringBuilder("guest")
                    showLongToast("黑名單至少保留guest，為了政策")
                } else {
                    for (s in aList) {
                        listString.append(s.trim()).append(",")
                    }
                }

                myEditor!!.putString(
                    PROPERTIES_BLOCK_LIST,
                    listString.toString()
                )?.commit()


                // 更新緩存
                updateBlockListCache()
            }

        // 重置黑名單
        @JvmStatic
        fun resetBlockList() {
            myEditor!!.putString(PROPERTIES_BLOCK_LIST, blockListDefault).commit()
            blockList = blockList
        }

        // 更新緩存的輔助方法
        private fun updateBlockListCache() {
            val blockListString: String = mySharedPref!!.getString(
                PROPERTIES_BLOCK_LIST,
                blockListDefault
            )!!
            val blockStrings =
                blockListString.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            val acSets = mutableSetOf<String?>(*blockStrings)
            ac = AhoCorasick(acSets)

            blockListCache = HashSet()
            for (s in blockStrings) {
                if (!s.isEmpty()) {
                    blockListCache?.add(s)
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
            return matches.isNotEmpty()
        }

        @JvmStatic
        var propertiesBlockListEnable: Boolean
            get() = mySharedPref!!.getBoolean(
                PROPERTIES_BLOCK_LIST_ENABLE,
                false
            )
            set(enable) {
                myEditor!!.putBoolean(
                    PROPERTIES_BLOCK_LIST_ENABLE,
                    enable
                ).commit()
            }

        @JvmStatic
        var propertiesBlockListForTitle: Boolean
            get() = mySharedPref!!.getBoolean(
                PROPERTIES_BLOCK_LIST_FOR_TITLE,
                false
            )
            set(enable) {
                myEditor!!.putBoolean(
                    PROPERTIES_BLOCK_LIST_FOR_TITLE,
                    enable
                ).commit()
            }

        @JvmStatic
        var propertiesKeepWifi: Boolean
            get() = mySharedPref!!.getBoolean(
                PROPERTIES_KEEP_WIFI_ENABLE,
                true
            )
            set(enable) {
                myEditor!!.putBoolean(
                    PROPERTIES_KEEP_WIFI_ENABLE,
                    enable
                ).commit()
            }

        @JvmStatic
        val floatingLocation: MutableList<Float?>
            get() {
                val pref: SharedPreferences = mySharedPref!!
                val list: MutableList<Float?> =
                    ArrayList()
                try {
                    list.add(pref.getFloat(FLOATING_LOCATION_X, -1f))
                    list.add(pref.getFloat(FLOATING_LOCATION_Y, -1f))
                } catch (_: Exception) {
                    val tempX = pref.getInt(FLOATING_LOCATION_X, -1)
                    list.add(tempX.toFloat())
                    val tempY = pref.getInt(FLOATING_LOCATION_Y, -1)
                    list.add(tempY.toFloat())
                }
                return list
            }

        @JvmStatic
        fun setFloatingLocation(x: Float, y: Float) {
            myEditor!!.putFloat(FLOATING_LOCATION_X, x)
            myEditor!!.putFloat(FLOATING_LOCATION_Y, y).commit()
        }

        @JvmStatic
        var toolbarIdle: Float
            get() {
                try {
                    return mySharedPref!!.getFloat(
                        PROPERTIES_TOOLBAR_IDLE,
                        2.0f
                    )
                } catch (_: ClassCastException) {
                    val value: Int = mySharedPref!!.getInt(
                        PROPERTIES_TOOLBAR_IDLE,
                        2
                    )
                    return value.toFloat()
                }
            }
            set(idle) {
                myEditor!!.putFloat(
                    PROPERTIES_TOOLBAR_IDLE,
                    idle
                ).commit()
            }
        @JvmStatic
        var toolbarAlpha: Float
            get() {
                try {
                    return mySharedPref!!.getFloat(
                        PROPERTIES_TOOLBAR_ALPHA,
                        20.0f
                    )
                } catch (_: ClassCastException) {
                    val value: Int = mySharedPref!!.getInt(
                        PROPERTIES_TOOLBAR_ALPHA,
                        20
                    )
                    return value.toFloat()
                }
            }
            set(alpha) {
                myEditor!!.putFloat(
                    PROPERTIES_TOOLBAR_ALPHA,
                    alpha
                ).commit()
            }

        @JvmStatic
        fun setPropertiesLinkAutoShow(enable: Boolean) {
            myEditor!!.putBoolean(PROPERTIES_LINK_AUTO_SHOW, enable).commit()
        }

        @JvmStatic
        val linkAutoShow: Boolean
            get() = mySharedPref!!.getBoolean(
                PROPERTIES_LINK_AUTO_SHOW,
                true
            )
        @JvmStatic
        var linkShowThumbnail: Boolean
            get() = mySharedPref!!.getBoolean(
                PROPERTIES_LINK_SHOW_THUMBNAIL,
                false
            )
            set(enable) {
                myEditor!!.putBoolean(
                    PROPERTIES_LINK_SHOW_THUMBNAIL,
                    enable
                ).commit()
            }
        @JvmStatic
        var linkShowOnlyWifi: Boolean
            get() = mySharedPref!!.getBoolean(
                PROPERTIES_LINK_SHOW_ONLY_WIFI,
                false
            )
            set(enable) {
                myEditor!!.putBoolean(
                    PROPERTIES_LINK_SHOW_ONLY_WIFI,
                    enable
                ).commit()
            }

        @JvmStatic
        var propertiesNoVipShortenTimes: Int
            get() = mySharedPref!!.getInt(
                NON_VIP_SHORTEN_TIMES_LIMIT,
                0
            )
            set(times) {
                myEditor!!.putInt(
                    NON_VIP_SHORTEN_TIMES_LIMIT,
                    times
                ).commit()
            }

        fun setPropertiesShortUrlNonId(enable: Boolean) {
            myEditor!!.putBoolean(PROPERTIES_SHORT_URL_NON_ID, enable).commit()
        }

        val shortUrlNonId: Boolean
            get() = mySharedPref!!.getBoolean(
                PROPERTIES_SHORT_URL_NON_ID,
                true
            )

        @JvmStatic
        var propertiesWebSignIn: Boolean
            get() = mySharedPref!!.getBoolean(
                PROPERTIES_WEB_SIGN_IN,
                false
            )
            set(enable) {
                myEditor!!.putBoolean(
                    PROPERTIES_WEB_SIGN_IN,
                    enable
                ).commit()
            }
    }
}
