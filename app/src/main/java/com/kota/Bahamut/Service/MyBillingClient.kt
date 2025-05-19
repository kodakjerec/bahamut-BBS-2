package com.kota.Bahamut.Service

import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchaseHistoryRecord
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryPurchaseHistoryParams
import com.android.billingclient.api.QueryPurchasesParams
import com.kota.ASFramework.Thread.ASRunner
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.R
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
            ASToast.showShortToast(TempSettings.applicationContext!!.getString(R.string.billing_page_result_cancel))
        } else {
            ASToast.showShortToast(TempSettings.applicationContext!!.getString(R.string.billing_page_result_error))
        }
    }

    /** 確認購買交易，且程式已授予使用者商品 */
    private fun handlePurchase(purchases: Purchase) {
        if (!purchases.isAcknowledged) {
            billingClient.acknowledgePurchase(
                AcknowledgePurchaseParams
                    .newBuilder()
                    .setPurchaseToken(purchases.purchaseToken)
                    .build()
            ) { billingResult: BillingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    for (pur in purchases.products) {
                        //Calling Consume to consume the current purchase
                        // so user will be able to buy same product again
                        consumePurchase(purchases)
                    }
                }
            }
        } else {
            for (pur in purchases.products) {
                //Calling Consume to consume the current purchase
                // so user will be able to buy same product again
                consumePurchase(purchases)
            }
        }
    }

    /** 購買後要回應訊息給google和使用者 */
    private fun consumePurchase(purchase: Purchase) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        val consumeResponseListener =
            ConsumeResponseListener { billingResult: BillingResult, _: String? ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    if (!UserSettings.getPropertiesVIP()) {
                        UserSettings.setPropertiesVIP(true)
                    }
                    ASToast.showShortToast(TempSettings.applicationContext!!.getString(R.string.billing_page_result_success))
                    // 將購買結果塞入雲端
                    if (UserSettings.getPropertiesUsername().isNotEmpty()) {
                        val userId = AESCrypt.encrypt(UserSettings.getPropertiesUsername())
                        val apiUrl = "https://user-buy-history.kodakjerec.workers.dev/"
                        val client = OkHttpClient()
                        val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                            .addFormDataPart("userId", userId)
                            .addFormDataPart("buyType", "purchase")
                            .addFormDataPart("qty", purchase.quantity.toString())
                            .addFormDataPart("purchaseData", purchase.originalJson)
                            .build()
                        val request: Request = Request.Builder()
                            .url(apiUrl)
                            .post(body)
                            .build()

                        ASRunner.runInNewThread {
                            client.newCall(request).execute().use { _ -> }
                        }
                    }
                }
            }
        billingClient.consumeAsync(consumeParams, consumeResponseListener)
    }

    /** 重新確認已購買的商品 */
    @JvmStatic
    fun checkPurchaseHistoryQuery() {
        var needQueryCloud = false
        try {
            billingClient.queryPurchaseHistoryAsync(
                QueryPurchaseHistoryParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            ) { billingResult: BillingResult, list: List<PurchaseHistoryRecord?>? ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && list != null) {
                    // 如果有成功購買紀錄, 但是沒有開啟VIP, 則開啟
                    if (list.toTypedArray().isNotEmpty()) {
                        UserSettings.setPropertiesVIP(true)
                        // 將購買結果塞入雲端
                        if (UserSettings.getPropertiesUsername().isNotEmpty()) {
                            list.forEach { record ->
                                val userId = AESCrypt.encrypt(UserSettings.getPropertiesUsername())
                                val apiUrl = "https://user-buy-history.kodakjerec.workers.dev/"
                                val client = OkHttpClient()
                                val body: RequestBody =
                                    MultipartBody.Builder().setType(MultipartBody.FORM)
                                        .addFormDataPart("userId", userId)
                                        .addFormDataPart("buyType", "history")
                                        .addFormDataPart("qty", record!!.quantity.toString())
                                        .addFormDataPart("purchaseData", record.originalJson)
                                        .build()
                                val request: Request = Request.Builder()
                                    .url(apiUrl)
                                    .post(body)
                                    .build()

                                ASRunner.runInNewThread {
                                    try {
                                        client.newCall(request).execute().use { _ -> }
                                    } catch (e:Exception) {
                                        needQueryCloud = true
                                    }
                                }
                            }
                        }
                    } else {
                        // 查不到有可能是函數不能用, 走其他方式
                        UserSettings.setPropertiesVIP(false)
                        needQueryCloud = true
                    }
                }
            }
        }catch (e:Exception) {
            needQueryCloud = true
        }

        if (needQueryCloud) {
            checkPurchaseHistoryCloud{ }
        }
    }

    /** 檢查購買紀錄 */
    @JvmStatic
    fun checkPurchaseHistoryCloud(callback: (Int) -> Unit) {
        val userId = AESCrypt.encrypt(UserSettings.getPropertiesUsername())
        val apiUrl = "https://user-buy-history.kodakjerec.workers.dev/"
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
        ASRunner.runInNewThread {
            client.newCall(request).execute().use { response ->
                val data = response.body!!.string()
                val jsonObject = JSONObject(data)
                val buyQty =  jsonObject.optString("qty", "0").toInt()
                if (buyQty>0) {
                    UserSettings.setPropertiesVIP(true)
                } else {
                    UserSettings.setPropertiesVIP(false)
                }
                callback(buyQty)
            }
        }
    }

    /** 處理應用程式外的購買交易 */
    @JvmStatic
    fun checkPurchase() {
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
        billingClient.endConnection()
    }
}
