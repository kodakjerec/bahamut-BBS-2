package com.kota.Bahamut.service

import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.kota.telnet.TelnetClient
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
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
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

    /** 取得當前有效帳號（優先使用即時連線帳號，次用偏好設定帳號） */
    fun getCurrentUsername(): String {
        return TelnetClient.myInstance?.username?.takeIf { it.isNotBlank() }
            ?: UserSettings.propertiesUsername
    }

    /**
     * 將購買紀錄寫入雲端 API
     * 確保課金紀錄必定發送到伺服器保存，並帶有完整日誌與例外處理
     */
    @JvmStatic
    fun uploadPurchaseRecordToCloud(
        purchase: Purchase,
        buyType: String = "purchase",
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        val username = getCurrentUsername()
        if (username.isBlank()) {
            Log.w(TAG, "uploadPurchaseRecordToCloud: 帳號為空，略過雲端購買紀錄寫入 (buyType=$buyType)")
            onComplete?.invoke(false)
            return
        }

        val userId = AESCrypt.encrypt(username)
        val apiUrl = "https://user-buy-history.kodakjerec.work/"
        val client = OkHttpClient()
        val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("userId", userId)
            .addFormDataPart("buyType", buyType)
            .addFormDataPart("qty", purchase.quantity.toString())
            .addFormDataPart("purchaseData", purchase.originalJson)
            .build()
        val request: Request = Request.Builder()
            .url(apiUrl)
            .post(body)
            .build()

        ASCoroutine.runInNewCoroutine {
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val respBody = response.body.string()
                        Log.d(TAG, "雲端寫入購買紀錄成功 (buyType=$buyType, user=$username): $respBody")
                        onComplete?.invoke(true)
                    } else {
                        Log.e(TAG, "雲端寫入購買紀錄失敗 (buyType=$buyType, user=$username): HTTP ${response.code}")
                        onComplete?.invoke(false)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "雲端寫入購買紀錄發生例外 (buyType=$buyType, user=$username): ${e.message}", e)
                onComplete?.invoke(false)
            }
        }
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
        // 第一時間寫入雲端購買紀錄，確保已付款資料必定上傳
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

    /** 重新確認已購買的商品 */
    @JvmStatic
    fun checkPurchaseHistoryQuery() {
        try {
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            ) { billingResult: BillingResult, list: List<Purchase?>? ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && list != null) {
                    // 如果有成功購買紀錄, 但是沒有開啟VIP, 則開啟
                    if (list.toTypedArray().isNotEmpty()) {
                        UserSettings.propertiesVIP = true
                        // 將購買結果補傳至雲端
                        list.filterNotNull().forEach { record ->
                            uploadPurchaseRecordToCloud(record, "history")
                        }
                    } else {
                        // Google Play 回傳空清單（因已消耗商品在 queryPurchasesAsync 不會返回），
                        // 絕不可在此處過早設置 propertiesVIP = false，需等待 checkPurchaseHistoryCloud 查詢後端
                        checkPurchaseHistoryCloud { }
                    }
                }
            }
        } catch (_: Exception) {
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
                        if (buyQty > 0) {
                            UserSettings.propertiesVIP = true
                        } else {
                            UserSettings.propertiesVIP = false
                        }
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
                checkPurchaseHistoryQuery()
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
