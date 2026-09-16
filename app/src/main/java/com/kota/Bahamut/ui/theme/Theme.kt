package com.kota.Bahamut.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 主題風格列舉
 */
enum class AppThemeStyle {
    DEFAULT, // 預設主題
    PINK,    // 粉紅主題
    EINK     // eInk 電子紙主題
}

/**
 * 語意化色彩規格
 */
@Immutable
data class AppColors(
    // 背景與介面層級
    val pageBackground: Color,
    val surface: Color,
    val dialogTitleBackground: Color,
    val dialogBlockBackground: Color,
    val dialogSelectArticleFocused: Color,

    // 文字
    val textPrimary: Color,
    val textSecondary: Color,
    val textLink: Color,
    val titleBarTitle: Color,
    val titleBarDetail: Color,
    val titleBarDetail2: Color,
    val buttonText: Color,
    val buttonTextPressed: Color,
    val buttonTextDisabled: Color,
    val buttonTextDanger: Color,
    val buttonTextDangerPressed: Color,
    val inputBoxText: Color,
    val inputBoxBackground: Color,

    // 工具列與按鈕
    val toolbarBackground: Color,
    val toolbarBackgroundPressed: Color,
    val toolbarBackgroundFocused: Color,
    val toolbarBackgroundDisabled: Color,
    val toolbarDivider: Color,
    val buttonDangerBackground: Color,
    val buttonDangerPressed: Color,
    val buttonDangerDisabled: Color,

    // 對話框按鈕
    val dialogButtonBackground: Color,
    val dialogButtonBackgroundPressed: Color,
    val dialogButtonText: Color,
    val dialogButtonDivider: Color,

    // 標籤頁 (Tab)
    val tabSelectedBackground: Color,
    val tabSelectedText: Color,
    val tabUnselectedBackground: Color,
    val tabUnselectedText: Color,

    // 邊框與分隔線
    val divider: Color,
    val dialogBorder: Color,
    val checkboxTint: Color,
    val checkboxCheckmark: Color,
    val checkboxUncheckedTint: Color,

    // 章節與分類標題、警示與通知
    val chapterBackground: Color,
    val chapterText: Color,
    val statusNotice: Color,

    // BBS 專用色彩 (內文、作者、看板列表)
    val bbsAuthor0: Color,
    val bbsContent0: Color,
    val bbsAuthor1: Color,
    val bbsContent1: Color,
    val bbsBoardNormal: Color,
    val bbsBoardNormalRead: Color,
    val bbsBoardFollowFirst: Color,
    val bbsBoardFollowFirstRead: Color,
    val bbsBoardFollowOther: Color,
    val bbsBoardFollowOtherRead: Color,
    val bbsMailStatus: Color,
    val bbsMailMark: Color,
    val bbsMailReply: Color,
    val bbsMailDate: Color,
    val bbsMailAuthor: Color,
    val bbsMailNumber: Color,
    val bbsBoardGy: Color
)

/**
 * 預設淺色配置
 */
