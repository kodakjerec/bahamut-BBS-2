package com.kota.Bahamut.pages.login

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.kota.Bahamut.R
import com.kota.Bahamut.service.UserSettings
import com.kota.asFramework.thread.ASCoroutine

class LoginWeb(private val context: Context, private val externalWebView: WebView? = null) {
    
    private var currentWebView: WebView? = null
    private var onFailCallback: (() -> Unit)? = null
    private var onSignDetectedCallback: (() -> Unit)? = null
    private var onManualCallback: ((String) -> Unit)? = null

    @SuppressLint("SetJavaScriptEnabled")
    fun init(onSignDetected: (() -> Unit)? = null, onFail: (() -> Unit)? = null, onManual: ((String) -> Unit)? = null) {
        this.onSignDetectedCallback = onSignDetected
        this.onFailCallback = onFail
        this.onManualCallback = onManual
        
        // 設定 20 秒後自動清理
        setupTimeout()
        
        try {
            // 使用外部提供的 WebView 或創建新的 WebView
            val webView = externalWebView ?: WebView(context)
            currentWebView = webView

            // WebView設定
            val webSettings: WebSettings = webView.settings
            webSettings.javaScriptEnabled = true

            // 確保可以載入網路資源（包括JavaScript文件）
            webSettings.allowFileAccess = true
            webSettings.allowContentAccess = true
            
            // 允許混合內容（HTTPS頁面載入HTTP資源）
            webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

            // 禁用圖片載入（但保持JavaScript和其他資源載入）
//            webSettings.loadsImagesAutomatically = false
//            webSettings.blockNetworkImage = true
                        
            // 禁用影片和媒體自動播放
            webSettings.mediaPlaybackRequiresUserGesture = true

            // 禁用不必要的功能來提升載入速度
            webSettings.builtInZoomControls = false
            webSettings.displayZoomControls = false
            webSettings.domStorageEnabled = true  // 保留DOM存儲（登入可能需要）
            
            // 確保用戶代理字串完整，某些網站根據此判斷是否載入JavaScript
            webSettings.userAgentString = webSettings.userAgentString

            // 添加 JavaScript 接口
            webView.addJavascriptInterface(WebAppInterface(), "Android")

            // 設定WebViewClient
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)

                    // 先注入通用腳本（所有頁面都需要）
                    injectCommonScript(view)
                    
                    // 然後根據URL執行特定腳本
                    when {
                        url?.contains("login.php") == true -> {
                            // 在登入頁面，注入登入腳本
                            injectLoginScript(view)
                        }
                        url?.contains("gamer.com.tw") == true -> {
                            // 在其他巴哈頁面，檢查登入狀態
                            injectForumScript(view)
                        }
                    }
                }

                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    return false
                }
            }

            // 載入巴哈姆特網站
            webView.loadUrl("https://user.gamer.com.tw/login.php")
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Error initializing LoginWeb", e)
        }
    }
    
    // 注入通用腳本（所有頁面都需要的函數）
    private fun injectCommonScript(webView: WebView?) {
        val script = """
            javascript:(function() {
                
                // 等待元素出現的函數
                window.waitForElement = function(selector, callback, timeout = 10000) {
                    var startTime = Date.now();
                    function check() {
                        var element = document.querySelector(selector);
                        if (element) {
                            callback(element);
                        } else if (Date.now() - startTime < timeout) {
                            setTimeout(check, 500);
                        } else {
                            console.log('等待元素超時: ' + selector);
                        }
                    }
                    check();
                };
                
                // 統一的簽到對話框檢測函數
                window.startSigningCheck = function() {
                    console.log('開始檢測簽到對話框...');
                    
                    // 呼叫簽到框
                    if (typeof Signin !== 'undefined') {
                        Signin.showSigninMap();
                    }

                    var startTime = Date.now();
                    var timeout = 10000; // 10秒超時
                    
                    function checkDialog() {
                        var signDialog = document.querySelector('dialog#dialogify_1.dialogify.fixed.popup-dailybox');
                        if (signDialog) {
                            console.log('找到簽到對話框，呼叫 signSuccess');
                            Android.signSuccess();
                            return;
                        }
                        
                        // 檢查是否超時
                        if (Date.now() - startTime >= timeout) {
                            console.log('10秒內未找到簽到對話框');
                            Android.onFail();
                            return;
                        }
                        
                        // 繼續檢查
                        setTimeout(checkDialog, 500);
                    }
                    
                    checkDialog();
                };
                
                console.log('巴哈姆特通用腳本已載入');
            })();
        """.trimIndent()
        
        webView?.evaluateJavascript(script, null)
    }
    
    // 注入登入腳本
    private fun injectLoginScript(webView: WebView?) {
        val script = """
            javascript:(function() {
                // 設置登入表單
                function setupLoginForm() {
                    console.log('開始設置登入表單');
                    
                    // 等待用戶名輸入框
                    window.waitForElement('input[name="userid"]', function(useridInput) {
                        console.log('找到用戶名輸入框');
                        useridInput.value = '${UserSettings.propertiesUsername}';
                        
                        // 等待密碼輸入框
                        window.waitForElement('input[name="password"]', function(passwordInput) {
                            console.log('找到密碼輸入框');
                            passwordInput.value = '${UserSettings.propertiesPassword}';
                            
                            // 等待一段時間讓伺服器響應和原生腳本處理
                            setTimeout(function() {
                                console.log('檢查是否需要手動驗證');
                                
                                // 檢查reCAPTCHA
                                var recaptchaElements = document.querySelectorAll('.recaptcha-checkbox, #recaptcha-anchor, iframe[src*="recaptcha"], .g-recaptcha');
                                if (recaptchaElements.length > 0) {
                                    console.log('檢測到reCAPTCHA驗證，需要手動處理');
                                    Android.needManualLogin();
                                    return;
                                }
                                
                                // 檢查2SA
                                var twoFactorInput = document.querySelector('#input-2sa');
                                if (twoFactorInput) {
                                    var computedStyle = window.getComputedStyle(twoFactorInput);
                                    if (computedStyle.display !== 'none' && computedStyle.visibility !== 'hidden') {
                                        console.log('檢測到2SA驗證，需要手動處理');
                                        Android.needManualLogin();
                                        return;
                                    }
                                }
                                
                                console.log('無需手動驗證，繼續自動登入流程');

                                // 找到按鈕並提交登入表單
                                window.waitForElement('#btn-login, .btn-login, button[type="submit"]', function(loginButton) {
                                    console.log('點擊登入按鈕');
                                    loginButton.click();
                                });
                            }, 1000); // 等待1秒讓巴哈姆特原生腳本處理2SA檢測
                        });
                    });
                }
                
                // 檢查是否已經在登入頁面
                var loginForm = document.querySelector('input[name="userid"]');
                if (loginForm) {
                    console.log('已經在登入頁面');
                    setupLoginForm();
                } else {
                    // 使用統一的簽到檢測函數
                    if (typeof window.startSigningCheck === 'function') {
                        window.startSigningCheck();
                    } else {
                        console.log('通用腳本尚未載入，等待後重試');
                        setTimeout(function() {
                            if (typeof window.startSigningCheck === 'function') {
                                window.startSigningCheck();
                            } else {
                                Android.onFail();
                            }
                        }, 1000);
                    }
                }
            })();
        """.trimIndent()
        
        webView?.evaluateJavascript(script, null)
    }
    
    // 注入論壇頁面檢查腳本
    private fun injectForumScript(webView: WebView?) {
        val script = """
            javascript:(function() {
                // 使用統一的簽到檢測函數
                if (typeof window.startSigningCheck === 'function') {
                    window.startSigningCheck();
                } else {
                    console.log('通用腳本尚未載入，等待後重試');
                    setTimeout(function() {
                        if (typeof window.startSigningCheck === 'function') {
                            window.startSigningCheck();
                        } else {
                            console.log('無法載入通用腳本，執行失敗邏輯');
                            Android.onFail();
                        }
                    }, 1000);
                }
            })();
        """.trimIndent()
        
        webView?.evaluateJavascript(script, null)
    }

    // 設定 20 秒後自動清理
    private var timeoutASCoroutine: ASCoroutine? = object:ASCoroutine() {
        override suspend fun run() {
            println("WebView 登入 20 秒超時，自動清理")
            onFailCallback?.invoke()
            cleanup()
        }
    }
    private fun setupTimeout() {
        timeoutASCoroutine?.postDelayed(20000L) // 20 秒 = 20000 毫秒
    }
    
    // 取消計時器
    private fun cancelTimeout() {
        timeoutASCoroutine?.cancel()
    }

    // 清理WebView資源
    fun cleanup() {
        // 先取消計時器
        cancelTimeout()

        ASCoroutine.ensureMainThread {
            currentWebView?.let { webView ->
                webView.stopLoading()
                webView.loadUrl("about:blank")
                webView.destroy()
            }
            currentWebView = null

            // 清理回調函數引用
            onFailCallback = null
            onSignDetectedCallback = null
            onManualCallback = null
        }
    }
    
    // JavaScript接口類
    inner class WebAppInterface {
        @JavascriptInterface
        fun onFail() {
            println("檢測不到簽到。")

            ASCoroutine.ensureMainThread {
                onFailCallback?.invoke()
                cleanup()
            }
        }

        @JavascriptInterface
        fun signSuccess() {
            println("檢測到簽到對話框！")

            ASCoroutine.ensureMainThread {
                onSignDetectedCallback?.invoke()
                cleanup()
            }
        }
        
        @JavascriptInterface
        fun needManualLogin() {
            println("需要手動登入驗證")
            cancelTimeout()

            ASCoroutine.ensureMainThread {
                onManualCallback?.invoke(context.getString(R.string.login_web_sign_in_msg06))
            }
        }
        
    }
}