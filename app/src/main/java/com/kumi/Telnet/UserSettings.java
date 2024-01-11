package com.kumi.Telnet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import com.kumi.ASFramework.PageController.ASNavigationController;
import java.io.File;
import java.util.Iterator;
import java.util.Vector;

public class UserSettings {
  public static final int ARTICLE_VIEW_MODE_TELNET = 1;
  
  public static final int ARTICLE_VIEW_MODE_TEXT = 0;
  
  public static final String PERF_NAME = "user_setting";
  
  public static final String PROPERTIES_ANIMATION_DISABLE = "AnimationDisable";
  
  public static final String PROPERTIES_ARTICLE_MOVE_DISABLE = "ArticleModeDisable";
  
  public static final String PROPERTIES_ARTICLE_VIEW_MODE = "ArticleViewMode";
  
  public static final String PROPERTIES_BLOCK_LIST = "BlockList";
  
  public static final String PROPERTIES_BLOCK_LIST_ENABLE = "BlockListEnable";
  
  public static final String PROPERTIES_EXTRA_TOOLBAR_ENABLE = "ExtraToolbarEnable";
  
  public static final String PROPERTIES_KEEP_POWER = "KeepPower";
  
  public static final String PROPERTIES_KEEP_WIFI_DISABLE = "KeepWifiDisable";
  
  public static final String PROPERTIES_LAST_CONNECTION_IS_NOT_OFFLINE_BY_USER = "LastConnectionIsNotOfflineByUser";
  
  public static final String PROPERTIES_PASSWORD = "Password";
  
  public static final String PROPERTIES_SAVE_LOGON_USER = "SaveLogonUser";
  
  public static final String PROPERTIES_USERNAME = "Username";
  
  private Vector<String> _block_list = null;
  
  private String _block_list_string_lower_cased = null;
  
  Context _context;
  
  private String[] _headers = new String[] { 
      "不加 ▼", "[問題]", "[情報]", "[心得]", "[討論]", "[攻略]", "[秘技]", "[閒聊]", "[程設]", "[職場]", 
      "[推廣]", "[手機]", "[平板]", "[新番]", "[電影]", "[新聞]", "[其它]" };
  
  private String[] _symbols = new String[] { 
      "( >_0)b", "( ;-w-)a", "( -3-)y-~", "ˋ(°▽ ° )ノˋ( ° ▽° )ノ", "#/-_-)/~╨──╨", "(||￣▽￣)a", "o( -_-)=0))-3-)/", "(#‵′)o", "O(‵皿′)o", "( T_T)", 
      "(o_O )", "_ψ(._. )", "v(￣︶￣)y", "ㄟ(￣▽￣ㄟ)...", "(っ´▽`)っ", "m(_ _)m", "ˋ(°ω ° )ノ", "◢▆▅▄▃崩╰(〒皿〒)╯潰▃▄▅▇◣", "( O口O)!?", "☆━━━(ﾟ∀ﾟ)━━━" };
  
  public Typeface _typeface = null;
  
  private boolean _updated = false;
  
  public UserSettings(Context paramContext) {
    this._context = paramContext;
    upgrade();
  }
  
  private String getBlockListString() {
    return this._context.getSharedPreferences("user_setting", 0).getString("BlockList", "");
  }
  
  public void addBlockName(String paramString) {
    Vector<String> vector = new Vector();
    vector.addAll(getBlockList());
    int i = paramString.hashCode();
    boolean bool = false;
    byte b = 0;
    while (true) {
      boolean bool1 = bool;
      if (b < vector.size())
        if (((String)vector.get(b)).hashCode() > i) {
          vector.insertElementAt(paramString, b);
          bool1 = true;
        } else {
          b++;
          continue;
        }  
      if (!bool1)
        vector.add(paramString); 
      updateBlockList(vector);
      return;
    } 
  }
  
  public void exchangeArticleViewMode() {
    setArticleViewState(1 - getArticleViewMode());
  }
  
  public String getArticleHeader(int paramInt) {
    String str2 = "";
    String str1 = str2;
    if (paramInt > 0) {
      str1 = str2;
      if (paramInt < this._headers.length)
        str1 = this._headers[paramInt]; 
    } 
    return str1;
  }
  
  public String[] getArticleHeaders() {
    return this._headers;
  }
  
  public int getArticleViewMode() {
    return this._context.getSharedPreferences("user_setting", 0).getInt("ArticleViewMode", 0);
  }
  
  public Vector<String> getBlockList() {
    String str = getBlockListString();
    if (this._block_list == null) {
      this._block_list = new Vector<String>();
      if (str != null && str.length() > 0)
        for (String str1 : str.split(" *, *")) {
          if (str1.length() > 0)
            this._block_list.add(str1); 
        }  
    } 
    return this._block_list;
  }
  
