package com.kota.Bahamut.Service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.kota.Telnet.PropertiesOperator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class UserSettings {
    static final String PERF_NAME = "user_setting";
    static final String PROPERTIES_ANIMATION_DISABLE = "AnimationDisable"; // 換頁動畫
    static final String PROPERTIES_ARTICLE_MOVE_DISABLE = "ArticleModeDisable"; // 文章首篇/末篇
    static final String PROPERTIES_ARTICLE_VIEW_MODE = "ArticleViewMode"; // 0-文字模式 1-telnet模式
    static final String PROPERTIES_GESTURE_ON_BOARD = "GestureOnBoard"; // 滑動手勢
    static final String PROPERTIES_BLOCK_LIST = "BlockList"; // 黑名單 list, 字串, ex: aaa,bbb,ccc
    static final String PROPERTIES_BLOCK_LIST_ENABLE = "BlockListEnable"; // 啟用黑名單
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

    // 執行階段比較不重要的設定
    static final String floatingLocationX = "floatingLocationX"; // 浮動工具列位置 X
    static final String floatingLocationY = "floatingLocationY"; // 浮動工具列位置 Y

    static Vector<String> _block_list = null; // 轉換後的黑名單清單
    static String _block_list_string_lower_cased = null; // 黑名單 list, 必定小寫, 字串, ex: aaa,bbb,ccc
    Context _context;
    
    static SharedPreferences _sharedPref;
    static SharedPreferences.Editor _editor;
    static final String[] _headers = {"不加 ▼", "[問題]", "[情報]", "[心得]", "[討論]", "[攻略]", "[秘技]", "[閒聊]", "[程設]", "[職場]", "[推廣]", "[手機]", "[平板]", "[新番]", "[電影]", "[新聞]", "[其它]"};
    static final String[] _expressions = {"( >_0)b", "( ;-w-)a", "( -3-)y-~", "ˋ(°▽ ° )ノˋ( ° ▽° )ノ", "#/-_-)/~╨──╨", "(||￣▽￣)a", "o( -_-)=0))-3-)/", "(#‵′)o", "O(‵皿′)o", "( T_T)", "(o_O )", "_ψ(._. )", "v(￣︶￣)y", "ㄟ(￣▽￣ㄟ)...", "(っ´▽`)っ", "m(_ _)m", "ˋ(°ω ° )ノ", "◢▆▅▄▃崩╰(〒皿〒)╯潰▃▄▅▇◣", "( O口O)!?", "☆━━━(ﾟ∀ﾟ)━━━"};

    public static int getIndexOfHeader(String aHeader) {
        if (aHeader == null || aHeader.length() == 0) {
            return 0;
        }
        for (int i = 1; i < _headers.length; i++) {
            if (_headers[i].equals(aHeader)) {
                return i;
            }
        }
        return -1;
    }

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
                float floating_location_x = prep.getPropertiesFloat(floatingLocationX);
                float floating_location_y = prep.getPropertiesFloat(floatingLocationY);

                _editor.putString(PROPERTIES_USERNAME, username);
                _editor.putString(PROPERTIES_PASSWORD, password);
                _editor.putBoolean(PROPERTIES_SAVE_LOGON_USER, save_logon_user);
                _editor.putInt(PROPERTIES_ARTICLE_VIEW_MODE, article_view_mode);
                _editor.putString(PROPERTIES_BLOCK_LIST, block_list);
                _editor.putBoolean(PROPERTIES_BLOCK_LIST_ENABLE, block_list_enable);
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
                _editor.putFloat(floatingLocationX, floating_location_x);
                _editor.putFloat(floatingLocationY, floating_location_y);
                _editor.putInt("upgrade", 1);
                _editor.commit();
            }
        }
    }

    public static void notifyDataUpdated() {
    }

    public static void setPropertiesDrawerLocation(int choice) {
        _editor.putInt(PROPERTIES_DRAWER_LOCATION, choice);
        _editor.apply();
    }
    public static int getPropertiesDrawerLocation() {
        return _sharedPref.getInt(PROPERTIES_DRAWER_LOCATION, 0);
    }
    public static void setPropertiesToolbarLocation(int choice) {
        _editor.putInt(PROPERTIES_TOOLBAR_LOCATION, choice);
        _editor.apply();
    }
    public static int getPropertiesToolbarLocation() {
        return _sharedPref.getInt(PROPERTIES_TOOLBAR_LOCATION, 0);
    }
    public static void setPropertiesToolbarOrder(int choice) {
        _editor.putInt(PROPERTIES_TOOLBAR_ORDER, choice);
        _editor.apply();
    }

    public static int getPropertiesToolbarOrder() {
        return _sharedPref.getInt(PROPERTIES_TOOLBAR_ORDER, 0);
    }
    public static void setPropertiesScreenOrientation(int choice) {
        _editor.putInt(PROPERTIES_SCREEN_ORIENTATION, choice);
        _editor.apply();
    }
    public static int getPropertiesScreenOrientation() {
        return _sharedPref.getInt(PROPERTIES_SCREEN_ORIENTATION, 0);
    }
    public static void setPropertiesVIP(boolean isEnable) {
        _editor.putBoolean(PROPERTIES_VIP, isEnable);
        _editor.apply();
    }
    public static boolean getPropertiesVIP() {
        return _sharedPref.getBoolean(PROPERTIES_VIP, false);
    }

    public static void setPropertiesAutoToChat(boolean isEnable) {
        _editor.putBoolean(PROPERTIES_AUTO_TO_CHAT, isEnable);
        _editor.apply();
    }

    public static boolean getPropertiesAutoToChat() {
        return _sharedPref.getBoolean(PROPERTIES_AUTO_TO_CHAT, false);
    }
    
    public static void setPropertiesGestureOnBoardEnable(boolean isEnable) {
        _editor.putBoolean(PROPERTIES_GESTURE_ON_BOARD, isEnable);
        _editor.apply();
    }

    public static boolean getPropertiesGestureOnBoardEnable() {
        return _sharedPref.getBoolean(PROPERTIES_GESTURE_ON_BOARD, true);
    }

    public static void setPropertiesExternalToolbarEnable(boolean isEnable) {
        _editor.putBoolean(PROPERTIES_EXTRA_TOOLBAR_ENABLE, isEnable);
        _editor.apply();
    }

    public static boolean getPropertiesExternalToolbarEnable() {
        return _sharedPref.getBoolean(PROPERTIES_EXTRA_TOOLBAR_ENABLE, false);
    }

    public static void setPropertiesUsername(String username) {
        _editor.putString(PROPERTIES_USERNAME, username);
        _editor.apply();
    }

    public static String getPropertiesUsername() {
        return _sharedPref.getString(PROPERTIES_USERNAME, "");
    }

    public static void setPropertiesPassword(String password) {
        
        _editor.putString(PROPERTIES_PASSWORD, password);
        _editor.apply();
    }

    public static String getPropertiesPassword() {
        return _sharedPref.getString(PROPERTIES_PASSWORD, "");
    }

    public static void setPropertiesSaveLogonUser(boolean save) {
        
        _editor.putBoolean(PROPERTIES_SAVE_LOGON_USER, save);
        _editor.apply();
    }
    public static boolean getPropertiesSaveLogonUser() {
        return _sharedPref.getBoolean(PROPERTIES_SAVE_LOGON_USER, false);
    }

    public static void setPropertiesAnimationEnable(boolean enable) {
        
        _editor.putBoolean(PROPERTIES_ANIMATION_DISABLE, enable);
        _editor.apply();
    }
    public static boolean getPropertiesAnimationEnable() {
        return _sharedPref.getBoolean(PROPERTIES_ANIMATION_DISABLE, true);
    }

    public static void setPropertiesArticleMoveDisable(boolean isDisable) {
        _editor.putBoolean(PROPERTIES_ARTICLE_MOVE_DISABLE, isDisable);
        _editor.apply();
    }
    public static boolean getPropertiesArticleMoveEnable() {
        return _sharedPref.getBoolean(PROPERTIES_ARTICLE_MOVE_DISABLE, true);
    }

    public static void setPropertiesArticleViewState(int state) {
        
        _editor.putInt(PROPERTIES_ARTICLE_VIEW_MODE, state);
        _editor.apply();
    }
    public static int getPropertiesArticleViewMode() {
        return _sharedPref.getInt(PROPERTIES_ARTICLE_VIEW_MODE, 0);
    }

    public static void exchangeArticleViewMode() {
        setPropertiesArticleViewState(1 - getPropertiesArticleViewMode());
    }


    // 取出所有符號
    public static String[] getArticleHeaders() {
        return _headers;
    }

    // 取出特定符號
    public static String getArticleHeader(int index) {
        if (index <= 0 || index >= _headers.length) {
            return "";
        }
        return _headers[index];
    }

    // 取出所有表情符號
    public static String[] getSymbols() {
        return _expressions;
    }

    // 取出黑名單字串(原始檔)
    static String getBlockListString() {
        return _sharedPref.getString(PROPERTIES_BLOCK_LIST, "");
    }

    // 取出黑名單(格式化後)
    public static Vector<String> getBlockList() {
        String blockListString = getBlockListString();
        if (_block_list == null) {
            _block_list = new Vector<>();
            if (blockListString.length() > 0) {
                for (String block_name : blockListString.split(" *, *")) {
                    if (block_name.length() > 0) {
                        _block_list.add(block_name);
                    }
                }
            }
        }
        return _block_list;
    }

    // 更新黑名單
    public static void updateBlockList(Vector<String> aList) {
        StringBuilder list_string = new StringBuilder(",");
        if (aList == null || aList.size() == 0) {
            list_string = new StringBuilder();
        } else {
            for (String s : aList) {
                list_string.append(s.trim()).append(",");
            }
        }
        
        _editor.putString(PROPERTIES_BLOCK_LIST, list_string.toString());
        _editor.apply();
        _block_list = null;
        _block_list_string_lower_cased = null;
    }

    // 新增黑名單
    public static void addBlockName(String aBlockName) {
        Vector<String> new_list = new Vector<>(getBlockList());
        int ref = aBlockName.hashCode();
        boolean find = false;
        int i = 0;
        while (true) {
            if (i >= new_list.size()) {
                break;
            } else if (new_list.get(i).hashCode() > ref) {
                new_list.insertElementAt(aBlockName, i);
                find = true;
                break;
            } else {
                i++;
            }
        }
        if (!find) {
            new_list.add(aBlockName);
        }
        updateBlockList(new_list);
    }

    @SuppressLint({"DefaultLocale"})
    public static boolean isBlockListContains(String aName) {
        String blockListString = getBlockListString();
        if (_block_list_string_lower_cased == null) {
            _block_list_string_lower_cased = blockListString.toLowerCase();
        }
        return _block_list_string_lower_cased.contains("," + aName.toLowerCase() + ",");
    }

    public static void setPropertiesBlockListEnable(boolean enable) {
        _editor.putBoolean(PROPERTIES_BLOCK_LIST_ENABLE, enable);
        _editor.apply();
    }

    public static boolean getPropertiesBlockListEnable() {
        return _sharedPref.getBoolean(PROPERTIES_BLOCK_LIST_ENABLE, false);
    }

    @SuppressLint({"DefaultLocale"})
    public static String getBlockListLowCasedString() {
        if (_block_list_string_lower_cased == null) {
            _block_list_string_lower_cased = getBlockListString().toLowerCase();
        }
        return _block_list_string_lower_cased;
    }

    public static void setPropertiesKeepWifi(boolean enable) {
        _editor.putBoolean(PROPERTIES_KEEP_WIFI_ENABLE, enable);
        _editor.apply();
    }

    public static boolean getPropertiesKeepWifi() {
        return _sharedPref.getBoolean(PROPERTIES_KEEP_WIFI_ENABLE, true);
    }

    public static List<Float> getFloatingLocation() {
        SharedPreferences pref = _sharedPref;
        List<Float> list = new ArrayList<>();
        list.add(pref.getFloat(floatingLocationX, -1));
        list.add(pref.getFloat(floatingLocationY, -1));
        return list;
    }

    public static void setFloatingLocation(float x, float y) {
        _editor.putFloat(floatingLocationX, x);
        _editor.putFloat(floatingLocationY, y);
        _editor.apply();
    }

    public static void setToolbarIdle(float idle) {
        _editor.putFloat(PROPERTIES_TOOLBAR_IDLE, idle);
        _editor.apply();
    }
    public static float getToolbarIdle() {
        return _sharedPref.getFloat(PROPERTIES_TOOLBAR_IDLE, 2.0f);
    }
    public static void setToolbarAlpha(float alpha) {
        _editor.putFloat(PROPERTIES_TOOLBAR_ALPHA, alpha);
        _editor.apply();
    }
    public static float getToolbarAlpha() {
        return _sharedPref.getFloat(PROPERTIES_TOOLBAR_ALPHA, 20);
    }
    
    public static void setPropertiesLinkAutoShow(boolean enable) {
        _editor.putBoolean(PROPERTIES_LINK_AUTO_SHOW, enable);
        _editor.apply();
    }
    public static boolean getLinkAutoShow() {
        return _sharedPref.getBoolean(PROPERTIES_LINK_AUTO_SHOW, true);
    }
    public static void setPropertiesLinkShowThumbnail(boolean enable) {
        _editor.putBoolean(PROPERTIES_LINK_SHOW_THUMBNAIL, enable);
        _editor.apply();
    }
    public static boolean getLinkShowThumbnail() {
        return _sharedPref.getBoolean(PROPERTIES_LINK_SHOW_THUMBNAIL, false);
    }
    public static void setPropertiesLinkShowOnlyWifi(boolean enable) {
        _editor.putBoolean(PROPERTIES_LINK_SHOW_ONLY_WIFI, enable);
        _editor.apply();
    }
    public static boolean getLinkShowOnlyWifi() {
        return _sharedPref.getBoolean(PROPERTIES_LINK_SHOW_ONLY_WIFI, false);
    }
}
