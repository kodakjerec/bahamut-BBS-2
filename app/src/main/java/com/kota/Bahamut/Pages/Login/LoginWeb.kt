package com.kota.Bahamut.Pages.Login

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.kota.ASFramework.Thread.ASRunner
import com.kota.Bahamut.Service.UserSettings

class LoginWeb(private val context: Context) {
    
    private var currentWebView: WebView? = null
    private var onLogoutCompleteCallback: (() -> Unit)? = null
    private var onSignDetectedCallback: (() -> Unit)? = null
    @SuppressLint("SetJavaScriptEnabled")
    fun init(onLogoutComplete: (() -> Unit)? = null, onSignDetected: (() -> Unit)? = null) {
        this.onLogoutCompleteCallback = onLogoutComplete
        this.onSignDetectedCallback = onSignDetected
        
        try {
            // 直接創建WebView，不使用Dialog
            val webView = WebView(context)
            currentWebView = webView
        
            // WebView設定
            val webSettings: WebSettings = webView.settings
            webSettings.javaScriptEnabled = true

            // 禁用圖片載入
            webSettings.loadsImagesAutomatically = false
            webSettings.blockNetworkImage = true
                        
            // 禁用影片和媒體自動播放
            webSettings.mediaPlaybackRequiresUserGesture = true

            // 禁用不必要的功能來提升載入速度
            webSettings.builtInZoomControls = false
            webSettings.displayZoomControls = false
            webSettings.allowFileAccess = false  // 禁用檔案存取
            webSettings.allowContentAccess = false  // 禁用內容存取
            webSettings.domStorageEnabled = true  // 保留DOM存儲（登入可能需要）

            // 添加 JavaScript 接口
            webView.addJavascriptInterface(WebAppInterface(), "Android")

            // 設定WebViewClient
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)

                    // 頁面載入完成後，根據URL執行不同的腳本
                    when {
                        url?.contains("login.php") == true -> {
                            // 在登入頁面，注入登入腳本
                            injectLoginScript(view)
                        }
                        url?.contains("logout.php") == true -> {
                            // 在登出頁面，注入登出腳本
                            injectLogoutScript(view)
                        }
                        url?.contains("gamer.com.tw") == true -> {
                            // 在其他巴哈頁面，檢查登入狀態
                            injectLoginScript(view)
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
            e.printStackTrace()
        }
    }
    
    private fun injectLoginScript(webView: WebView?) {
        val script = """
            javascript:(function() {
                // 檢查 sessionStorage 是否已經執行過腳本
                if (sessionStorage.getItem('bahamutLoginScriptExecuted')) {
                    console.log('登入腳本已經執行過，跳過重複執行');
                    Android.onLogoutSuccess();
                    return;
                }
                
                // 標記腳本已執行到 sessionStorage
                sessionStorage.setItem('bahamutLoginScriptExecuted', 'true');
                console.log('開始注入登入腳本');
                
                // 等待元素出現的函數
                function waitForElement(selector, callback, timeout = 10000) {
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
                }
                
                // 設置登入表單
                function setupLoginForm() {
                    console.log('開始設置登入表單');
                    
                    // 等待用戶名輸入框
                    waitForElement('input[name="userid"]', function(useridInput) {
                        console.log('找到用戶名輸入框');
                        useridInput.value = '${UserSettings.getPropertiesUsername()}';
                        
                        // 等待密碼輸入框
                        waitForElement('input[name="password"]', function(passwordInput) {
                            console.log('找到密碼輸入框');
                            passwordInput.value = '${UserSettings.getPropertiesPassword()}';
                            
                            // 等待登入按鈕
                            waitForElement('#btn-login, .btn-login, button[type="submit"]', function(loginButton) {
                                console.log('自動點擊登入按鈕');
                                loginButton.click();
                            });
                        });
                    });
                }
                
                // 檢查是否已經在登入頁面
                var loginForm = document.querySelector('input[name="userid"]');
                if (loginForm) {
                    console.log('已經在登入頁面');
                    setupLoginForm();
                } else {
                    console.log('不在登入頁面，5秒後跳轉到登出頁面');
                    
                    console.log('開始檢測簽到對話框...');
                    
                    Signin.mobile();

                    var startTime = Date.now();
                    var timeout = 5000; // 5秒超時
                    
                    function checkDialog() {
                        var signDialog = document.querySelector('dialog#dialogify_1.dialogify.fixed.popup-dailybox');
                        if (signDialog) {
                            console.log('找到簽到對話框，呼叫 signSuccess，跳轉登出');
                            Android.signSuccess();
                            setTimeout(()=>{
                                window.location.href = 'https://user.gamer.com.tw/logout.php';
                            }, 3000);
                            return;
                        }
                        
                        // 檢查是否超時
                        if (Date.now() - startTime >= timeout) {
                            console.log('5秒內未找到簽到對話框，跳轉登出');
                            // 跳轉到登出頁面
                            setTimeout(()=>{
                                window.location.href = 'https://user.gamer.com.tw/logout.php';
                            }, 3000);
                            return;
                        }
                        
                        // 繼續檢查
                        setTimeout(checkDialog, 500);
                    }
                    
                    checkDialog();
                }
            })();
        """.trimIndent()
        
        webView?.evaluateJavascript(script, null)
    }
    
    private fun injectLogoutScript(webView: WebView?) {
        val script = """
            javascript:(function() {
                // 檢查 sessionStorage 是否已經執行過登出腳本
                if (sessionStorage.getItem('bahamutLogoutScriptExecuted')) {
                    console.log('登出腳本已經執行過，跳過重複執行');
                    Android.onLogoutSuccess();
                    return;
                }
                
                // 標記登出腳本已執行到 sessionStorage
                sessionStorage.setItem('bahamutLogoutScriptExecuted', 'true');
                console.log('開始注入登出腳本');
                
                // 等待元素出現的函數
                function waitForElement(selector, callback, timeout = 10000) {
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
                }
                
                // 等待並點擊登出確定按鈕
                waitForElement('button.btn.btn--primary[onclick="logout();"]', function(logoutButton) {
                    console.log('找到登出確定按鈕，點擊登出');
                    logoutButton.click();
                    
                    setTimeout(()=>{
                    Android.onLogoutSuccess();
                    }, 3000);
                });
            })();
        """.trimIndent()
        
        webView?.evaluateJavascript(script, null)
    }
    
    // 清理WebView資源
    fun cleanup() {
        object : ASRunner() {
            override fun run() {
                currentWebView?.let { webView ->
                    webView.stopLoading()
                    webView.clearHistory()
                    webView.clearCache(true)
                    webView.loadUrl("about:blank")
                    webView.destroy()
                }
                currentWebView = null
            }
        }.runInMainThread()
    }
    
    // JavaScript接口類
    inner class WebAppInterface {
        @JavascriptInterface
        fun onLogoutSuccess() {
            println("登出執行完畢！")
            
            // 通知登入完成
            object : ASRunner() {
                override fun run() {
                    onLogoutCompleteCallback?.invoke()
                    cleanup()
                }
            }.runInMainThread()
        }

        @JavascriptInterface
        fun signSuccess() {
            println("檢測到簽到對話框！")
            
            // 通知檢測到簽到
            object : ASRunner() {
                override fun run() {
                    onSignDetectedCallback?.invoke()
                }
            }.runInMainThread()
        }
    }
}