  @SuppressLint({"DefaultLocale"})
  public String getBlockListLowCasedString() {
    if (this._block_list_string_lower_cased == null)
      this._block_list_string_lower_cased = getBlockListString().toLowerCase(); 
    return this._block_list_string_lower_cased;
  }
  
  public int getIndexOfHeader(String paramString) {
    if (paramString == null || paramString.length() == 0)
      return 0; 
    byte b1 = -1;
    byte b = 1;
    while (true) {
      byte b2 = b1;
      if (b < this._headers.length) {
        if (this._headers[b].equals(paramString))
          return b; 
        b++;
        continue;
      } 
      return b2;
    } 
  }
  
  public String getPassword() {
    return this._context.getSharedPreferences("user_setting", 0).getString("Password", "");
  }
  
  public String getSymbol(int paramInt) {
    return this._symbols[paramInt];
  }
  
  public int getSymbolSize() {
    return this._symbols.length;
  }
  
  public String[] getSymbols() {
    return this._symbols;
  }
  
  public Typeface getTypeface() {
    return this._typeface;
  }
  
  public String getUsername() {
    return this._context.getSharedPreferences("user_setting", 0).getString("Username", "");
  }
  
  public boolean isAnimationEnable() {
    boolean bool = false;
    if (!this._context.getSharedPreferences("user_setting", 0).getBoolean("AnimationDisable", false))
      bool = true; 
    return bool;
  }
  
  public boolean isArticleMoveDisable() {
    return this._context.getSharedPreferences("user_setting", 0).getBoolean("ArticleModeDisable", false);
  }
  
  @SuppressLint({"DefaultLocale"})
  public boolean isBlockListContains(String paramString) {
    if (getBlockListString() == null)
      return false; 
    if (this._block_list_string_lower_cased == null)
      this._block_list_string_lower_cased = getBlockListString().toLowerCase(); 
    return this._block_list_string_lower_cased.contains("," + paramString.toLowerCase() + ",");
  }
  
  public boolean isBlockListEnable() {
    return this._context.getSharedPreferences("user_setting", 0).getBoolean("BlockListEnable", false);
  }
  
  public boolean isExternalToolbarEnable() {
    return this._context.getSharedPreferences("user_setting", 0).getBoolean("ExtraToolbarEnable", false);
  }
  
  public boolean isKeepWifi() {
    boolean bool = false;
    if (!this._context.getSharedPreferences("user_setting", 0).getBoolean("KeepWifiDisable", false))
      bool = true; 
    return bool;
  }
  
  public boolean isLastConnectionIsOfflineByUser() {
    boolean bool = false;
    if (!this._context.getSharedPreferences("user_setting", 0).getBoolean("LastConnectionIsNotOfflineByUser", false))
      bool = true; 
    return bool;
  }
  
  public boolean isSaveLogonUser() {
    return this._context.getSharedPreferences("user_setting", 0).getBoolean("SaveLogonUser", false);
  }
  
  public void notifyDataUpdated() {
    this._updated = true;
  }
  
  public void releaseWifiSetting() {
    ASNavigationController.getCurrentController().getDeviceController().unlockWifi();
  }
  
  public void reloadAnimationSetting() {
    ASNavigationController.getCurrentController().setAnimationEnable(isAnimationEnable());
  }
  
  public void reloadWifiSetting() {
    if (isKeepWifi()) {
      ASNavigationController.getCurrentController().getDeviceController().lockWifi();
      return;
    } 
    ASNavigationController.getCurrentController().getDeviceController().unlockWifi();
  }
  
  public void removeBlockName(String paramString) {
    Vector<String> vector = new Vector();
    vector.addAll(getBlockList());
    for (int i = vector.size(); i > 0; i--) {
      if (((String)vector.get(i - 1)).equals(paramString))
        vector.remove(i - 1); 
    } 
    updateBlockList(vector);
  }
  
  public void setAnimationEnable(boolean paramBoolean) {
    boolean bool = false;
    SharedPreferences.Editor editor = this._context.getSharedPreferences("user_setting", 0).edit();
    if (!paramBoolean)
      bool = true; 
    editor.putBoolean("AnimationDisable", bool);
    editor.commit();
  }
  
  public void setArticleMoveDisable(boolean paramBoolean) {
    SharedPreferences.Editor editor = this._context.getSharedPreferences("user_setting", 0).edit();
    editor.putBoolean("ArticleModeDisable", paramBoolean);
    editor.commit();
  }
  
  public void setArticleViewState(int paramInt) {
    SharedPreferences.Editor editor = this._context.getSharedPreferences("user_setting", 0).edit();
    editor.putInt("ArticleViewMode", paramInt);
    editor.commit();
  }
  
