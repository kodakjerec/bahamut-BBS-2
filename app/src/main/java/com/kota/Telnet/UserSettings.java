package com.kota.Telnet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class UserSettings {
    public static final String PERF_NAME = "user_setting";
    public static final String PROPERTIES_ANIMATION_DISABLE = "AnimationDisable"; // 換頁動畫
    public static final String PROPERTIES_ARTICLE_MOVE_DISABLE = "ArticleModeDisable"; // 文章首篇/末篇
    public static final String PROPERTIES_ARTICLE_VIEW_MODE = "ArticleViewMode"; // 0-文字模式 1-telnet模式
    public static final String PROPERTIES_GESTURE_ON_BOARD = "GestureOnBoard"; // 滑動手勢
    public static final String PROPERTIES_BLOCK_LIST = "BlockList"; // 黑名單 list, 字串, ex: aaa,bbb,ccc
    public static final String PROPERTIES_BLOCK_LIST_ENABLE = "BlockListEnable"; // 啟用黑名單
    public static final String PROPERTIES_EXTRA_TOOLBAR_ENABLE = "ExtraToolbarEnable"; // 開啟工具列
    public static final String PROPERTIES_KEEP_WIFI_ENABLE = "KeepWifiEnable"; // 防止Wifi因為待機而中斷
    public static final String PROPERTIES_PASSWORD = "Password";
    public static final String PROPERTIES_USERNAME = "Username";
    public static final String PROPERTIES_SAVE_LOGON_USER = "SaveLogonUser"; // 記住我的資料
    public static final String PROPERTIES_AUTO_TO_CHAT = "AutoToChat"; // 使用自動登入
    private static final String PROPERTIES_VIP = "VIP"; // VIP 權限
    private static final String PROPERTIES_SCREEN_ORIENTATION = "ScreenOrientation"; // 螢幕方向
    private static final String PROPERTIES_TOOLBAR_LOCATION = "ToolBarLocation"; // 工具列位置
    private static final String PROPERTIES_TOOLBAR_ORDER = "ToolBarOrder"; // 工具列順序
    private static final String PROPERTIES_DRAWER_LOCATION = "DrawerLocation"; // 側滑選單
    private static final String PROPERTIES_TOOLBAR_IDLE = "ToolBarIdle"; // 浮動工具列閒置多久隱藏
    private static final String PROPERTIES_TOOLBAR_ALPHA = "ToolBarAlpha"; // 浮動工具列閒置多久隱藏不透明度

    // 執行階段比較不重要的設定
    private static final String floatingLocationX = "floatingLocationX"; // 浮動工具列位置 X
    private static final String floatingLocationY = "floatingLocationY"; // 浮動工具列位置 Y

    private Vector<String> _block_list = null; // 轉換後的黑名單清單
    private String _block_list_string_lower_cased = null; // 黑名單 list, 必定小寫, 字串, ex: aaa,bbb,ccc
    Context _context;
    private final String[] _headers = {"不加 ▼", "[問題]", "[情報]", "[心得]", "[討論]", "[攻略]", "[秘技]", "[閒聊]", "[程設]", "[職場]", "[推廣]", "[手機]", "[平板]", "[新番]", "[電影]", "[新聞]", "[其它]"};
    private final String[] _symbols = {"( >_0)b", "( ;-w-)a", "( -3-)y-~", "ˋ(°▽ ° )ノˋ( ° ▽° )ノ", "#/-_-)/~╨──╨", "(||￣▽￣)a", "o( -_-)=0))-3-)/", "(#‵′)o", "O(‵皿′)o", "( T_T)", "(o_O )", "_ψ(._. )", "v(￣︶￣)y", "ㄟ(￣▽￣ㄟ)...", "(っ´▽`)っ", "m(_ _)m", "ˋ(°ω ° )ノ", "◢▆▅▄▃崩╰(〒皿〒)╯潰▃▄▅▇◣", "( O口O)!?", "☆━━━(ﾟ∀ﾟ)━━━"};

    public int getIndexOfHeader(String aHeader) {
        if (aHeader == null || aHeader.length() == 0) {
            return 0;
        }
        for (int i = 1; i < this._headers.length; i++) {
            if (this._headers[i].equals(aHeader)) {
                return i;
            }
        }
        return -1;
    }

    public UserSettings(Context context) {
        this._context = context;
        upgrade();
    }

    private void upgrade() {
        String settings_file_path = this._context.getFilesDir().getPath() + "/default_login.properties";
        if (new File(settings_file_path).exists()) {
            SharedPreferences perf = this._context.getSharedPreferences(PERF_NAME, 0);
            if (perf.getInt("upgrade", 0) != 1) {
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
                int toolbar_location = prep.getPropertiesInteger(PROPERTIES_TOOLBAR_LOCATION);
                int toolbar_order = prep.getPropertiesInteger(PROPERTIES_TOOLBAR_ORDER);
                int drawer_location = prep.getPropertiesInteger(PROPERTIES_DRAWER_LOCATION);
                float toolbar_idle = prep.getPropertiesFloat(PROPERTIES_TOOLBAR_IDLE);
                float toolbar_alpha = prep.getPropertiesFloat(PROPERTIES_TOOLBAR_ALPHA);
                float floating_location_x = prep.getPropertiesFloat(floatingLocationX);
                float floating_location_y = prep.getPropertiesFloat(floatingLocationY);

                SharedPreferences.Editor editor = perf.edit();
                editor.putString(PROPERTIES_USERNAME, username);
                editor.putString(PROPERTIES_PASSWORD, password);
                editor.putBoolean(PROPERTIES_SAVE_LOGON_USER, save_logon_user);
                editor.putInt(PROPERTIES_ARTICLE_VIEW_MODE, article_view_mode);
                editor.putString(PROPERTIES_BLOCK_LIST, block_list);
                editor.putBoolean(PROPERTIES_BLOCK_LIST_ENABLE, block_list_enable);
                editor.putBoolean(PROPERTIES_KEEP_WIFI_ENABLE, keep_wifi);
                editor.putBoolean(PROPERTIES_ANIMATION_DISABLE, animation_disable);
                editor.putBoolean(PROPERTIES_EXTRA_TOOLBAR_ENABLE, extra_toolbar);
                editor.putBoolean(PROPERTIES_GESTURE_ON_BOARD, gesture_on_board);
                editor.putBoolean(PROPERTIES_AUTO_TO_CHAT, auto_to_chat);
                editor.putBoolean(PROPERTIES_VIP, is_vip);
                editor.putInt(PROPERTIES_TOOLBAR_LOCATION, toolbar_location);
                editor.putInt(PROPERTIES_TOOLBAR_ORDER, toolbar_order);
                editor.putInt(PROPERTIES_DRAWER_LOCATION, drawer_location);
                editor.putFloat(PROPERTIES_TOOLBAR_IDLE, toolbar_idle);
                editor.putFloat(PROPERTIES_TOOLBAR_ALPHA, toolbar_alpha);
                editor.putFloat(floatingLocationX, floating_location_x);
                editor.putFloat(floatingLocationY, floating_location_y);
                editor.putInt("upgrade", 1);
                editor.apply();
            }
        }
    }

    public void notifyDataUpdated() {
    }

    public void setPropertiesDrawerLocation(int choice) {
        SharedPreferences.Editor editor = this._context.getSharedPreferences(PERF_NAME, 0).edit();
        editor.putInt(PROPERTIES_DRAWER_LOCATION, choice);
        editor.apply();
    }
    public int getPropertiesDrawerLocation() {
        return this._context.getSharedPreferences(PERF_NAME, 0).getInt(PROPERTIES_DRAWER_LOCATION, 0);
    }
    public void setPropertiesToolbarLocation(int choice) {
        SharedPreferences.Editor editor = this._context.getSharedPreferences(PERF_NAME, 0).edit();
        editor.putInt(PROPERTIES_TOOLBAR_LOCATION, choice);
        editor.apply();
    }
    public int getPropertiesToolbarLocation() {
        return this._context.getSharedPreferences(PERF_NAME, 0).getInt(PROPERTIES_TOOLBAR_LOCATION, 0);
    }
    public void setPropertiesToolbarOrder(int choice) {
        SharedPreferences.Editor editor = this._context.getSharedPreferences(PERF_NAME, 0).edit();
        editor.putInt(PROPERTIES_TOOLBAR_ORDER, choice);
        editor.apply();
    }

    public int getPropertiesToolbarOrder() {
        return this._context.getSharedPreferences(PERF_NAME, 0).getInt(PROPERTIES_TOOLBAR_ORDER, 0);
    }
    public void setPropertiesScreenOrientation(int choice) {
        SharedPreferences.Editor editor = this._context.getSharedPreferences(PERF_NAME, 0).edit();
        editor.putInt(PROPERTIES_SCREEN_ORIENTATION, choice);
        editor.apply();
    }
    public int getPropertiesScreenOrientation() {
        return this._context.getSharedPreferences(PERF_NAME, 0).getInt(PROPERTIES_SCREEN_ORIENTATION, 0);
    }
    public void setPropertiesVIP(boolean isEnable) {
        SharedPreferences.Editor editor = this._context.getSharedPreferences(PERF_NAME, 0).edit();
        editor.putBoolean(PROPERTIES_VIP, isEnable);
        editor.apply();
    }
    public boolean getPropertiesVIP() {
        return this._context.getSharedPreferences(PERF_NAME, 0).getBoolean(PROPERTIES_VIP, false);
    }

    public void setPropertiesAutoToChat(boolean isEnable) {
        SharedPreferences.Editor editor = this._context.getSharedPreferences(PERF_NAME, 0).edit();
        editor.putBoolean(PROPERTIES_AUTO_TO_CHAT, isEnable);
        editor.apply();
    }

    public boolean getPropertiesAutoToChat() {
        return this._context.getSharedPreferences(PERF_NAME, 0).getBoolean(PROPERTIES_AUTO_TO_CHAT, false);
    }
    
    public void setPropertiesGestureOnBoardEnable(boolean isEnable) {
        SharedPreferences.Editor editor = this._context.getSharedPreferences(PERF_NAME, 0).edit();
        editor.putBoolean(PROPERTIES_GESTURE_ON_BOARD, isEnable);
        editor.apply();
    }

    public boolean getPropertiesGestureOnBoardEnable() {
        return this._context.getSharedPreferences(PERF_NAME, 0).getBoolean(PROPERTIES_GESTURE_ON_BOARD, true);
    }

    public void setPropertiesExternalToolbarEnable(boolean isEnable) {
        SharedPreferences.Editor editor = this._context.getSharedPreferences(PERF_NAME, 0).edit();
        editor.putBoolean(PROPERTIES_EXTRA_TOOLBAR_ENABLE, isEnable);
        editor.apply();
    }

    public boolean getPropertiesExternalToolbarEnable() {
        return this._context.getSharedPreferences(PERF_NAME, 0).getBoolean(PROPERTIES_EXTRA_TOOLBAR_ENABLE, false);
    }

    public void setPropertiesUsername(String username) {
        SharedPreferences.Editor editor = this._context.getSharedPreferences(PERF_NAME, 0).edit();
        editor.putString(PROPERTIES_USERNAME, username);
        editor.apply();
    }

    public String getPropertiesUsername() {
        return this._context.getSharedPreferences(PERF_NAME, 0).getString(PROPERTIES_USERNAME, "");
    }

    public void setPropertiesPassword(String password) {
        SharedPreferences.Editor editor = this._context.getSharedPreferences(PERF_NAME, 0).edit();
        editor.putString(PROPERTIES_PASSWORD, password);
        editor.apply();
    }

    public String getPropertiesPassword() {
        return this._context.getSharedPreferences(PERF_NAME, 0).getString(PROPERTIES_PASSWORD, "");
    }

    public void setPropertiesSaveLogonUser(boolean save) {
        SharedPreferences.Editor editor = this._context.getSharedPreferences(PERF_NAME, 0).edit();
        editor.putBoolean(PROPERTIES_SAVE_LOGON_USER, save);
        editor.apply();
    }
    public boolean getPropertiesSaveLogonUser() {
        return this._context.getSharedPreferences(PERF_NAME, 0).getBoolean(PROPERTIES_SAVE_LOGON_USER, false);
    }

    public void setPropertiesAnimationEnable(boolean enable) {
        SharedPreferences.Editor editor = this._context.getSharedPreferences(PERF_NAME, 0).edit();
        editor.putBoolean(PROPERTIES_ANIMATION_DISABLE, enable);
        editor.apply();
    }
    public boolean getPropertiesAnimationEnable() {
        return this._context.getSharedPreferences(PERF_NAME, 0).getBoolean(PROPERTIES_ANIMATION_DISABLE, true);
    }

    public void setPropertiesArticleMoveDisable(boolean isDisable) {
        SharedPreferences.Editor editor = this._context.getSharedPreferences(PERF_NAME, 0).edit();
        editor.putBoolean(PROPERTIES_ARTICLE_MOVE_DISABLE, isDisable);
        editor.apply();
    }
    public boolean getPropertiesArticleMoveEnable() {
        return this._context.getSharedPreferences(PERF_NAME, 0).getBoolean(PROPERTIES_ARTICLE_MOVE_DISABLE, true);
    }

    public void setPropertiesArticleViewState(int state) {
        SharedPreferences.Editor editor = this._context.getSharedPreferences(PERF_NAME, 0).edit();
        editor.putInt(PROPERTIES_ARTICLE_VIEW_MODE, state);
        editor.apply();
    }
    public int getPropertiesArticleViewMode() {
        return this._context.getSharedPreferences(PERF_NAME, 0).getInt(PROPERTIES_ARTICLE_VIEW_MODE, 0);
    }

    public void exchangeArticleViewMode() {
        setPropertiesArticleViewState(1 - getPropertiesArticleViewMode());
    }


    public String[] getArticleHeaders() {
        return this._headers;
    }

    public String getArticleHeader(int index) {
        if (index <= 0 || index >= this._headers.length) {
            return "";
        }
        return this._headers[index];
    }

    public String[] getSymbols() {
        return this._symbols;
    }

    private String getBlockListString() {
        return this._context.getSharedPreferences(PERF_NAME, 0).getString(PROPERTIES_BLOCK_LIST, "");
    }

    public Vector<String> getBlockList() {
        String blockListString = getBlockListString();
        if (this._block_list == null) {
            this._block_list = new Vector<>();
            if (blockListString.length() > 0) {
                for (String block_name : blockListString.split(" *, *")) {
                    if (block_name.length() > 0) {
                        this._block_list.add(block_name);
                    }
                }
            }
        }
        return this._block_list;
    }

    public void updateBlockList(Vector<String> aList) {
        StringBuilder list_string = new StringBuilder(",");
        if (aList == null || aList.size() == 0) {
            list_string = new StringBuilder();
        } else {
            for (String s : aList) {
                list_string.append(s.trim()).append(",");
            }
        }
        SharedPreferences.Editor editor = this._context.getSharedPreferences(PERF_NAME, 0).edit();
        editor.putString(PROPERTIES_BLOCK_LIST, list_string.toString());
        editor.apply();
        this._block_list = null;
        this._block_list_string_lower_cased = null;
    }

    public void addBlockName(String aBlockName) {
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
    public boolean isBlockListContains(String aName) {
        String blockListString = getBlockListString();
        if (this._block_list_string_lower_cased == null) {
            this._block_list_string_lower_cased = blockListString.toLowerCase();
        }
        return this._block_list_string_lower_cased.contains("," + aName.toLowerCase() + ",");
    }

    public void setPropertiesBlockListEnable(boolean enable) {
        SharedPreferences.Editor editor = this._context.getSharedPreferences(PERF_NAME, 0).edit();
        editor.putBoolean(PROPERTIES_BLOCK_LIST_ENABLE, enable);
        editor.apply();
    }

    public boolean getPropertiesBlockListEnable() {
        return this._context.getSharedPreferences(PERF_NAME, 0).getBoolean(PROPERTIES_BLOCK_LIST_ENABLE, false);
    }

    @SuppressLint({"DefaultLocale"})
    public String getBlockListLowCasedString() {
        if (this._block_list_string_lower_cased == null) {
            this._block_list_string_lower_cased = getBlockListString().toLowerCase();
        }
        return this._block_list_string_lower_cased;
    }

    public void setPropertiesKeepWifi(boolean enable) {
        SharedPreferences.Editor editor = this._context.getSharedPreferences(PERF_NAME, 0).edit();
        editor.putBoolean(PROPERTIES_KEEP_WIFI_ENABLE, enable);
        editor.apply();
    }

    public boolean getPropertiesKeepWifi() {
        return this._context.getSharedPreferences(PERF_NAME, 0).getBoolean(PROPERTIES_KEEP_WIFI_ENABLE, true);
    }

    public List<Float> getFloatingLocation() {
        SharedPreferences pref = this._context.getSharedPreferences(PERF_NAME, 0);
        List<Float> list = new ArrayList<>();
        list.add(pref.getFloat(floatingLocationX, -1));
        list.add(pref.getFloat(floatingLocationY, -1));
        return list;
    }

    public void setFloatingLocation(float x, float y) {
        SharedPreferences.Editor editor = this._context.getSharedPreferences(PERF_NAME, 0).edit();
        editor.putFloat(floatingLocationX, x);
        editor.putFloat(floatingLocationY, y);
        editor.apply();
    }

    public void setToolbarIdle(float idle) {
        SharedPreferences.Editor editor = this._context.getSharedPreferences(PERF_NAME, 0).edit();
        editor.putFloat(PROPERTIES_TOOLBAR_IDLE, idle);
        editor.apply();
    }
    public float getToolbarIdle() {
        return this._context.getSharedPreferences(PERF_NAME, 0).getFloat(PROPERTIES_TOOLBAR_IDLE, 2.0f);
    }
    public void setToolbarAlpha(float alpha) {
        SharedPreferences.Editor editor = this._context.getSharedPreferences(PERF_NAME, 0).edit();
        editor.putFloat(PROPERTIES_TOOLBAR_ALPHA, alpha);
        editor.apply();
    }
    public float getToolbarAlpha() {
        return this._context.getSharedPreferences(PERF_NAME, 0).getFloat(PROPERTIES_TOOLBAR_ALPHA, 20);
    }
}
