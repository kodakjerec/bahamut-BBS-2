package com.kota.Bahamut.dialogs.uploadImgMethod

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class UploaderPostimageorg(private val context: Context) {

    @SuppressLint("SetJavaScriptEnabled")
    suspend fun postImage(source: Uri): String = suspendCancellableCoroutine { cont ->
        // 建立 Dialog 來顯示 WebView
        val dialog = android.app.Dialog(context)
        val webView = WebView(context)
        webView.settings.javaScriptEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.domStorageEnabled = true
        dialog.setContentView(webView)
        dialog.setCancelable(true)
        dialog.show()

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                // 設定 select#expire 為 value=7
                view?.evaluateJavascript(
                    """
                        (function() {
                            var sel = document.getElementById('expire');
                            if (sel) {
                                sel.value = '7';
                                var evt = document.createEvent('HTMLEvents');
                                evt.initEvent('change', true, false);
                                sel.dispatchEvent(evt);
                                return 'expire set';
                            } else {
                                return 'expire not found';
                            }
                        })();
                        """.trimIndent()
                , null)

                // 監聽 code_html 產生，取得網址
                view?.evaluateJavascript(
                    "(function() { var el = document.getElementById('code_html'); return el ? el.value : ''; })();"
                ) { value ->
                    val cleanValue = value?.replace("\"", "") ?: ""
                    if (cleanValue.isNotEmpty() && cleanValue.startsWith("https://postimg.cc")) {
                        dialog.dismiss()
                        cont.resume(cleanValue)
                    }
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                view: WebView?,
                filePathCallback: android.webkit.ValueCallback<Array<Uri>>?,
                fileChooserParams: android.webkit.WebChromeClient.FileChooserParams?
            ): Boolean {
                // 自動選擇 source Uri
                filePathCallback?.onReceiveValue(arrayOf(source))
                return true
            }
        }

        webView.loadUrl("https://postimages.org/")
    }
}