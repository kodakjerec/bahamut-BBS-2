package com.kota.Bahamut.Service

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.kota.ASFramework.UI.ASToast
import com.kota.Telnet.PropertiesOperator
import java.io.File

class UserSettings(private val context: Context) {
    
    init {
        upgrade()
    }

    private fun upgrade() {
        val settingsFilePath = "${context.filesDir.path}/default_login.properties"
        sharedPref = context.getSharedPreferences(PERF_NAME, 0)
        editor = sharedPref.edit()
        
        if (File(settingsFilePath).exists()) {
            if (sharedPref.getInt("upgrade", 0) != 1) {
                val prep = PropertiesOperator(settingsFilePath)
                prep.load()
                
                val username = prep.getPropertiesString(PROPERTIES_USERNAME)
                val password = prep.getPropertiesString(PROPERTIES_PASSWORD)
                val saveLogonUser = prep.getPropertiesBoolean(PROPERTIES_SAVE_LOGON_USER)
                val articleViewMode = prep.getPropertiesInteger(PROPERTIES_ARTICLE_VIEW_MODE)
                val blockList = prep.getPropertiesString(PROPERTIES_BLOCK_LIST)
                val blockListEnable = prep.getPropertiesBoolean(PROPERTIES_BLOCK_LIST_ENABLE)
                val blockListForTitle = prep.getPropertiesBoolean(PROPERTIES_BLOCK_LIST_FOR_TITLE)
                val keepWifi = prep.getPropertiesBoolean(PROPERTIES_KEEP_WIFI_ENABLE)
                val animationDisable = prep.getPropertiesBoolean(PROPERTIES_ANIMATION_DISABLE)
                val extraToolbar = prep.getPropertiesBoolean(PROPERTIES_EXTRA_TOOLBAR_ENABLE)
                val gestureOnBoard = prep.getPropertiesBoolean(PROPERTIES_GESTURE_ON_BOARD)
                val autoToChat = prep.getPropertiesBoolean(PROPERTIES_AUTO_TO_CHAT)
                val isVip = prep.getPropertiesBoolean(PROPERTIES_VIP)
                val linkAutoShow = prep.getPropertiesBoolean(PROPERTIES_LINK_AUTO_SHOW)
                val linkShowThumbnail = prep.getPropertiesBoolean(PROPERTIES_LINK_SHOW_THUMBNAIL)
                val linkShowOnlyWifi = prep.getPropertiesBoolean(PROPERTIES_LINK_SHOW_ONLY_WIFI)
                val toolbarLocation = prep.getPropertiesInteger(PROPERTIES_TOOLBAR_LOCATION)
                val toolbarOrder = prep.getPropertiesInteger(PROPERTIES_TOOLBAR_ORDER)
                val drawerLocation = prep.getPropertiesInteger(PROPERTIES_DRAWER_LOCATION)
                val toolbarIdle = prep.getPropertiesFloat(PROPERTIES_TOOLBAR_IDLE)
                val toolbarAlpha = prep.getPropertiesFloat(PROPERTIES_TOOLBAR_ALPHA)
                val articleHeaders = prep.getPropertiesString(PROPERTIES_ARTICLE_HEADS)
                val shortUrlNonId = prep.getPropertiesBoolean(PROPERTIES_SHORT_URL_NON_ID)
                val floatingLocationX = prep.getPropertiesFloat(FLOATING_LOCATION_X)
                val floatingLocationY = prep.getPropertiesFloat(FLOATING_LOCATION_Y)
                val varNoVipShortenTimes = prep.getPropertiesInteger(NO_VIP_SHORTEN_TIMES)
                val webSignIn = prep.getPropertiesInteger(PROPERTIES_WEB_SIGN_IN)

                editor.putString(PROPERTIES_USERNAME, username)
                    .putString(PROPERTIES_PASSWORD, password)
                    .putBoolean(PROPERTIES_SAVE_LOGON_USER, saveLogonUser)
                    .putInt(PROPERTIES_ARTICLE_VIEW_MODE, articleViewMode)
                    .putString(PROPERTIES_BLOCK_LIST, blockList)
                    .putBoolean(PROPERTIES_BLOCK_LIST_ENABLE, blockListEnable)
                    .putBoolean(PROPERTIES_BLOCK_LIST_FOR_TITLE, blockListForTitle)
                    .putBoolean(PROPERTIES_KEEP_WIFI_ENABLE, keepWifi)
                    .putBoolean(PROPERTIES_ANIMATION_DISABLE, animationDisable)
                    .putBoolean(PROPERTIES_EXTRA_TOOLBAR_ENABLE, extraToolbar)
                    .putBoolean(PROPERTIES_GESTURE_ON_BOARD, gestureOnBoard)
                    .putBoolean(PROPERTIES_AUTO_TO_CHAT, autoToChat)
                    .putBoolean(PROPERTIES_VIP, isVip)
                    .putInt(PROPERTIES_TOOLBAR_LOCATION, toolbarLocation)
                    .putInt(PROPERTIES_TOOLBAR_ORDER, toolbarOrder)
                    .putInt(PROPERTIES_DRAWER_LOCATION, drawerLocation)
                    .putFloat(PROPERTIES_TOOLBAR_IDLE, toolbarIdle)
                    .putFloat(PROPERTIES_TOOLBAR_ALPHA, toolbarAlpha)
                    .putBoolean(PROPERTIES_LINK_AUTO_SHOW, linkAutoShow)
                    .putBoolean(PROPERTIES_LINK_SHOW_THUMBNAIL, linkShowThumbnail)
                    .putBoolean(PROPERTIES_LINK_SHOW_ONLY_WIFI, linkShowOnlyWifi)
                    .putString(PROPERTIES_ARTICLE_HEADS, articleHeaders)
                    .putBoolean(PROPERTIES_SHORT_URL_NON_ID, shortUrlNonId)
                    .putFloat(FLOATING_LOCATION_X, floatingLocationX)
                    .putFloat(FLOATING_LOCATION_Y, floatingLocationY)
                    .putInt(NO_VIP_SHORTEN_TIMES, varNoVipShortenTimes)
                    .putInt(PROPERTIES_WEB_SIGN_IN, webSignIn)
                    .putInt("upgrade", 1)
                    .commit()
            }
        }
    }