private val DefaultLightColors = AppColors(
    pageBackground = AppPalette.Black,
    surface = AppPalette.DarkGray20,
    dialogTitleBackground = AppPalette.DarkGray20,
    dialogBlockBackground = AppPalette.DarkGray20,
    dialogSelectArticleFocused = AppPalette.DarkGray60,

    textPrimary = AppPalette.White,
    textSecondary = AppPalette.HalfWhite,
    textLink = AppPalette.LinkCyan,
    titleBarTitle = AppPalette.Yellow,
    titleBarDetail = AppPalette.White,
    titleBarDetail2 = Color(0xFFC0FFFF),
    buttonText = AppPalette.White,
    buttonTextPressed = AppPalette.Black,
    buttonTextDisabled = Color(0xFF608060),
    buttonTextDanger = AppPalette.White,
    buttonTextDangerPressed = AppPalette.Black,
    inputBoxText = AppPalette.Black,
    inputBoxBackground = Color(0xFFE0E0E0),

    toolbarBackground = AppPalette.DefaultToolbarNormal,
    toolbarBackgroundPressed = AppPalette.DefaultToolbarPressed,
    toolbarBackgroundFocused = AppPalette.DefaultToolbarFocused,
    toolbarBackgroundDisabled = AppPalette.DefaultToolbarDisabled,
    toolbarDivider = AppPalette.ToolbarDivider,
    buttonDangerBackground = AppPalette.Red,
    buttonDangerPressed = AppPalette.RedBright,
    buttonDangerDisabled = Color(0xFF400000),

    dialogButtonBackground = AppPalette.DialogItemNormal,
    dialogButtonBackgroundPressed = AppPalette.DialogItemPressed,
    dialogButtonText = AppPalette.White,
    dialogButtonDivider = AppPalette.ToolbarDivider,

    tabSelectedBackground = AppPalette.DefaultToolbarNormal,
    tabSelectedText = AppPalette.White,
    tabUnselectedBackground = AppPalette.Transparent,
    tabUnselectedText = AppPalette.HalfWhite,

    divider = AppPalette.Divider,
    dialogBorder = AppPalette.HalfWhite,
    checkboxTint = AppPalette.HoloGreen,
    checkboxCheckmark = AppPalette.Black,
    checkboxUncheckedTint = AppPalette.HalfWhite,

    chapterBackground = AppPalette.DarkGray20,
    chapterText = AppPalette.GrayWhite,
    statusNotice = AppPalette.Red,

    bbsAuthor0 = AppPalette.BbsArticleAuthor0,
    bbsContent0 = AppPalette.BbsArticleContent0,
    bbsAuthor1 = AppPalette.BbsArticleAuthor1,
    bbsContent1 = AppPalette.BbsArticleContent1,
    bbsBoardNormal = AppPalette.White,
    bbsBoardNormalRead = AppPalette.HalfWhite,
    bbsBoardFollowFirst = AppPalette.BbsBoardFollowFirst,
    bbsBoardFollowFirstRead = AppPalette.BbsBoardFollowFirstRead,
    bbsBoardFollowOther = AppPalette.BbsBoardFollowOther,
    bbsBoardFollowOtherRead = AppPalette.BbsBoardFollowOtherRead,
    bbsMailStatus = AppPalette.BbsMailStatus,
    bbsMailMark = AppPalette.BbsMailMark,
    bbsMailReply = AppPalette.BbsMailReply,
    bbsMailDate = AppPalette.BbsMailDate,
    bbsMailAuthor = AppPalette.BbsMailAuthor,
    bbsMailNumber = AppPalette.BbsMailNumber,
    bbsBoardGy = AppPalette.BbsBoardGy
)

/**
 * 預設深色配置
 */
