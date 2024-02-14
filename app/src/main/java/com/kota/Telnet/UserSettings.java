package com.kota.Telnet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import com.kota.ASFramework.PageController.ASNavigationController;
import java.io.File;
import java.util.Vector;

public class UserSettings {
    public static final int ARTICLE_VIEW_MODE_TELNET = 1;
    public static final int ARTICLE_VIEW_MODE_TEXT = 0;
    public static final String PERF_NAME = "user_setting";
    public static final String PROPERTIES_ANIMATION_DISABLE = "AnimationDisable";
    public static final String PROPERTIES_ARTICLE_MOVE_DISABLE = "ArticleModeDisable";
    public static final String PROPERTIES_ARTICLE_VIEW_MODE = "ArticleViewMode";
    public static final String PROPERTIES_GETSURE_ON_BOARD = "GestureOnBoard";
    public static final String PROPERTIES_BLOCK_LIST = "BlockList";
    public static final String PROPERTIES_BLOCK_LIST_ENABLE = "BlockListEnable";
    public static final String PROPERTIES_EXTRA_TOOLBAR_ENABLE = "ExtraToolbarEnable";
    public static final String PROPERTIES_kEEP_POWER = "KeepPower";
    public static final String PROPERTIES_kEEP_WIFI_DISABLE = "KeepWifiDisable";
    public static final String PROPERTIES_LAST_CONNECTION_IS_NOT_OFFLINE_BY_USER = "LastConnectionIsNotOfflineByUser";
    public static final String PROPERTIES_PASSWORD = "Password";
    public static final String PROPERTIES_SAVE_LOGON_USER = "SaveLogonUser";
    public static final String PROPERTIES_USERNAME = "Username";
    public static final String PROPERTIES_AUTO_TO_CHAT = "AutoToChat"; // 使用自動登入
    private static final String PROPERTIES_VIP = "VIP"; // VIP 權限
    private static final String PROPERTIES_SCREEN_ORIENTATION = "ScreenOrientation"; // 螢幕方向
    private static final String isUnderAutoToChat = "isUnderAutoToChat"; // 正在自動登入ing

    private Vector<String> _block_list = null;
    private String _block_list_string_lower_cased = null;
    Context _context;
    private final String[] _headers = {"不加 ▼", "[問題]", "[情報]", "[心得]", "[討論]", "[攻略]", "[秘技]", "[閒聊]", "[程設]", "[職場]", "[推廣]", "[手機]", "[平板]", "[新番]", "[電影]", "[新聞]", "[其它]"};
    private final String[] _symbols = {"( >_0)b", "( ;-w-)a", "( -3-)y-~", "ˋ(°▽ ° )ノˋ( ° ▽° )ノ", "#/-_-)/~╨──╨", "(||￣▽￣)a", "o( -_-)=0))-3-)/", "(#‵′)o", "O(‵皿′)o", "( T_T)", "(o_O )", "_ψ(._. )", "v(￣︶￣)y", "ㄟ(￣▽￣ㄟ)...", "(っ´▽`)っ", "m(_ _)m", "ˋ(°ω ° )ノ", "◢▆▅▄▃崩╰(〒皿〒)╯潰▃▄▅▇◣", "( O口O)!?", "☆━━━(ﾟ∀ﾟ)━━━"};
    public Typeface _typeface = null;
    private boolean _updated = false;

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