    companion object {
        private const val PERF_NAME = "user_setting"
        private const val PROPERTIES_ANIMATION_DISABLE = "AnimationDisable" // 換頁動畫
        private const val PROPERTIES_BOARD_MOVE_DISABLE = "BoardMoveDisable" // 看板上一頁/下一頁
        private const val PROPERTIES_ARTICLE_MOVE_DISABLE = "ArticleModeDisable" // 文章首篇/末篇
        private const val PROPERTIES_ARTICLE_VIEW_MODE = "ArticleViewMode" // 0-文字模式 1-telnet模式
        private const val PROPERTIES_GESTURE_ON_BOARD = "GestureOnBoard" // 滑動手勢
        private const val PROPERTIES_BLOCK_LIST = "BlockList" // 黑名單 list, 字串, ex: aaa,bbb,ccc
        private const val PROPERTIES_BLOCK_LIST_ENABLE = "BlockListEnable" // 啟用黑名單
        private const val PROPERTIES_BLOCK_LIST_FOR_TITLE = "BlockListForTitle" // 黑名單套用至標題
        private const val PROPERTIES_EXTRA_TOOLBAR_ENABLE = "ExtraToolbarEnable" // 開啟工具列
        private const val PROPERTIES_KEEP_WIFI_ENABLE = "KeepWifiEnable" // 防止Wifi因為待機而中斷
        private const val PROPERTIES_PASSWORD = "Password"
        private const val PROPERTIES_USERNAME = "Username"
        private const val PROPERTIES_SAVE_LOGON_USER = "SaveLogonUser" // 記住我的資料
        private const val PROPERTIES_AUTO_TO_CHAT = "AutoToChat" // 使用自動登入
        private const val PROPERTIES_VIP = "VIP" // VIP 權限
        private const val PROPERTIES_SCREEN_ORIENTATION = "ScreenOrientation" // 螢幕方向
        private const val PROPERTIES_TOOLBAR_LOCATION = "ToolBarLocation" // 工具列位置
        private const val PROPERTIES_TOOLBAR_ORDER = "ToolBarOrder" // 工具列順序
        private const val PROPERTIES_DRAWER_LOCATION = "DrawerLocation" // 側滑選單
        private const val PROPERTIES_TOOLBAR_IDLE = "ToolBarIdle" // 浮動工具列閒置多久隱藏
        private const val PROPERTIES_TOOLBAR_ALPHA = "ToolBarAlpha" // 浮動工具列閒置多久隱藏不透明度
        private const val PROPERTIES_LINK_AUTO_SHOW = "LinkAutoShow" // 連結自動預覽
        private const val PROPERTIES_LINK_SHOW_THUMBNAIL = "LinkShowThumbnail" // 顯示預覽圖
        private const val PROPERTIES_LINK_SHOW_ONLY_WIFI = "LinkShowOnlyWifi" // 只在Wifi下顯示預覽圖
        private const val PROPERTIES_ARTICLE_HEADS = "ArticleHeaders" // 文章標題清單
        private const val PROPERTIES_ARTICLE_EXPRESSIONS = "ArticleExpressions" // 表情符號清單
        private const val PROPERTIES_SHORT_URL_NON_ID = "ShortUrlNonId" // 短網址,開啟去識別化
        private const val PROPERTIES_WEB_SIGN_IN = "WebSignIn" // 開啟網頁登入

        // 執行階段比較不重要的設定
        private const val FLOATING_LOCATION_X = "floatingLocationX" // 浮動工具列位置 X
        private const val FLOATING_LOCATION_Y = "floatingLocationY" // 浮動工具列位置 Y
        private const val NO_VIP_SHORTEN_TIMES = "noVipShortenTimes" // 非VIP轉檔限制

        // 其他設定
        private const val BLOCK_LIST_DEFAULT = "guest" // 黑名單 list, 必定小寫, 字串, ex: aaa,bbb,ccc
        private const val ARTICLE_HEADERS_DEFAULT = "不加 ▼,[問題],[情報],[心得],[討論],[攻略],[秘技],[閒聊],[程設],[職場],[推廣],[手機],[平板],[新番],[電影],[新聞],[其它]"
        private const val ARTICLE_EXPRESSIONS = "( >_0)b,( ;-w-)a,( -3-)y-~,ˋ(°▽ ° )ノˋ( ° ▽° )ノ,#/-_-)/~╨──╨,(||￣▽￣)a,o( -_-)=0))-3-)/,(#‵′)o,O(‵皿′)o,( T_T),(o_O ),_ψ(._. ),v(￣︶￣)y,ㄟ(￣▽￣ㄟ)...,(っ´▽`)っ,m(_ _)m,ˋ(°ω ° )ノ,◢▆▅▄▃崩╰(〒皿〒)╯潰▃▄▅▇◣,( O口O)!?, ☆━━━(ﾟ∀ﾟ)━━━, *[1;33m洽特*[m"

        lateinit var sharedPref: SharedPreferences
        lateinit var editor: SharedPreferences.Editor

        // 加入黑名單緩存
        private var blockListCache: HashSet<String>? = null
        private var ac: AhoCorasick? = null

        // 通知更新
        fun notifyDataUpdated() {
            // 雲端備份
            if (NotificationSettings.getCloudSave()) {
                val cloudBackup = CloudBackup()
                cloudBackup.backup()
            }
        }

        fun setPropertiesDrawerLocation(choice: Int) {
            editor.putInt(PROPERTIES_DRAWER_LOCATION, choice).apply()
        }

        fun getPropertiesDrawerLocation(): Int {
            return sharedPref.getInt(PROPERTIES_DRAWER_LOCATION, 0)
        }

        fun setPropertiesToolbarLocation(choice: Int) {
            editor.putInt(PROPERTIES_TOOLBAR_LOCATION, choice).apply()
        }

        fun getPropertiesToolbarLocation(): Int {
            return sharedPref.getInt(PROPERTIES_TOOLBAR_LOCATION, 0)
        }

        fun setPropertiesToolbarOrder(choice: Int) {
            editor.putInt(PROPERTIES_TOOLBAR_ORDER, choice).apply()
        }

        fun getPropertiesToolbarOrder(): Int {
            return sharedPref.getInt(PROPERTIES_TOOLBAR_ORDER, 0)
        }

        fun setPropertiesScreenOrientation(choice: Int) {
            editor.putInt(PROPERTIES_SCREEN_ORIENTATION, choice).apply()
        }

        fun getPropertiesScreenOrientation(): Int {
            return sharedPref.getInt(PROPERTIES_SCREEN_ORIENTATION, 0)
        }

        fun setPropertiesVIP(isEnable: Boolean) {
            editor.putBoolean(PROPERTIES_VIP, isEnable).apply()
        }

        fun getPropertiesVIP(): Boolean {
            return sharedPref.getBoolean(PROPERTIES_VIP, false)
        }

        fun setPropertiesAutoToChat(isEnable: Boolean) {
            editor.putBoolean(PROPERTIES_AUTO_TO_CHAT, isEnable).apply()
        }

        fun getPropertiesAutoToChat(): Boolean {
            return sharedPref.getBoolean(PROPERTIES_AUTO_TO_CHAT, false)
        }

        fun setPropertiesGestureOnBoardEnable(isEnable: Boolean) {
            editor.putBoolean(PROPERTIES_GESTURE_ON_BOARD, isEnable).apply()
        }

        fun getPropertiesGestureOnBoardEnable(): Boolean {
            return sharedPref.getBoolean(PROPERTIES_GESTURE_ON_BOARD, true)
        }

        fun setPropertiesExternalToolbarEnable(isEnable: Boolean) {
            editor.putBoolean(PROPERTIES_EXTRA_TOOLBAR_ENABLE, isEnable).apply()
        }

        fun getPropertiesExternalToolbarEnable(): Boolean {
            return sharedPref.getBoolean(PROPERTIES_EXTRA_TOOLBAR_ENABLE, false)
        }

        fun setPropertiesUsername(username: String) {
            editor.putString(PROPERTIES_USERNAME, username).apply()
        }

        fun getPropertiesUsername(): String {
            return sharedPref.getString(PROPERTIES_USERNAME, "") ?: ""
        }

        fun setPropertiesPassword(password: String) {
            editor.putString(PROPERTIES_PASSWORD, password).apply()
        }

        fun getPropertiesPassword(): String {
            return sharedPref.getString(PROPERTIES_PASSWORD, "") ?: ""
        }

        fun setPropertiesSaveLogonUser(save: Boolean) {
            editor.putBoolean(PROPERTIES_SAVE_LOGON_USER, save).apply()
        }

        fun getPropertiesSaveLogonUser(): Boolean {
            return sharedPref.getBoolean(PROPERTIES_SAVE_LOGON_USER, false)
        }

        fun setPropertiesAnimationEnable(enable: Boolean) {
            editor.putBoolean(PROPERTIES_ANIMATION_DISABLE, enable).apply()
        }

        fun getPropertiesAnimationEnable(): Boolean {
            return sharedPref.getBoolean(PROPERTIES_ANIMATION_DISABLE, true)
        }

        /** 看板上一頁/下一頁 */
        fun setPropertiesBoardMoveDisable(isDisable: Int) {
            editor.putInt(PROPERTIES_BOARD_MOVE_DISABLE, isDisable).apply()
        }

        fun getPropertiesBoardMoveEnable(): Int {
            return sharedPref.getInt(PROPERTIES_BOARD_MOVE_DISABLE, 0)
        }

        /** 文章首篇/末篇 */
        fun setPropertiesArticleMoveDisable(isDisable: Boolean) {
            editor.putBoolean(PROPERTIES_ARTICLE_MOVE_DISABLE, isDisable).apply()
        }

        fun getPropertiesArticleMoveEnable(): Boolean {
            return sharedPref.getBoolean(PROPERTIES_ARTICLE_MOVE_DISABLE, true)
        }

        fun setPropertiesArticleViewState(state: Int) {
            editor.putInt(PROPERTIES_ARTICLE_VIEW_MODE, state).apply()
        }

        fun getPropertiesArticleViewMode(): Int {
            return sharedPref.getInt(PROPERTIES_ARTICLE_VIEW_MODE, 0)
        }

        fun exchangeArticleViewMode() {
            setPropertiesArticleViewState(1 - getPropertiesArticleViewMode())
        }

        // 取出所有符號
        fun getArticleHeaders(): Array<String> {
            val source = sharedPref.getString(PROPERTIES_ARTICLE_HEADS, ARTICLE_HEADERS_DEFAULT) ?: ARTICLE_HEADERS_DEFAULT
            return source.split(",").toTypedArray()
        }

        fun resetArticleHeaders() {
            editor.putString(PROPERTIES_ARTICLE_HEADS, ARTICLE_HEADERS_DEFAULT).apply()
        }

        fun setArticleHeaders(stringList: List<String>) {
            val saveString = stringList.joinToString(",")
            editor.putString(PROPERTIES_ARTICLE_HEADS, saveString).apply()
        }

        // 取出所有表情
        fun getArticleExpressions(): Array<String> {
            val source = sharedPref.getString(PROPERTIES_ARTICLE_EXPRESSIONS, ARTICLE_EXPRESSIONS) ?: ARTICLE_EXPRESSIONS
            return source.split(",").toTypedArray()
        }

        fun resetArticleExpressions() {
            editor.putString(PROPERTIES_ARTICLE_EXPRESSIONS, ARTICLE_EXPRESSIONS).apply()
        }

        fun setArticleExpressions(stringList: List<String>) {
            val saveString = stringList.joinToString(",")
            editor.putString(PROPERTIES_ARTICLE_EXPRESSIONS, saveString).apply()
        }

        // 取出黑名單(格式化後)
        fun getBlockList(): List<String> {
            val blockListString = sharedPref.getString(PROPERTIES_BLOCK_LIST, BLOCK_LIST_DEFAULT) ?: BLOCK_LIST_DEFAULT
            val blockList = mutableListOf<String>()
            
            if (blockListString.isNotEmpty()) {
                for (blockName in blockListString.split(",")) {
                    if (blockName.isNotEmpty()) {
                        blockList.add(blockName)
                    }
                }
            }
            
            return blockList
        }

        // 重置黑名單
        fun resetBlockList() {
            editor.putString(PROPERTIES_BLOCK_LIST, BLOCK_LIST_DEFAULT).apply()
            setBlockList(getBlockList())
        }

        // 更新黑名單時同時更新緩存
        fun setBlockList(aList: List<String>?) {
            val listString = if (aList.isNullOrEmpty()) {
                ASToast.showLongToast("黑名單至少保留guest，為了政策")
                "guest"
            } else {
                aList.joinToString(",") { it.trim() } + ","
            }
            
            editor.putString(PROPERTIES_BLOCK_LIST, listString).apply()
            
            // 更新緩存
            updateBlockListCache()
        }

        // 更新緩存的輔助方法
        private fun updateBlockListCache() {
            val blockListString = sharedPref.getString(PROPERTIES_BLOCK_LIST, BLOCK_LIST_DEFAULT) ?: BLOCK_LIST_DEFAULT
            val blockStrings = blockListString.split(",").toTypedArray()

            val acSets = blockStrings.toSet()
            ac = AhoCorasick(acSets)

            blockListCache = HashSet<String>().apply {
                for (s in blockStrings) {
                    if (s.isNotEmpty()) {
                        add(s)
                    }
                }
            }
        }

        // 檢查是否在黑名單中, 精確比對
        @SuppressLint("DefaultLocale")
        fun isBlockListContains(aName: String?): Boolean {
            if (aName.isNullOrEmpty()) return false

            // 初始化緩存
            if (blockListCache == null) {
                updateBlockListCache()
            }

            // 比對
            return blockListCache?.contains(aName) == true
        }

        // 模糊比對
        @SuppressLint("DefaultLocale")
        fun isBlockListContainsFuzzy(aName: String?): Boolean {
            if (aName.isNullOrEmpty()) return false

            // 初始化緩存
            if (ac == null) {
                updateBlockListCache()
            }

            // 比對
            val matches = ac?.search(aName) ?: emptyList()
            return matches.isNotEmpty()
        }

        fun setPropertiesBlockListEnable(enable: Boolean) {
            editor.putBoolean(PROPERTIES_BLOCK_LIST_ENABLE, enable).apply()
        }

        fun getPropertiesBlockListEnable(): Boolean {
            return sharedPref.getBoolean(PROPERTIES_BLOCK_LIST_ENABLE, false)
        }

        fun setPropertiesBlockListForTitle(enable: Boolean) {
            editor.putBoolean(PROPERTIES_BLOCK_LIST_FOR_TITLE, enable).apply()
        }

        fun getPropertiesBlockListForTitle(): Boolean {
            return sharedPref.getBoolean(PROPERTIES_BLOCK_LIST_FOR_TITLE, false)
        }

        fun setPropertiesKeepWifi(enable: Boolean) {
            editor.putBoolean(PROPERTIES_KEEP_WIFI_ENABLE, enable).apply()
        }

        fun getPropertiesKeepWifi(): Boolean {
            return sharedPref.getBoolean(PROPERTIES_KEEP_WIFI_ENABLE, true)
        }

        fun getFloatingLocation(): List<Float> {
            val pref = sharedPref
            val list = mutableListOf<Float>()
            try {
                list.add(pref.getFloat(FLOATING_LOCATION_X, -1f))
                list.add(pref.getFloat(FLOATING_LOCATION_Y, -1f))
            } catch (ignored: Exception) {
                val tempX = pref.getInt(FLOATING_LOCATION_X, -1)
                list.add(tempX.toFloat())
                val tempY = pref.getInt(FLOATING_LOCATION_Y, -1)
                list.add(tempY.toFloat())
            }
            return list
        }

        fun setFloatingLocation(x: Float, y: Float) {
            editor.putFloat(FLOATING_LOCATION_X, x)
                .putFloat(FLOATING_LOCATION_Y, y)
                .apply()
        }

        fun setToolbarIdle(idle: Float) {
            editor.putFloat(PROPERTIES_TOOLBAR_IDLE, idle).apply()
        }

        fun getToolbarIdle(): Float {
            return try {
                sharedPref.getFloat(PROPERTIES_TOOLBAR_IDLE, 2.0f)
            } catch (e: ClassCastException) {
                val value = sharedPref.getInt(PROPERTIES_TOOLBAR_IDLE, 2)
                value.toFloat()
            }
        }

        fun setToolbarAlpha(alpha: Float) {
            editor.putFloat(PROPERTIES_TOOLBAR_ALPHA, alpha).apply()
        }

        fun getToolbarAlpha(): Float {
            return try {
                sharedPref.getFloat(PROPERTIES_TOOLBAR_ALPHA, 20.0f)
            } catch (e: ClassCastException) {
                val value = sharedPref.getInt(PROPERTIES_TOOLBAR_ALPHA, 20)
                value.toFloat()
            }
        }

        fun setPropertiesLinkAutoShow(enable: Boolean) {
            editor.putBoolean(PROPERTIES_LINK_AUTO_SHOW, enable).apply()
        }

        fun getLinkAutoShow(): Boolean {
            return sharedPref.getBoolean(PROPERTIES_LINK_AUTO_SHOW, true)
        }

        fun setLinkShowThumbnail(enable: Boolean) {
            editor.putBoolean(PROPERTIES_LINK_SHOW_THUMBNAIL, enable).apply()
        }

        fun getLinkShowThumbnail(): Boolean {
            return sharedPref.getBoolean(PROPERTIES_LINK_SHOW_THUMBNAIL, false)
        }

        fun setLinkShowOnlyWifi(enable: Boolean) {
            editor.putBoolean(PROPERTIES_LINK_SHOW_ONLY_WIFI, enable).apply()
        }

        fun getLinkShowOnlyWifi(): Boolean {
            return sharedPref.getBoolean(PROPERTIES_LINK_SHOW_ONLY_WIFI, false)
        }

        fun setPropertiesNoVipShortenTimes(times: Int) {
            editor.putInt(NO_VIP_SHORTEN_TIMES, times).apply()
        }

        fun getPropertiesNoVipShortenTimes(): Int {
            return sharedPref.getInt(NO_VIP_SHORTEN_TIMES, 0)
        }

        fun setPropertiesShortUrlNonId(enable: Boolean) {
            editor.putBoolean(PROPERTIES_SHORT_URL_NON_ID, enable).apply()
        }

        fun getShortUrlNonId(): Boolean {
            return sharedPref.getBoolean(PROPERTIES_SHORT_URL_NON_ID, true)
        }

        fun setPropertiesWebSignIn(enable: Boolean) {
            editor.putBoolean(PROPERTIES_WEB_SIGN_IN, enable).apply()
        }

        fun getPropertiesWebSignIn(): Boolean {
            return sharedPref.getBoolean(PROPERTIES_WEB_SIGN_IN, false)
        }
    }
}