private val DefaultDarkColors = AppColors(
    pageBackground = AppPalette.DarkGray10,
    surface = AppPalette.DarkGray20,
    dialogTitleBackground = AppPalette.DarkGray20,
    dialogBlockBackground = AppPalette.DarkGray20,
    dialogSelectArticleFocused = AppPalette.DarkGray60,

    textPrimary = AppPalette.DarkTextWhite,
    textSecondary = AppPalette.HalfWhite,
    textLink = AppPalette.LinkCyan,
    titleBarTitle = AppPalette.Yellow,
    titleBarDetail = AppPalette.DarkTextWhite,
    titleBarDetail2 = Color(0xFFC0FFFF),
    buttonText = AppPalette.DarkTextWhite,
    buttonTextPressed = AppPalette.White,
    buttonTextDisabled = Color(0xFF507050),
    buttonTextDanger = AppPalette.DarkTextWhite,
    buttonTextDangerPressed = AppPalette.White,
    inputBoxText = AppPalette.Black,
    inputBoxBackground = Color(0xFFE0E0E0),

    toolbarBackground = AppPalette.DefaultDarkToolbarNormal,
    toolbarBackgroundPressed = AppPalette.DefaultDarkToolbarPressed,
    toolbarBackgroundFocused = AppPalette.DefaultDarkToolbarFocused,
    toolbarBackgroundDisabled = AppPalette.DefaultDarkToolbarDisabled,
    toolbarDivider = AppPalette.DefaultDarkToolbarDisabled,
    buttonDangerBackground = AppPalette.DefaultDarkDangerBackground,
    buttonDangerPressed = AppPalette.DefaultDarkDangerPressed,
    buttonDangerDisabled = Color(0xFF2A0A0A),

    dialogButtonBackground = AppPalette.DialogItemNormal,
    dialogButtonBackgroundPressed = AppPalette.DialogItemPressed,
    dialogButtonText = AppPalette.DarkTextWhite,
    dialogButtonDivider = AppPalette.DefaultDarkToolbarDisabled,

    tabSelectedBackground = AppPalette.DefaultDarkToolbarNormal,
    tabSelectedText = AppPalette.DarkTextWhite,
    tabUnselectedBackground = AppPalette.Transparent,
    tabUnselectedText = AppPalette.HalfWhite,

    divider = AppPalette.Divider,
    dialogBorder = AppPalette.HalfWhite,
    checkboxTint = AppPalette.HoloGreen,
    checkboxCheckmark = AppPalette.Black,
    checkboxUncheckedTint = AppPalette.HalfWhite,

    chapterBackground = AppPalette.DarkGray20,
    chapterText = AppPalette.GrayWhite,
    statusNotice = Color(0xFFC04040),

    bbsAuthor0 = AppPalette.DarkTextWhite,
    bbsContent0 = AppPalette.GrayWhite,
    bbsAuthor1 = AppPalette.BbsDarkArticleAuthor1,
    bbsContent1 = AppPalette.BbsDarkArticleContent1,
    bbsBoardNormal = AppPalette.GrayWhite,
    bbsBoardNormalRead = AppPalette.HalfWhite,
    bbsBoardFollowFirst = AppPalette.BbsDarkBoardFollowFirst,
    bbsBoardFollowFirstRead = AppPalette.BbsDarkBoardFollowFirstRead,
    bbsBoardFollowOther = AppPalette.BbsDarkBoardFollowOther,
    bbsBoardFollowOtherRead = AppPalette.BbsDarkBoardFollowOtherRead,
    bbsMailStatus = AppPalette.BbsDarkMailStatus,
    bbsMailMark = AppPalette.BbsDarkMailMark,
    bbsMailReply = AppPalette.BbsMailReply,
    bbsMailDate = AppPalette.BbsDarkMailDate,
    bbsMailAuthor = AppPalette.BbsDarkMailAuthor,
    bbsMailNumber = AppPalette.BbsDarkMailNumber,
    bbsBoardGy = AppPalette.BbsDarkBoardGy
)

/**
 * 粉紅淺色配置
 */
private val PinkLightColors = DefaultLightColors.copy(
    toolbarBackground = AppPalette.PinkButtonNormal,
    toolbarBackgroundPressed = AppPalette.PinkButtonPressed,
    toolbarBackgroundFocused = AppPalette.PinkButtonPressed,
    toolbarBackgroundDisabled = AppPalette.PinkButtonDisabled,
    toolbarDivider = Color(0xFF400A24),
    tabSelectedBackground = AppPalette.PinkButtonNormal,
    tabSelectedText = AppPalette.White,
    tabUnselectedBackground = AppPalette.Transparent,
    tabUnselectedText = AppPalette.HalfWhite,
    chapterBackground = AppPalette.PinkHeaderBackground,
    chapterText = AppPalette.PinkHeaderText,
    statusNotice = AppPalette.PinkHeaderText
)

/**
 * 粉紅深色配置
 */
private val PinkDarkColors = DefaultDarkColors.copy(
    toolbarBackground = AppPalette.PinkDarkButtonNormal,
    toolbarBackgroundPressed = AppPalette.PinkDarkButtonPressed,
    toolbarBackgroundFocused = AppPalette.PinkDarkButtonPressed,
    toolbarBackgroundDisabled = AppPalette.PinkDarkButtonDisabled,
    toolbarDivider = Color(0xFF300A24),
    buttonTextDisabled = Color(0xFF805068),
    tabSelectedBackground = AppPalette.PinkDarkButtonNormal,
    tabSelectedText = AppPalette.White,
    tabUnselectedBackground = AppPalette.Transparent,
    tabUnselectedText = AppPalette.HalfWhite,
    chapterBackground = AppPalette.PinkDarkHeaderBackground,
    chapterText = AppPalette.PinkDarkHeaderText,
    statusNotice = AppPalette.PinkDarkHeaderText
)