  public void setBlockListEnable(boolean paramBoolean) {
    SharedPreferences.Editor editor = this._context.getSharedPreferences("user_setting", 0).edit();
    editor.putBoolean("BlockListEnable", paramBoolean);
    editor.commit();
  }
  
  public void setExternalToolbarEnable(boolean paramBoolean) {
    SharedPreferences.Editor editor = this._context.getSharedPreferences("user_setting", 0).edit();
    editor.putBoolean("ExtraToolbarEnable", paramBoolean);
    editor.commit();
  }
  
  public void setKeepWifi(boolean paramBoolean) {
    boolean bool = false;
    SharedPreferences.Editor editor = this._context.getSharedPreferences("user_setting", 0).edit();
    if (!paramBoolean)
      bool = true; 
    editor.putBoolean("KeepWifiDisable", bool);
    editor.commit();
  }
  
  public void setLastConnectionIsOfflineByUser(boolean paramBoolean) {
    boolean bool = false;
    SharedPreferences.Editor editor = this._context.getSharedPreferences("user_setting", 0).edit();
    if (!paramBoolean)
      bool = true; 
    editor.putBoolean("LastConnectionIsNotOfflineByUser", bool);
    editor.commit();
  }
  
  public void setPassword(String paramString) {
    SharedPreferences.Editor editor = this._context.getSharedPreferences("user_setting", 0).edit();
    editor.putString("Password", paramString);
    editor.commit();
  }
  
  public void setSaveLogonUser(boolean paramBoolean) {
    SharedPreferences.Editor editor = this._context.getSharedPreferences("user_setting", 0).edit();
    editor.putBoolean("SaveLogonUser", paramBoolean);
    editor.commit();
  }
  
  public void setUsername(String paramString) {
    SharedPreferences.Editor editor = this._context.getSharedPreferences("user_setting", 0).edit();
    editor.putString("Username", paramString);
    editor.commit();
  }
  
  public void updateBlockList(Vector<String> paramVector) {
    String str = ",";
    if (paramVector == null || paramVector.size() == 0) {
      str = "";
    } else {
      Iterator<String> iterator = paramVector.iterator();
      String str1 = str;
      while (true) {
        str = str1;
        if (iterator.hasNext()) {
          str = iterator.next();
          str1 = str1 + str.trim() + ",";
          continue;
        } 
        SharedPreferences.Editor editor1 = this._context.getSharedPreferences("user_setting", 0).edit();
        editor1.putString("BlockList", str);
        editor1.commit();
        this._block_list = null;
        this._block_list_string_lower_cased = null;
        return;
      } 
    } 
    SharedPreferences.Editor editor = this._context.getSharedPreferences("user_setting", 0).edit();
    editor.putString("BlockList", str);
    editor.commit();
    this._block_list = null;
    this._block_list_string_lower_cased = null;
  }
  
  void upgrade() {
    String str = this._context.getFilesDir().getPath() + "/default_login.properties";
    if ((new File(str)).exists()) {
      SharedPreferences sharedPreferences = this._context.getSharedPreferences("user_setting", 0);
      if (sharedPreferences.getInt("upgrade", 0) != 1) {
        PropertiesOperator propertiesOperator = new PropertiesOperator(str);
        propertiesOperator.load();
        String str2 = propertiesOperator.getPropertiesString("Username");
        String str1 = propertiesOperator.getPropertiesString("Password");
        boolean bool1 = propertiesOperator.getPropertiesBoolean("SaveLogonUser");
        int i = propertiesOperator.getPropertiesInteger("ArticleViewMode");
        str = propertiesOperator.getPropertiesString("BlockList");
        boolean bool2 = propertiesOperator.getPropertiesBoolean("BlockListEnable");
        boolean bool3 = propertiesOperator.getPropertiesBoolean("KeepWifiDisable");
        boolean bool4 = propertiesOperator.getPropertiesBoolean("KeepPower");
        boolean bool5 = propertiesOperator.getPropertiesBoolean("LastConnectionIsNotOfflineByUser");
        boolean bool6 = propertiesOperator.getPropertiesBoolean("AnimationDisable");
        boolean bool7 = propertiesOperator.getPropertiesBoolean("ExtraToolbarEnable");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Username", str2);
        editor.putString("Password", str1);
        editor.putBoolean("SaveLogonUser", bool1);
        editor.putInt("ArticleViewMode", i);
        editor.putString("BlockList", str);
        editor.putBoolean("BlockListEnable", bool2);
        editor.putBoolean("KeepWifiDisable", bool3);
        editor.putBoolean("KeepPower", bool4);
        editor.putBoolean("LastConnectionIsNotOfflineByUser", bool5);
        editor.putBoolean("AnimationDisable", bool6);
        editor.putBoolean("ExtraToolbarEnable", bool7);
        editor.putInt("upgrade", 1);
        editor.commit();
      } 
    } 
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\UserSettings.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */