package com.kota.asFramework.pageController

import android.content.Context
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import com.kota.Bahamut.ui.dialogs.BahaGlobalDialogHost
import com.kota.Bahamut.ui.theme.setBahamutContent

/**
 * 專案純 Jetpack Compose 頁面控制器基底
 */
abstract class ASComposeViewController : ASViewController() {
    override val pageLayout: Int
        get() = 0

    @Composable
    abstract fun ComposeContent()

    override fun createPageView(context: Context): View {
        return ComposeView(context).apply {
            setBahamutContent {
                ComposeContent()
                BahaGlobalDialogHost()
            }
        }
    }
}

