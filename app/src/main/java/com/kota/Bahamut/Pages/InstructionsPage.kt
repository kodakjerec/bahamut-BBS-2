package com.kota.Bahamut.Pages

import android.annotation.SuppressLint
import android.webkit.WebView
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.R
import com.kota.TelnetUI.TelnetPage

class InstructionsPage : TelnetPage() {
    override fun getPageType(): Int {
        return BahamutPage.BAHAMUT_INSTRUCTIONS
    }

    override fun getPageLayout(): Int {
        return R.layout.instruction_page
    }

    // com.kota.TelnetUI.TelnetPage
    override fun isPopupPage(): Boolean {
        return true
    }

    // com.kota.TelnetUI.TelnetPage
    override fun isKeepOnOffline(): Boolean {
        return true
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onPageDidLoad() {
        val webView = findViewById(R.id.instruction_web_view) as WebView
//        webView.settings.javaScriptEnabled = true
        webView.loadUrl("https://github.com/kodakjerec/bahamut-BBS-2/blob/main/book/outline.md")
    }
}
