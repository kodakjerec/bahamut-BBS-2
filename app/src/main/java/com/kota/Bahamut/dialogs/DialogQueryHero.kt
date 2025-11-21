package com.kota.Bahamut.dialogs

import android.annotation.SuppressLint
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.dialog.ASDialog
import com.kota.Bahamut.R
import java.util.Vector


class DialogQueryHero : ASDialog() {
    private lateinit var id: String
    @SuppressLint("SetTextI18n")
    fun getData(fromStrings: Vector<String>) {
        try{
            val regex = """
                ^(?<id>\w+)\((?<nick>.*)\) HP： (?<hp>\d+) ，MP： (?<mp>\d+) ，(?<authType>\w+)：(?<authStatus>.*)
            """.trimIndent().toRegex()
            val regex2 = """
                ^上次\((?<lastDate>.*)日 (?<lastTime>.*)\)來自\((?<fromIp>.*)\)
            """.trimIndent().toRegex()

            val match = regex.find(fromStrings[2])!!
            id = match.groups[1]?.value!!
            val nick = match.groups[2]?.value
            val hp = match.groups[3]?.value
            val mp = match.groups[4]?.value
            val authStatus = match.groups[5]?.value ?: "未知"
            val authLevel = match.groups[6]?.value ?: "無"
            val match2 = regex2.find(fromStrings[3])!!

            val lastDate = match2.groups[1]?.value + "日"
            val lastTime = match2.groups[2]?.value
            val fromIp = match2.groups[3]?.value

            val contextView = findViewById<LinearLayout>(R.id.content_view)
            contextView.findViewById<TextView>(R.id.dialog_query_hero_id).text = id
            contextView.findViewById<TextView>(R.id.dialog_query_hero_nick).text = nick
            contextView.findViewById<TextView>(R.id.dialog_query_hero_hp).text = hp
            contextView.findViewById<TextView>(R.id.dialog_query_hero_mp).text = mp
            contextView.findViewById<TextView>(R.id.dialog_query_hero_auth1).text = authStatus
            contextView.findViewById<TextView>(R.id.dialog_query_hero_auth2).text = authLevel
            contextView.findViewById<TextView>(R.id.dialog_query_hero_last_date).text = lastDate
            contextView.findViewById<TextView>(R.id.dialog_query_hero_last_time).text = lastTime
            contextView.findViewById<TextView>(R.id.dialog_query_hero_from_ip).text = fromIp
        } catch(_: Exception) {
            dismiss()
            ASAlertDialog.createDialog()
                .setTitle("錯誤")
                .setMessage("取得勇者資料出錯")
                .addButton("確定")
                .setListener { aDialog: ASAlertDialog, _: Int -> aDialog.dismiss() }
                .show()
        }
    }

    override val name: String?
        get() = "BahamutQueryHeroDialog"

    /* 顯示WEB資訊 */
    @SuppressLint("SetJavaScriptEnabled")
    private val showWebViewListener: View.OnClickListener = View.OnClickListener { view ->
        val contextView = findViewById<LinearLayout>(R.id.content_view)
        val apiUrl = "https://m.gamer.com.tw/home/home.php?owner=$id"
        val webView = contextView.findViewById<WebView>(R.id.dialog_query_hero_web_view)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                view.evaluateJavascript("document.getElementsByClassName('download-app_box01')[0].style.display='none';", null)
                view.evaluateJavascript("document.getElementsByClassName('bh-banner')[0].style.display='none';", null)
                view.evaluateJavascript("document.getElementsByClassName('sidebar-navbar_rwd')[0].style.visibility='hidden';", null)
            }
        }

        webView.loadUrl(apiUrl)
        webView.visibility = View.VISIBLE
        view.visibility = View.GONE
    }

    init {
        requestWindowFeature(1)
        setContentView(R.layout.dialog_query_hero)
        window?.setBackgroundDrawable(null)

        val contextView = findViewById<LinearLayout>(R.id.content_view)
        contextView.findViewById<Button>(R.id.cancel).setOnClickListener { dismiss() }

        contextView.findViewById<Button>(R.id.dialog_query_hero_show_web_view).setOnClickListener(showWebViewListener)
    }
}
