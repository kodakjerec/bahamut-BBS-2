package com.kota.Bahamut.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.kota.Bahamut.pages.theme.ThemeStore
import com.kota.Bahamut.service.UserSettings
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * View 與 Compose 的主題橋接中心 (Theme Bridge)
 * 負責集中管理與響應目前選取的主題風格與深色模式狀態。
 */
object ThemeBridge {
    private val _themeStyle = MutableStateFlow(AppThemeStyle.DEFAULT)
    val themeStyle: StateFlow<AppThemeStyle> = _themeStyle.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    /**
     * 從既有 ThemeStore 與系統設定同步當前主題與深淺色狀態
     */
    fun syncFromSystem(context: Context) {
        val index = ThemeStore.getSelectIndex()
        val style = when (index) {
            1 -> AppThemeStyle.PINK
            2 -> AppThemeStyle.EINK
            else -> AppThemeStyle.DEFAULT
        }
        _themeStyle.value = style

        // Android 10 (API 29) 以下放棄深色模式，一律套用淺色
        val isDark = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            false
        } else {
            UserSettings.propertiesFollowSystemDarkMode && ThemeStore.isSystemDarkMode(context)
        }
        _isDarkTheme.value = isDark
    }

    /**
     * 更新主題風格並同步儲存至 ThemeStore
     */
    fun setThemeStyle(style: AppThemeStyle, context: Context? = null) {
        _themeStyle.value = style
        val index = when (style) {
            AppThemeStyle.DEFAULT -> 0
            AppThemeStyle.PINK -> 1
            AppThemeStyle.EINK -> 2
        }
        ThemeStore.setSelectIndex(index)
        if (context != null) {
            syncFromSystem(context)
        }
    }
}

/**
 * 專案全域 Compose 主題包裝容器
 * 自動監聽 ThemeBridge 狀態變化，無論系統深淺切換或主題變更均即時重組
 */
@Composable
fun BahamutAppTheme(content: @Composable () -> Unit) {
    val style by ThemeBridge.themeStyle.collectAsState()
    val isDark by ThemeBridge.isDarkTheme.collectAsState()

    AppTheme(
        style = style,
        darkTheme = isDark,
        content = content
    )
}

/**
 * ComposeView 便捷擴充函式
 * 自動設定生命週期釋放策略並注入 BahamutAppTheme
 */
fun ComposeView.setBahamutContent(content: @Composable () -> Unit) {
    // 確保 ViewTreeLifecycleOwner 與 ViewTreeSavedStateRegistryOwner 存在
    if (findViewTreeLifecycleOwner() == null) {
        val owner = (context as? LifecycleOwner)
        if (owner != null) {
            setViewTreeLifecycleOwner(owner)
        }
    }
    if (findViewTreeSavedStateRegistryOwner() == null) {
        val owner = (context as? SavedStateRegistryOwner)
        if (owner != null) {
            setViewTreeSavedStateRegistryOwner(owner)
        }
    }
    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
    setContent {
        BahamutAppTheme(content = content)
    }
}

