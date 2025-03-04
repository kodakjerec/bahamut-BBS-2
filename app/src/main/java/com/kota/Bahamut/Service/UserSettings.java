package com.kota.Bahamut.Service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.kota.ASFramework.UI.ASToast;
import com.kota.Telnet.PropertiesOperator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class UserSettings {
    static final String PERF_NAME = "user_setting";
    static final String PROPERTIES_ANIMATION_DISABLE = "AnimationDisable"; // 換頁動畫
    static  final String PROPERTIES_BOARD_MOVE_DISABLE = "BoardMoveDisable"; // 看板上一頁/下一頁
    static final String PROPERTIES_ARTICLE_MOVE_DISABLE = "ArticleModeDisable"; // 文章首篇/末篇
    static final String PROPERTIES_ARTICLE_VIEW_MODE = "ArticleViewMode"; // 0-文字模式 1-telnet模式
    static final String PROPERTIES_GESTURE_ON_BOARD = "GestureOnBoard"; // 滑動手勢
    static final String PROPERTIES_BLOCK_LIST = "BlockList"; // 黑名單 list, 字串, ex: aaa,bbb,ccc
    static final String PROPERTIES_BLOCK_LIST_ENABLE = "BlockListEnable"; // 啟用黑名單
    static final String PROPERTIES_BLOCK_LIST_FOR_TITLE = "BlockListForTitle"; // 黑名單套用至標題
    static final String PROPERTIES_EXTRA_TOOLBAR_ENABLE = "ExtraToolbarEnable"; // 開啟工具列
    static final String PROPERTIES_KEEP_WIFI_ENABLE = "KeepWifiEnable"; // 防止Wifi因為待機而中斷
    static final String PROPERTIES_PASSWORD = "Password";
    static final String PROPERTIES_USERNAME = "Username";
    static final String PROPERTIES_SAVE_LOGON_USER = "SaveLogonUser"; // 記住我的資料
    static final String PROPERTIES_AUTO_TO_CHAT = "AutoToChat"; // 使用自動登入
    static final String PROPERTIES_VIP = "VIP"; // VIP 權限
    static final String PROPERTIES_SCREEN_ORIENTATION = "ScreenOrientation"; // 螢幕方向
    static final String PROPERTIES_TOOLBAR_LOCATION = "ToolBarLocation"; // 工具列位置
    static final String PROPERTIES_TOOLBAR_ORDER = "ToolBarOrder"; // 工具列順序
    static final String PROPERTIES_DRAWER_LOCATION = "DrawerLocation"; // 側滑選單
    static final String PROPERTIES_TOOLBAR_IDLE = "ToolBarIdle"; // 浮動工具列閒置多久隱藏
    static final String PROPERTIES_TOOLBAR_ALPHA = "ToolBarAlpha"; // 浮動工具列閒置多久隱藏不透明度
    static final String PROPERTIES_LINK_AUTO_SHOW = "LinkAutoShow"; // 連結自動預覽
    static final String PROPERTIES_LINK_SHOW_THUMBNAIL = "LinkShowThumbnail"; // 顯示預覽圖
    static final String PROPERTIES_LINK_SHOW_ONLY_WIFI = "LinkShowOnlyWifi"; // 只在Wifi下顯示預覽圖
    static final String PROPERTIES_ARTICLE_HEADS = "ArticleHeaders"; // 文章標題清單
    static final String PROPERTIES_ARTICLE_EXPRESSIONS = "ArticleExpressions"; // 表情符號清單
    static final String PROPERTIES_SHORT_URL_NON_ID = "ShortUrlNonId"; // 短網址,開啟去識別化

    // 執行階段比較不重要的設定
    static final String floatingLocationX = "floatingLocationX"; // 浮動工具列位置 X
    static final String floatingLocationY = "floatingLocationY"; // 浮動工具列位置 Y
    static final String noVipShortenTimes = "noVipShortenTimes"; // 非VIP轉檔限制

    // 其他設定
    static String _blockListDefault = "guest"; // 黑名單 list, 必定小寫, 字串, ex: aaa,bbb,ccc
    Context _context;
    
    public static SharedPreferences _sharedPref;
    public static SharedPreferences.Editor _editor;
    static final String _articleHeadersDefault = "不加 ▼,[問題],[情報],[心得],[討論],[攻略],[秘技],[閒聊],[程設],[職場],[推廣],[手機],[平板],[新番],[電影],[新聞],[其它]";
    static final String _articleExpressions = "( >_0)b,( ;-w-)a,( -3-)y-~,ˋ(°▽ ° )ノˋ( ° ▽° )ノ,#/-_-)/~╨──╨,(||￣▽￣)a,o( -_-)=0))-3-)/,(#‵′)o,O(‵皿′)o,( T_T),(o_O ),_ψ(._. ),v(￣︶￣)y,ㄟ(￣▽￣ㄟ)...,(っ´▽`)っ,m(_ _)m,ˋ(°ω ° )ノ,◢▆▅▄▃崩╰(〒皿〒)╯潰▃▄▅▇◣,( O口O)!?, ☆━━━(ﾟ∀ﾟ)━━━, *[1;33m洽特*[m";

    public UserSettings(Context context) {
        _context = context;
        upgrade();
    }

    void upgrade() {
        String settings_file_path = _context.getFilesDir().getPath() + "/default_login.properties";
        _sharedPref = _context.getSharedPreferences(PERF_NAME, 0);
        _editor = _sharedPref.edit();
        if (new File(settings_file_path).exists()) {
            if (_sharedPref.getInt("upgrade", 0) != 1) {
                PropertiesOperator prep = new PropertiesOperator(settings_file_path);
                prep.load();
                String username = prep.getPropertiesString(PROPERTIES_USERNAME);
                String password = prep.getPropertiesString(PROPERTIES_PASSWORD);
                boolean save_logon_user = prep.getPropertiesBoolean(PROPERTIES_SAVE_LOGON_USER);
                int article_view_mode = prep.getPropertiesInteger(PROPERTIES_ARTICLE_VIEW_MODE);
                String block_list = prep.getPropertiesString(PROPERTIES_BLOCK_LIST);
                boolean block_list_enable = prep.getPropertiesBoolean(PROPERTIES_BLOCK_LIST_ENABLE);
                boolean block_list_for_title = prep.getPropertiesBoolean(PROPERTIES_BLOCK_LIST_FOR_TITLE);
                boolean keep_wifi = prep.getPropertiesBoolean(PROPERTIES_KEEP_WIFI_ENABLE);
                boolean animation_disable = prep.getPropertiesBoolean(PROPERTIES_ANIMATION_DISABLE);
                boolean extra_toolbar = prep.getPropertiesBoolean(PROPERTIES_EXTRA_TOOLBAR_ENABLE);
                boolean gesture_on_board = prep.getPropertiesBoolean(PROPERTIES_GESTURE_ON_BOARD);
                boolean auto_to_chat = prep.getPropertiesBoolean(PROPERTIES_AUTO_TO_CHAT);
                boolean is_vip = prep.getPropertiesBoolean(PROPERTIES_VIP);
                boolean linkAutoShow = prep.getPropertiesBoolean(PROPERTIES_LINK_AUTO_SHOW);
                boolean linkShowThumbnail = prep.getPropertiesBoolean(PROPERTIES_LINK_SHOW_THUMBNAIL);
                boolean linkShowOnlyWifi = prep.getPropertiesBoolean(PROPERTIES_LINK_SHOW_ONLY_WIFI);
                int toolbar_location = prep.getPropertiesInteger(PROPERTIES_TOOLBAR_LOCATION);
                int toolbar_order = prep.getPropertiesInteger(PROPERTIES_TOOLBAR_ORDER);
                int drawer_location = prep.getPropertiesInteger(PROPERTIES_DRAWER_LOCATION);
                float toolbar_idle = prep.getPropertiesFloat(PROPERTIES_TOOLBAR_IDLE);
                float toolbar_alpha = prep.getPropertiesFloat(PROPERTIES_TOOLBAR_ALPHA);
                String article_headers = prep.getPropertiesString(PROPERTIES_ARTICLE_HEADS);
                boolean shortUrlNonId = prep.getPropertiesBoolean(PROPERTIES_SHORT_URL_NON_ID);
                float floating_location_x = prep.getPropertiesFloat(floatingLocationX);
                float floating_location_y = prep.getPropertiesFloat(floatingLocationY);
                int varNoVipShortenTimes = prep.getPropertiesInteger(noVipShortenTimes);

                _editor.putString(PROPERTIES_USERNAME, username);
                _editor.putString(PROPERTIES_PASSWORD, password);
                _editor.putBoolean(PROPERTIES_SAVE_LOGON_USER, save_logon_user);
                _editor.putInt(PROPERTIES_ARTICLE_VIEW_MODE, article_view_mode);
                _editor.putString(PROPERTIES_BLOCK_LIST, block_list);
                _editor.putBoolean(PROPERTIES_BLOCK_LIST_ENABLE, block_list_enable);
                _editor.putBoolean(PROPERTIES_BLOCK_LIST_FOR_TITLE, block_list_for_title);
                _editor.putBoolean(PROPERTIES_KEEP_WIFI_ENABLE, keep_wifi);
                _editor.putBoolean(PROPERTIES_ANIMATION_DISABLE, animation_disable);
                _editor.putBoolean(PROPERTIES_EXTRA_TOOLBAR_ENABLE, extra_toolbar);
                _editor.putBoolean(PROPERTIES_GESTURE_ON_BOARD, gesture_on_board);
                _editor.putBoolean(PROPERTIES_AUTO_TO_CHAT, auto_to_chat);
                _editor.putBoolean(PROPERTIES_VIP, is_vip);
                _editor.putInt(PROPERTIES_TOOLBAR_LOCATION, toolbar_location);
                _editor.putInt(PROPERTIES_TOOLBAR_ORDER, toolbar_order);
                _editor.putInt(PROPERTIES_DRAWER_LOCATION, drawer_location);
                _editor.putFloat(PROPERTIES_TOOLBAR_IDLE, toolbar_idle);
                _editor.putFloat(PROPERTIES_TOOLBAR_ALPHA, toolbar_alpha);
                _editor.putBoolean(PROPERTIES_LINK_AUTO_SHOW, linkAutoShow);
                _editor.putBoolean(PROPERTIES_LINK_SHOW_THUMBNAIL, linkShowThumbnail);
                _editor.putBoolean(PROPERTIES_LINK_SHOW_ONLY_WIFI, linkShowOnlyWifi);
                _editor.putString(PROPERTIES_ARTICLE_HEADS, article_headers);
                _editor.putBoolean(PROPERTIES_SHORT_URL_NON_ID, shortUrlNonId);
                _editor.putFloat(floatingLocationX, floating_location_x);
                _editor.putFloat(floatingLocationY, floating_location_y);
                _editor.putInt(noVipShortenTimes, varNoVipShortenTimes);
                _editor.putInt("upgrade", 1);
                _editor.commit();
            }
        }
    }

    // 通知更新
    public static void notifyDataUpdated() {
        // 雲端備份
        if (NotificationSettings.getCloudSave()) {
            CloudBackup cloudBackup = new CloudBackup();
            cloudBackup.backup();
        }
    }

    public static void setPropertiesDrawerLocation(int choice) {
        _editor.putInt(PROPERTIES_DRAWER_LOCATION, choice).apply();
    }
    public static int getPropertiesDrawerLocation() {
        return _sharedPref.getInt(PROPERTIES_DRAWER_LOCATION, 0);
    }
    public static void setPropertiesToolbarLocation(int choice) {
        _editor.putInt(PROPERTIES_TOOLBAR_LOCATION, choice).apply();
    }
    public static int getPropertiesToolbarLocation() {
        return _sharedPref.getInt(PROPERTIES_TOOLBAR_LOCATION, 0);
    }
    public static void setPropertiesToolbarOrder(int choice) {
        _editor.putInt(PROPERTIES_TOOLBAR_ORDER, choice).apply();
    }

    public static int getPropertiesToolbarOrder() {
        return _sharedPref.getInt(PROPERTIES_TOOLBAR_ORDER, 0);
    }
    public static void setPropertiesScreenOrientation(int choice) {
        _editor.putInt(PROPERTIES_SCREEN_ORIENTATION, choice).apply();
    }
    public static int getPropertiesScreenOrientation() {
        return _sharedPref.getInt(PROPERTIES_SCREEN_ORIENTATION, 0);
    }
    public static void setPropertiesVIP(boolean isEnable) {
        _editor.putBoolean(PROPERTIES_VIP, isEnable).apply();
    }
    public static boolean getPropertiesVIP() {
        return _sharedPref.getBoolean(PROPERTIES_VIP, false);
    }

    public static void setPropertiesAutoToChat(boolean isEnable) {
        _editor.putBoolean(PROPERTIES_AUTO_TO_CHAT, isEnable).apply();
    }

    public static boolean getPropertiesAutoToChat() {
        return _sharedPref.getBoolean(PROPERTIES_AUTO_TO_CHAT, false);
    }
    
    public static void setPropertiesGestureOnBoardEnable(boolean isEnable) {
        _editor.putBoolean(PROPERTIES_GESTURE_ON_BOARD, isEnable).apply();
    }

    public static boolean getPropertiesGestureOnBoardEnable() {
        return _sharedPref.getBoolean(PROPERTIES_GESTURE_ON_BOARD, true);
    }

    public static void setPropertiesExternalToolbarEnable(boolean isEnable) {
        _editor.putBoolean(PROPERTIES_EXTRA_TOOLBAR_ENABLE, isEnable).apply();
    }

    public static boolean getPropertiesExternalToolbarEnable() {
        return _sharedPref.getBoolean(PROPERTIES_EXTRA_TOOLBAR_ENABLE, false);
    }

    public static void setPropertiesUsername(String username) {
        _editor.putString(PROPERTIES_USERNAME, username).apply();
    }

    public static String getPropertiesUsername() {
        return _sharedPref.getString(PROPERTIES_USERNAME, "");
    }

    public static void setPropertiesPassword(String password) {
        _editor.putString(PROPERTIES_PASSWORD, password).apply();
    }

    public static String getPropertiesPassword() {
        return _sharedPref.getString(PROPERTIES_PASSWORD, "");
    }

    public static void setPropertiesSaveLogonUser(boolean save) {
        _editor.putBoolean(PROPERTIES_SAVE_LOGON_USER, save).apply();
    }
    public static boolean getPropertiesSaveLogonUser() {
        return _sharedPref.getBoolean(PROPERTIES_SAVE_LOGON_USER, false);
    }

    public static void setPropertiesAnimationEnable(boolean enable) {
        _editor.putBoolean(PROPERTIES_ANIMATION_DISABLE, enable).apply();
    }
    public static boolean getPropertiesAnimationEnable() {
        return _sharedPref.getBoolean(PROPERTIES_ANIMATION_DISABLE, true);
    }

    /** 看板上一頁/下一頁 */
    public static void setPropertiesBoardMoveDisable(int isDisable) {
        _editor.putInt(PROPERTIES_BOARD_MOVE_DISABLE, isDisable).apply();
    }
    public static int getPropertiesBoardMoveEnable() {
        return _sharedPref.getInt(PROPERTIES_BOARD_MOVE_DISABLE, 0);
    }

    /** 文章首篇/末篇 */
    public static void setPropertiesArticleMoveDisable(boolean isDisable) {
        _editor.putBoolean(PROPERTIES_ARTICLE_MOVE_DISABLE, isDisable).apply();
    }
    public static boolean getPropertiesArticleMoveEnable() {
        return _sharedPref.getBoolean(PROPERTIES_ARTICLE_MOVE_DISABLE, true);
    }

    public static void setPropertiesArticleViewState(int state) {
        _editor.putInt(PROPERTIES_ARTICLE_VIEW_MODE, state).apply();
    }
    public static int getPropertiesArticleViewMode() {
        return _sharedPref.getInt(PROPERTIES_ARTICLE_VIEW_MODE, 0);
    }

    public static void exchangeArticleViewMode() {
        setPropertiesArticleViewState(1 - getPropertiesArticleViewMode());
    }

    // 取出所有符號
    public static String[] getArticleHeaders() {
        String _source = _sharedPref.getString(PROPERTIES_ARTICLE_HEADS, _articleHeadersDefault);
        return _source.split(",");
    }
    public static void resetArticleHeaders() {
        _editor.putString(PROPERTIES_ARTICLE_HEADS, _articleHeadersDefault).apply();
    }
    public static void setArticleHeaders(List<String> _stringList) {
        String _saveString = String.join(",",_stringList);
        _editor.putString(PROPERTIES_ARTICLE_HEADS, _saveString).apply();
    }


    // 取出所有表情
    public static String[] getArticleExpressions() {
        String _source = _sharedPref.getString(PROPERTIES_ARTICLE_EXPRESSIONS, _articleExpressions);
        return _source.split(",");
    }
    public static void resetArticleExpressions() {
        _editor.putString(PROPERTIES_ARTICLE_EXPRESSIONS, _articleExpressions).apply();
    }
    public static void setArticleExpressions(List<String> _stringList) {
        String _saveString = String.join(",",_stringList);
        _editor.putString(PROPERTIES_ARTICLE_EXPRESSIONS, _saveString).apply();
    }

    // 取出黑名單(格式化後)
    public static List<String> getBlockList() {
        String blockListString = _sharedPref.getString(PROPERTIES_BLOCK_LIST, _blockListDefault);

        List<String> _block_list = new ArrayList<String>();
        if (blockListString.length() > 0) {
            for (String block_name : blockListString.split(",")) {
                if (block_name.length() > 0) {
                    _block_list.add(block_name);
                }
            }
        }

        return _block_list;
    }
    // 重置黑名單
    public static void resetBlockList() {
        _editor.putString(PROPERTIES_BLOCK_LIST, _blockListDefault).apply();
    }
    // 更新黑名單時同時更新緩存
    public static void setBlockList(List<String> aList) {
        StringBuilder list_string = new StringBuilder();
        if (aList == null || aList.size() == 0) {
            list_string = new StringBuilder("guest");
            ASToast.showLongToast("黑名單至少保留guest，為了政策");
        } else {
            for (String s : aList) {
                list_string.append(s.trim()).append(",");
            }
        }
        
        _editor.putString(PROPERTIES_BLOCK_LIST, list_string.toString()).apply();
        
        // 更新緩存
        updateBlockListCache();
    }

    // 更新緩存的輔助方法
    private static void updateBlockListCache() {
        String blockListString = _sharedPref.getString(PROPERTIES_BLOCK_LIST, _blockListDefault);
        String[] blockStrings = blockListString.split(",");
        blockListCache = new HashSet<>();
        for (String s : blockStrings) {
            if (!s.isEmpty()) {
                blockListCache.add(s);
            }
        }
    }

    // 加入黑名單緩存
    private static HashSet<String> blockListCache = null;

    // 檢查是否在黑名單中, 精確比對
    @SuppressLint({"DefaultLocale"})
    public static boolean isBlockListContains(String aName) {
        if (aName == null || aName.isEmpty())
            return false;

        // 初始化緩存
        if (blockListCache == null) {
            updateBlockListCache();
        }

        // 使用緩存的 HashSet 來檢查
        for (String keyword : blockListCache) {
            if (aName.equals(keyword)) {
                return true;
            }
        }
        return false;
    }
    // 模糊比對
    @SuppressLint({"DefaultLocale"})
    public static boolean isBlockListContainsFuzzy(String aName) {
        if (aName == null || aName.isEmpty())
            return false;

        // 初始化緩存
        if (blockListCache == null) {
            updateBlockListCache();
        }

        // 使用緩存的 HashSet 來檢查
        for (String keyword : blockListCache) {
            if (aName.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    public static void setPropertiesBlockListEnable(boolean enable) {
        _editor.putBoolean(PROPERTIES_BLOCK_LIST_ENABLE, enable).apply();
    }

    public static boolean getPropertiesBlockListEnable() {
        return _sharedPref.getBoolean(PROPERTIES_BLOCK_LIST_ENABLE, false);
    }

    public static void setPropertiesBlockListForTitle(boolean enable) {
        _editor.putBoolean(PROPERTIES_BLOCK_LIST_FOR_TITLE, enable).apply();
    }

    public static boolean getPropertiesBlockListForTitle() {
        return _sharedPref.getBoolean(PROPERTIES_BLOCK_LIST_FOR_TITLE, false);
    }
    public static void setPropertiesKeepWifi(boolean enable) {
        _editor.putBoolean(PROPERTIES_KEEP_WIFI_ENABLE, enable).apply();
    }

    public static boolean getPropertiesKeepWifi() {
        return _sharedPref.getBoolean(PROPERTIES_KEEP_WIFI_ENABLE, true);
    }

    public static List<Float> getFloatingLocation() {
        SharedPreferences pref = _sharedPref;
        List<Float> list = new ArrayList<>();
        try {
            list.add(pref.getFloat(floatingLocationX, (float) -1));
            list.add(pref.getFloat(floatingLocationY, (float) -1));
        } catch (Exception ignored) {
            Integer tempX = pref.getInt(floatingLocationX, -1);
            list.add(tempX.floatValue());
            Integer tempY = pref.getInt(floatingLocationY, -1);
            list.add(tempY.floatValue());
        }
        return list;
    }

    public static void setFloatingLocation(float x, float y) {
        _editor.putFloat(floatingLocationX, x);
        _editor.putFloat(floatingLocationY, y).apply();
    }

    public static void setToolbarIdle(float idle) {
        _editor.putFloat(PROPERTIES_TOOLBAR_IDLE, idle).apply();
    }
    public static float getToolbarIdle() {
        try {
            return _sharedPref.getFloat(PROPERTIES_TOOLBAR_IDLE, 2.0f);
        } catch (ClassCastException e) {
            int value = _sharedPref.getInt(PROPERTIES_TOOLBAR_IDLE, 2);
            return (float) value;
        }
    }
    public static void setToolbarAlpha(float alpha) {
        _editor.putFloat(PROPERTIES_TOOLBAR_ALPHA, alpha).apply();
    }
    public static float getToolbarAlpha() {
        try {
            return _sharedPref.getFloat(PROPERTIES_TOOLBAR_ALPHA, 20.0f);
        } catch (ClassCastException e) {
            int value = _sharedPref.getInt(PROPERTIES_TOOLBAR_ALPHA, 20);
            return (float) value;
        }
    }
    
    public static void setPropertiesLinkAutoShow(boolean enable) {
        _editor.putBoolean(PROPERTIES_LINK_AUTO_SHOW, enable).apply();
    }
    public static boolean getLinkAutoShow() {
        return _sharedPref.getBoolean(PROPERTIES_LINK_AUTO_SHOW, true);
    }
    public static void setLinkShowThumbnail(boolean enable) {
        _editor.putBoolean(PROPERTIES_LINK_SHOW_THUMBNAIL, enable).apply();
    }
    public static boolean getLinkShowThumbnail() {
        return _sharedPref.getBoolean(PROPERTIES_LINK_SHOW_THUMBNAIL, false);
    }
    public static void setLinkShowOnlyWifi(boolean enable) {
        _editor.putBoolean(PROPERTIES_LINK_SHOW_ONLY_WIFI, enable).apply();
    }
    public static boolean getLinkShowOnlyWifi() {
        return _sharedPref.getBoolean(PROPERTIES_LINK_SHOW_ONLY_WIFI, false);
    }

    public static void setPropertiesNoVipShortenTimes(int times) {
        _editor.putInt(noVipShortenTimes, times).apply();
    }
    public static int getPropertiesNoVipShortenTimes() {
        return _sharedPref.getInt(noVipShortenTimes, 0);
    }

    public static void setPropertiesShortUrlNonId(boolean enable) {
        _editor.putBoolean(PROPERTIES_SHORT_URL_NON_ID, enable).apply();
    }
    public static boolean getShortUrlNonId() {
        return _sharedPref.getBoolean(PROPERTIES_SHORT_URL_NON_ID, true);
    }
}