    /* access modifiers changed from: package-private */
    public void upgrade() {
        String settings_file_path = this._context.getFilesDir().getPath() + "/default_login.properties";
        if (new File(settings_file_path).exists()) {
            SharedPreferences perf = this._context.getSharedPreferences(PERF_NAME, 0);
            if (perf.getInt("upgrade", 0) != 1) {
                PropertiesOperator perp = new PropertiesOperator(settings_file_path);
                perp.load();
                String username = perp.getPropertiesString(PROPERTIES_USERNAME);
                String password = perp.getPropertiesString(PROPERTIES_PASSWORD);
                boolean save_logon_user = perp.getPropertiesBoolean(PROPERTIES_SAVE_LOGON_USER);
                int article_view_mode = perp.getPropertiesInteger(PROPERTIES_ARTICLE_VIEW_MODE);
                String block_list = perp.getPropertiesString(PROPERTIES_BLOCK_LIST);
                boolean block_list_enable = perp.getPropertiesBoolean(PROPERTIES_BLOCK_LIST_ENABLE);
                boolean keep_wifi = perp.getPropertiesBoolean(PROPERTIES_kEEP_WIFI_DISABLE);
                boolean keep_power = perp.getPropertiesBoolean(PROPERTIES_kEEP_POWER);
                boolean offline_by_user = perp.getPropertiesBoolean(PROPERTIES_LAST_CONNECTION_IS_NOT_OFFLINE_BY_USER);
                boolean animation_disable = perp.getPropertiesBoolean(PROPERTIES_ANIMATION_DISABLE);
                boolean extra_toolbar = perp.getPropertiesBoolean(PROPERTIES_EXTRA_TOOLBAR_ENABLE);
                boolean getsure_on_board = perp.getPropertiesBoolean(PROPERTIES_GETSURE_ON_BOARD);
                boolean auto_to_chat = perp.getPropertiesBoolean(PROPERTIES_AUTO_TO_CHAT);
                boolean is_vip = perp.getPropertiesBoolean(PROPERTIES_VIP);
                SharedPreferences.Editor editor = perf.edit();
                editor.putString(PROPERTIES_USERNAME, username);
                editor.putString(PROPERTIES_PASSWORD, password);
                editor.putBoolean(PROPERTIES_SAVE_LOGON_USER, save_logon_user);
                editor.putInt(PROPERTIES_ARTICLE_VIEW_MODE, article_view_mode);
                editor.putString(PROPERTIES_BLOCK_LIST, block_list);
                editor.putBoolean(PROPERTIES_BLOCK_LIST_ENABLE, block_list_enable);
                editor.putBoolean(PROPERTIES_kEEP_WIFI_DISABLE, keep_wifi);
                editor.putBoolean(PROPERTIES_kEEP_POWER, keep_power);
                editor.putBoolean(PROPERTIES_LAST_CONNECTION_IS_NOT_OFFLINE_BY_USER, offline_by_user);
                editor.putBoolean(PROPERTIES_ANIMATION_DISABLE, animation_disable);
                editor.putBoolean(PROPERTIES_EXTRA_TOOLBAR_ENABLE, extra_toolbar);
                editor.putBoolean(PROPERTIES_GETSURE_ON_BOARD, getsure_on_board);
                editor.putBoolean(PROPERTIES_AUTO_TO_CHAT, auto_to_chat);
                editor.putBoolean(PROPERTIES_VIP, is_vip);
                editor.putInt("upgrade", 1);
                editor.apply();
            }
        }
    }

