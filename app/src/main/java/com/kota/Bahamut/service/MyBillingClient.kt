package com.kota.Bahamut.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryPurchasesParams
import com.kota.Bahamut.R
import com.kota.asFramework.thread.ASCoroutine
import com.kota.asFramework.ui.ASToast
import com.kota.telnet.TelnetClient
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject

object MyBillingClient {
    lateinit var billingClient: BillingClient

    /** 購買結果 */
    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, list ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && list != null) {
            for (purchase in list) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            ASToast.showShortToast(TempSettings.applicationContext?.getString(R.string.billing_page_result_cancel))
        } else {
            ASToast.showShortToast(TempSettings.applicationContext?.getString(R.string.billing_page_result_error))
        }
    }

    private const val TAG = "MyBillingClient"
    private const val PREFS_NAME = "billing_pending_queue"
    private const val KEY_PENDING_LIST = "pending_list"

    /** 取得本機待重送佇列的 SharedPreferences */
    private fun getPendingPrefs(): SharedPreferences? {
        return TempSettings.applicationContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /** 取得當前有效帳號（優先使用即時連線帳號，次用偏好設定帳號） */
    fun getCurrentUsername(): String {
        return TelnetClient.myInstance?.username?.takeIf { it.isNotBlank() }
            ?: UserSettings.propertiesUsername
    }

    /** 取得本機待重送佇列中的所有購買紀錄 */
    @Synchronized
    fun getPendingPurchases(): List<JSONObject> {
        val prefs = getPendingPrefs() ?: return emptyList()
        val rawJson = prefs.getString(KEY_PENDING_LIST, null) ?: return emptyList()
        val list = mutableListOf<JSONObject>()
        try {
            val jsonArray = JSONArray(rawJson)
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getJSONObject(i))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getPendingPurchases parse error: ${e.message}")
        }
        return list
    }

    /** 新增或更新購買紀錄至本機待重送佇列（方案 2：防漏重試機制） */
    @Synchronized
    fun addPendingPurchase(
        purchaseToken: String,
        username: String,
        buyType: String,
        qty: Int,
        purchaseData: String
    ) {
        if (purchaseToken.isBlank()) return
        val prefs = getPendingPrefs() ?: return
        try {
            val list = getPendingPurchases().toMutableList()
            list.removeAll { it.optString("purchaseToken") == purchaseToken }
            val item = JSONObject().apply {
                put("purchaseToken", purchaseToken)
                put("username", username)
                put("buyType", buyType)
                put("qty", qty)
                put("purchaseData", purchaseData)
                put("timestamp", System.currentTimeMillis())
            }
            list.add(item)
            val jsonArray = JSONArray()
            list.forEach { jsonArray.put(it) }
            prefs.edit { putString(KEY_PENDING_LIST, jsonArray.toString()) }
            Log.d(TAG, "addPendingPurchase: 已暫存至本機待送達佇列 (token=$purchaseToken, user=$username, 總待補數=${list.size})")
        } catch (e: Exception) {
            Log.e(TAG, "addPendingPurchase error: ${e.message}", e)
        }
    }

    /** 雲端確認成功後，自待重送佇列移除紀錄 */
    @Synchronized
    fun removePendingPurchase(purchaseToken: String) {
        if (purchaseToken.isBlank()) return
        val prefs = getPendingPrefs() ?: return
        try {
            val list = getPendingPurchases().toMutableList()
            val removed = list.removeAll { it.optString("purchaseToken") == purchaseToken }
            if (removed) {
                val jsonArray = JSONArray()
                list.forEach { jsonArray.put(it) }
                prefs.edit { putString(KEY_PENDING_LIST, jsonArray.toString()) }
                Log.d(TAG, "removePendingPurchase: 雲端寫入確認成功，移出待送達佇列 (token=$purchaseToken, 剩餘=${list.size})")
            }
        } catch (e: Exception) {
            Log.e(TAG, "removePendingPurchase error: ${e.message}", e)
        }
    }

    /** 呼叫後端 API 寫入購買紀錄 */
    private fun sendPurchaseRecordApi(
        purchaseToken: String,
        username: String,
        buyType: String,
        qty: Int,
        purchaseData: String,
        onResult: ((Boolean) -> Unit)? = null
    ) {
        if (username.isBlank()) {
            onResult?.invoke(false)
            return
        }

        val userId = AESCrypt.encrypt(username)
        val apiUrl = "https://user-buy-history.kodakjerec.work/"
        val client = OkHttpClient()
        val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("userId", userId)
            .addFormDataPart("buyType", buyType)
            .addFormDataPart("qty", qty.toString())
            .addFormDataPart("purchaseData", purchaseData)
            .build()
        val request: Request = Request.Builder()
            .url(apiUrl)
            .post(body)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val respBody = response.body.string()
                    Log.d(TAG, "雲端寫入購買紀錄成功 (buyType=$buyType, user=$username): $respBody")
                    onResult?.invoke(true)
                } else {
                    Log.e(TAG, "雲端寫入購買紀錄失敗 (buyType=$buyType, user=$username): HTTP ${response.code}")
                    onResult?.invoke(false)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "雲端寫入購買紀錄發生例外 (buyType=$buyType, user=$username): ${e.message}", e)
            onResult?.invoke(false)
        }
    }

    /**
     * 重送本機待送達佇列中的所有購買紀錄（斷網恢復或登入時自動重試）
     */
    @JvmStatic
    fun processPendingPurchases(onAllComplete: (() -> Unit)? = null) {
        val pendingList = getPendingPurchases()
        if (pendingList.isEmpty()) {
            onAllComplete?.invoke()
            return
        }

        Log.d(TAG, "processPendingPurchases: 發現 ${pendingList.size} 筆未送達購買紀錄，開始重送...")
        ASCoroutine.runInNewCoroutine {
            val currentUsername = getCurrentUsername()
            for (record in pendingList) {
                val token = record.optString("purchaseToken")
                var user = record.optString("username")
                if (user.isBlank() && currentUsername.isNotBlank()) {
                    user = currentUsername
                    // 補上登入帳號至本地暫存
                    addPendingPurchase(
                        token,
                        user,
                        record.optString("buyType", "purchase"),
                        record.optInt("qty", 1),
                        record.optString("purchaseData")
                    )
                }
                if (user.isNotBlank()) {
                    val buyType = record.optString("buyType", "purchase")
                    val qty = record.optInt("qty", 1)
                    val purchaseData = record.optString("purchaseData")
                    sendPurchaseRecordApi(token, user, buyType, qty, purchaseData) { success ->
                        if (success) {
                            removePendingPurchase(token)
                            if (!UserSettings.propertiesVIP) {
                                UserSettings.propertiesVIP = true
                            }
                        }
                    }
                }
            }
            onAllComplete?.invoke()
        }
    }

    /**
     * 將購買紀錄寫入雲端 API（同時由本機待送達佇列防護）
     */
    @JvmStatic
    fun uploadPurchaseRecordToCloud(
        purchaseToken: String,
        qty: Int,
        purchaseData: String,
        buyType: String = "purchase",
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        val username = getCurrentUsername()
        // 1. 第一時間寫入本機待送達佇列，確保網路斷線或閃退不遺漏
        addPendingPurchase(purchaseToken, username, buyType, qty, purchaseData)

        if (username.isBlank()) {
            Log.w(TAG, "uploadPurchaseRecordToCloud: 帳號為空，已安全保存於本地待重試佇列 (buyType=$buyType)")
            onComplete?.invoke(false)
            return
        }

        ASCoroutine.runInNewCoroutine {
            sendPurchaseRecordApi(purchaseToken, username, buyType, qty, purchaseData) { success ->
                if (success) {
                    // 2. 確定雲端寫入成功後移出佇列
                    removePendingPurchase(purchaseToken)
                    if (!UserSettings.propertiesVIP) {
                        UserSettings.propertiesVIP = true
                    }
                }
                onComplete?.invoke(success)
            }
        }
    }

    @JvmStatic
    fun uploadPurchaseRecordToCloud(
        purchase: Purchase,
        buyType: String = "purchase",
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        uploadPurchaseRecordToCloud(
            purchaseToken = purchase.purchaseToken,
            qty = purchase.quantity,
            purchaseData = purchase.originalJson,
            buyType = buyType,
            onComplete = onComplete
        )
    }

    /** 確認購買交易，且程式已授予使用者商品 */
    private fun handlePurchase(purchases: Purchase) {
        if (purchases.purchaseState != Purchase.PurchaseState.PURCHASED) {
            return
        }
        if (!purchases.isAcknowledged) {
            billingClient.acknowledgePurchase(
                AcknowledgePurchaseParams
                    .newBuilder()
                    .setPurchaseToken(purchases.purchaseToken)
                    .build()
            ) { billingResult: BillingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    consumePurchase(purchases)
                }
            }
        } else {
            consumePurchase(purchases)
        }
    }

    /** 購買後要回應訊息給google和使用者，並將購買紀錄寫入雲端 */
    private fun consumePurchase(purchase: Purchase) {
        // 第一時間寫入雲端購買紀錄，確保已付款資料必定上傳（含本機佇列保護）
        uploadPurchaseRecordToCloud(purchase, "purchase")

        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        val consumeResponseListener =
            ConsumeResponseListener { billingResult: BillingResult, _: String? ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    if (!UserSettings.propertiesVIP) {
                        UserSettings.propertiesVIP = true
                    }
                    ASToast.showShortToast(TempSettings.applicationContext?.getString(R.string.billing_page_result_success))
                }
            }
        billingClient.consumeAsync(consumeParams, consumeResponseListener)
    }

    /** 重新確認已購買的商品（結合未消耗查詢與本機待送達佇列重試） */
    @JvmStatic
    @JvmOverloads
    fun checkPurchaseHistoryQuery(forceCheck: Boolean = false) {
        // 先處理本機待重送的訂單（方案 2：防漏機制）
        processPendingPurchases()

        // 若當前已是 VIP 且非強制手動檢查，則略過檢查
        if (!forceCheck && UserSettings.propertiesVIP) {
            Log.d(TAG, "checkPurchaseHistoryQuery: 當前已是 VIP，略過檢查")
            return
        }

        if (!::billingClient.isInitialized || !billingClient.isReady) {
            Log.w(TAG, "checkPurchaseHistoryQuery: BillingClient 尚未就緒，直接走雲端查詢")
            checkPurchaseHistoryCloud { }
            return
        }

        try {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()

            billingClient.queryPurchasesAsync(params) { billingResult: BillingResult, purchaseList: List<Purchase> ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchaseList.isNotEmpty()) {
                    Log.d(TAG, "queryPurchasesAsync 找到 ${purchaseList.size} 筆進行中有效商品")
                    UserSettings.propertiesVIP = true
                    purchaseList.forEach { record ->
                        uploadPurchaseRecordToCloud(record, "history")
                    }
                } else {
                    // 因消耗型商品消耗後不在 queryPurchasesAsync 中，
                    // 依 Google 官方指引，向伺服器端（儲存歷史並可串接 Google Play Developer API）核對紀錄
                    checkPurchaseHistoryCloud { }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkPurchaseHistoryQuery exception: ${e.message}", e)
            checkPurchaseHistoryCloud { }
        }
    }

    /** 檢查購買紀錄 */
    @JvmStatic
    fun checkPurchaseHistoryCloud(callback: (Int) -> Unit) {
        val username = getCurrentUsername()
        if (username.isBlank()) {
            Log.w(TAG, "checkPurchaseHistoryCloud: 帳號為空，略過雲端歷史檢查")
            callback(0)
            return
        }

        val userId = AESCrypt.encrypt(username)
        val apiUrl = "https://user-buy-history.kodakjerec.work/"
        val client = OkHttpClient()
        val body: RequestBody =
            MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("userId", userId)
                .addFormDataPart("buyType", "query")
                .build()
        val request: Request = Request.Builder()
            .url(apiUrl)
            .post(body)
            .build()
        ASCoroutine.runInNewCoroutine {
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val data = response.body.string()
                        val jsonObject = JSONObject(data)
                        val buyQty = jsonObject.optString("qty", "0").toInt()
                        UserSettings.propertiesVIP = buyQty > 0
                        Log.d(TAG, "checkPurchaseHistoryCloud: 帳號 $username 購買數量=$buyQty, VIP=${UserSettings.propertiesVIP}")
                        callback(buyQty)
                    } else {
                        // 網路或伺服器異常時，不主動修改使用者 VIP 權限
                        Log.w(TAG, "checkPurchaseHistoryCloud HTTP error: ${response.code}")
                        callback(0)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "checkPurchaseHistoryCloud exception: ${e.message}")
                callback(0)
            }
        }
    }

    /** 處理應用程式外的購買交易 */
    @JvmStatic
    fun checkPurchase() {
        if (::billingClient.isInitialized && billingClient.isReady) {
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP)
                    .build()
            ) { billingResult: BillingResult, list: List<Purchase>? ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && list != null) {
                    for (purchase in list) {
                        handlePurchase(purchase)
                    }
                }
            }
        }
    }

    /** 初始化 BillingClient */
    @JvmStatic
    fun initBillingClient() {
        billingClient = BillingClient.newBuilder(TempSettings.applicationContext!!)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .build()

        // 商店付款建立
        // initial
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                print("Billing Service disconnected")
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    processPendingPurchases()
                    if (!UserSettings.propertiesVIP) {
                        checkPurchaseHistoryQuery()
                    }
                }
            }
        })
    }

    @JvmStatic
    fun closeBillingClient() {
        if (::billingClient.isInitialized && billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}