/**
 * eInk 淺色配置 (高對比白底黑字)
 */
private val EInkLightColors = AppColors(
    pageBackground = AppPalette.White,
    surface = AppPalette.White,
    dialogTitleBackground = AppPalette.White,
    dialogBlockBackground = AppPalette.White,
    dialogSelectArticleFocused = AppPalette.GrayWhite,

    textPrimary = AppPalette.Black,
    textSecondary = AppPalette.HalfWhite,
    textLink = AppPalette.Black,
    titleBarTitle = AppPalette.Black,
    titleBarDetail = AppPalette.Black,
    titleBarDetail2 = AppPalette.HalfWhite,
    buttonText = AppPalette.EInkButtonTextNormal,
    buttonTextPressed = AppPalette.EInkButtonTextPressed,
    buttonTextDisabled = AppPalette.EInkButtonTextDisabled,
    buttonTextDanger = AppPalette.White,
    buttonTextDangerPressed = AppPalette.White,
    inputBoxText = AppPalette.Black,
    inputBoxBackground = AppPalette.White,

    toolbarBackground = AppPalette.EInkButtonNormal,
    toolbarBackgroundPressed = AppPalette.EInkButtonPressed,
    toolbarBackgroundFocused = AppPalette.EInkButtonPressed,
    toolbarBackgroundDisabled = AppPalette.EInkButtonDisabled,
    toolbarDivider = AppPalette.Black,
    buttonDangerBackground = AppPalette.EInkDangerNormal,
    buttonDangerPressed = AppPalette.EInkDangerPressed,
    buttonDangerDisabled = AppPalette.EInkButtonDisabled,

    dialogButtonBackground = AppPalette.Black,
    dialogButtonBackgroundPressed = AppPalette.DarkGray40,
    dialogButtonText = AppPalette.White,
    dialogButtonDivider = AppPalette.White,

    tabSelectedBackground = AppPalette.Black,
    tabSelectedText = AppPalette.White,
    tabUnselectedBackground = AppPalette.White,
    tabUnselectedText = AppPalette.Black,

    divider = AppPalette.Black,
    dialogBorder = AppPalette.Black,
    checkboxTint = AppPalette.Black,
    checkboxCheckmark = AppPalette.White,
    checkboxUncheckedTint = AppPalette.Black,

    chapterBackground = AppPalette.GrayWhite,
    chapterText = AppPalette.Black,
    statusNotice = AppPalette.Black,

    bbsAuthor0 = AppPalette.Black,
    bbsContent0 = AppPalette.Black,
    bbsAuthor1 = AppPalette.HalfWhite,
    bbsContent1 = AppPalette.HalfWhite,
    bbsBoardNormal = AppPalette.Black,
    bbsBoardNormalRead = AppPalette.HalfWhite,
    bbsBoardFollowFirst = AppPalette.Black,
    bbsBoardFollowFirstRead = AppPalette.HalfWhite,
    bbsBoardFollowOther = AppPalette.Black,
    bbsBoardFollowOtherRead = AppPalette.HalfWhite,
    bbsMailStatus = AppPalette.Black,
    bbsMailMark = AppPalette.Black,
    bbsMailReply = AppPalette.Black,
    bbsMailDate = AppPalette.HalfWhite,
    bbsMailAuthor = AppPalette.HalfWhite,
    bbsMailNumber = AppPalette.HalfWhite,
    bbsBoardGy = AppPalette.HalfWhite
)

/**
 * eInk 深色配置 (黑白對調高對比)
 */