    public void notifyDataUpdated() {
        this._updated = true;
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
    public void setIsUnderAutoToChat(boolean isEnable) {
        SharedPreferences.Editor editor = this._context.getSharedPreferences(PERF_NAME, 0).edit();
        editor.putBoolean(isUnderAutoToChat, isEnable);
        editor.apply();
    }
    public boolean isUnderAutoToChat() {
        return this._context.getSharedPreferences(PERF_NAME, 0).getBoolean(isUnderAutoToChat, false);
    }
    
    public void setPropertiesGetsureOnBoardEnable(boolean isEnable) {
        SharedPreferences.Editor editor = this._context.getSharedPreferences(PERF_NAME, 0).edit();
        editor.putBoolean(PROPERTIES_GETSURE_ON_BOARD, isEnable);
        editor.apply();
    }

    public boolean getPropertiesGetsureOnBoardEnable() {
        return this._context.getSharedPreferences(PERF_NAME, 0).getBoolean(PROPERTIES_GETSURE_ON_BOARD, true);
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
        boolean z = false;
        SharedPreferences.Editor editor = this._context.getSharedPreferences(PERF_NAME, 0).edit();
        if (!enable) {
            z = true;
        }
        editor.putBoolean(PROPERTIES_ANIMATION_DISABLE, z);
        editor.apply();
    }
    public boolean getPropertiesAnimationEnable() {
        return !this._context.getSharedPreferences(PERF_NAME, 0).getBoolean(PROPERTIES_ANIMATION_DISABLE, false);
    }

    public void setPropertiesArticleMoveDisable(boolean isDisable) {
        SharedPreferences.Editor editor = this._context.getSharedPreferences(PERF_NAME, 0).edit();
        editor.putBoolean(PROPERTIES_ARTICLE_MOVE_DISABLE, isDisable);
        editor.apply();
    }
    public boolean getPropertiesArticleMoveEnsable() {
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

    public int getSymbolSize() {
        return this._symbols.length;
    }

    public String getSymbol(int index) {
        return this._symbols[index];
    }

    public Typeface getTypeface() {
        return this._typeface;
    }

    private String getBlockListString() {
        return this._context.getSharedPreferences(PERF_NAME, 0).getString(PROPERTIES_BLOCK_LIST, "");
    }

    public Vector<String> getBlockList() {
        String block_string = getBlockListString();
        if (this._block_list == null) {
            this._block_list = new Vector<>();
            if (block_string.length() > 0) {
                for (String block_name : block_string.split(" *, *")) {
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

    public void removeBlockName(String aBlockName) {
        Vector<String> new_list = new Vector<>(getBlockList());
        for (int i = new_list.size(); i > 0; i--) {
            if (new_list.get(i - 1).equals(aBlockName)) {
                new_list.remove(i - 1);
            }
        }
        updateBlockList(new_list);
    }

    @SuppressLint({"DefaultLocale"})
    public boolean isBlockListContains(String aName) {
        getBlockListString();
        if (this._block_list_string_lower_cased == null) {
            this._block_list_string_lower_cased = getBlockListString().toLowerCase();
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

    public void setPropertiesKeepWifi(boolean keep) {
        boolean z = false;
        SharedPreferences.Editor editor = this._context.getSharedPreferences(PERF_NAME, 0).edit();
        if (!keep) {
            z = true;
        }
        editor.putBoolean(PROPERTIES_kEEP_WIFI_DISABLE, z);
        editor.apply();
    }
    public boolean getPropertiesKeepWifi() {
        return !this._context.getSharedPreferences(PERF_NAME, 0).getBoolean(PROPERTIES_kEEP_WIFI_DISABLE, false);
    }


    public void reloadWifiSetting() {
        if (getPropertiesKeepWifi()) {
            ASNavigationController.getCurrentController().getDeviceController().lockWifi();
        } else {
            ASNavigationController.getCurrentController().getDeviceController().unlockWifi();
        }
    }

    public void releaseWifiSetting() {
        ASNavigationController.getCurrentController().getDeviceController().unlockWifi();
    }

    public void reloadAnimationSetting() {
        ASNavigationController.getCurrentController().setAnimationEnable(getPropertiesAnimationEnable());
    }

    public void setPropertiesLastConnectionIsOfflineByUser(boolean isOfflineByUser) {
        boolean z = false;
        SharedPreferences.Editor editor = this._context.getSharedPreferences(PERF_NAME, 0).edit();
        if (!isOfflineByUser) {
            z = true;
        }
        editor.putBoolean(PROPERTIES_LAST_CONNECTION_IS_NOT_OFFLINE_BY_USER, z);
        editor.apply();
    }

    public boolean getPropertiesLastConnectionIsOfflineByUser() {
        return !this._context.getSharedPreferences(PERF_NAME, 0).getBoolean(PROPERTIES_LAST_CONNECTION_IS_NOT_OFFLINE_BY_USER, false);
    }
}