private val EInkDarkColors = AppColors(
    pageBackground = AppPalette.Black,
    surface = AppPalette.Black,
    dialogTitleBackground = AppPalette.Black,
    dialogBlockBackground = AppPalette.Black,
    dialogSelectArticleFocused = AppPalette.DarkGray40,

    textPrimary = AppPalette.White,
    textSecondary = AppPalette.HalfWhite,
    textLink = AppPalette.White,
    titleBarTitle = AppPalette.White,
    titleBarDetail = AppPalette.White,
    titleBarDetail2 = AppPalette.HalfWhite,
    buttonText = AppPalette.EInkDarkButtonTextNormal,
    buttonTextPressed = AppPalette.EInkDarkButtonTextPressed,
    buttonTextDisabled = AppPalette.EInkButtonTextDisabled,
    buttonTextDanger = AppPalette.Black,
    buttonTextDangerPressed = AppPalette.Black,
    inputBoxText = AppPalette.White,
    inputBoxBackground = AppPalette.Black,

    toolbarBackground = AppPalette.EInkDarkButtonNormal,
    toolbarBackgroundPressed = AppPalette.EInkDarkButtonPressed,
    toolbarBackgroundFocused = AppPalette.EInkDarkButtonPressed,
    toolbarBackgroundDisabled = AppPalette.EInkDarkButtonDisabled,
    toolbarDivider = AppPalette.White,
    buttonDangerBackground = AppPalette.EInkDarkDangerNormal,
    buttonDangerPressed = AppPalette.EInkDarkDangerPressed,
    buttonDangerDisabled = AppPalette.EInkDarkButtonDisabled,

    dialogButtonBackground = AppPalette.White,
    dialogButtonBackgroundPressed = AppPalette.GrayWhite,
    dialogButtonText = AppPalette.Black,
    dialogButtonDivider = AppPalette.Black,

    tabSelectedBackground = AppPalette.White,
    tabSelectedText = AppPalette.Black,
    tabUnselectedBackground = AppPalette.Black,
    tabUnselectedText = AppPalette.White,

    divider = AppPalette.White,
    dialogBorder = AppPalette.White,
    checkboxTint = AppPalette.White,
    checkboxCheckmark = AppPalette.Black,
    checkboxUncheckedTint = AppPalette.White,

    chapterBackground = AppPalette.DarkGray40,
    chapterText = AppPalette.White,
    statusNotice = AppPalette.White,

    bbsAuthor0 = AppPalette.White,
    bbsContent0 = AppPalette.White,
    bbsAuthor1 = AppPalette.HalfWhite,
    bbsContent1 = AppPalette.HalfWhite,
    bbsBoardNormal = AppPalette.White,
    bbsBoardNormalRead = AppPalette.HalfWhite,
    bbsBoardFollowFirst = AppPalette.White,
    bbsBoardFollowFirstRead = AppPalette.HalfWhite,
    bbsBoardFollowOther = AppPalette.White,
    bbsBoardFollowOtherRead = AppPalette.HalfWhite,
    bbsMailStatus = AppPalette.White,
    bbsMailMark = AppPalette.White,
    bbsMailReply = AppPalette.White,
    bbsMailDate = AppPalette.HalfWhite,
    bbsMailAuthor = AppPalette.HalfWhite,
    bbsMailNumber = AppPalette.HalfWhite,
    bbsBoardGy = AppPalette.HalfWhite
)

/**
 * 色彩對應工廠函式 (共 6 組配色組合)
 */
fun createAppColors(style: AppThemeStyle, darkTheme: Boolean): AppColors {
    return when (style) {
        AppThemeStyle.DEFAULT -> if (darkTheme) DefaultDarkColors else DefaultLightColors
        AppThemeStyle.PINK -> if (darkTheme) PinkDarkColors else PinkLightColors
        AppThemeStyle.EINK -> if (darkTheme) EInkDarkColors else EInkLightColors
    }
}

val LocalAppColors = staticCompositionLocalOf { DefaultLightColors }

/**
 * 全域 Theme Composable
 *
 * 特別限制：
 * Android 10 (API 29) 以下的系統深色模式效果不佳，
 * 因此若 SDK < 29，一律強制 darkTheme 為 false (套用日間淺色主題)。
 */
@Composable
fun AppTheme(
    style: AppThemeStyle = AppThemeStyle.DEFAULT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Android 10 以下放棄深色模式
    val effectiveDarkTheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) darkTheme else false
    val colors = remember(style, effectiveDarkTheme) {
        createAppColors(style, effectiveDarkTheme)
    }

    CompositionLocalProvider(
        LocalAppColors provides colors,
        content = content
    )
}

/**
 * 導出方便存取的全域物件 AppTheme.colors
 */
object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}
