package com.kota.Bahamut.pages

import android.app.Activity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsResult
import com.kota.asFramework.thread.ASRunner
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.pages.theme.ThemeFunctions
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.MyBillingClient
import com.kota.Bahamut.service.MyBillingClient.checkPurchaseHistoryCloud
import com.kota.Bahamut.service.MyBillingClient.checkPurchaseHistoryQuery
import com.kota.telnetUI.TelnetPage

class BillingPage : TelnetPage() {
    private var billingClient: BillingClient? = null

    val pageType: Int
        // com.kota.ASFramework.PageController.ASViewController
        get() = BahamutPage.BAHAMUT_BILLING

    val pageLayout: Int
        get() = R.layout.billing_page

    val isPopupPage: Boolean
        // com.kota.TelnetUI.TelnetPage
        get() = true

    val isKeepOnOffline: Boolean
        // com.kota.TelnetUI.TelnetPage
        get() = true

    public override fun onPageDidLoad() {
        billingClient = MyBillingClient.billingClient
        this.productList

        checkPurchaseHistoryQuery()

        // 檢查已購買
        val button1 = findViewById(R.id.button_checkPurchaseQuery) as Button?
        button1!!.setOnClickListener(View.OnClickListener { view: View? ->
            checkPurchaseHistoryCloud { qty: Int? ->
                val totalMoney = (qty!! * 90).toString()
                val textView = findViewById(R.id.BillingPage_already_billing_value) as TextView?
                if (textView != null) {
                    textView.setText(totalMoney)
                }
                Unit
            }
            showShortToast(getContextString(R.string.billing_page_result_success))
        })
        button1.performClick()

        // 替換外觀
        ThemeFunctions().layoutReplaceTheme(findViewById(R.id.toolbar) as LinearLayout?)
    }

    val productList: Unit
        // 取得商品清單
        get() {
            val activity: Activity? = navigationController

            // The BillingClient is ready. You can query purchases here.
            val productList =
                ArrayList<QueryProductDetailsParams.Product?>()
            productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId("com.kota.billing.90")
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
            // list products
            val queryProductDetailsParams =
                QueryProductDetailsParams.newBuilder()
                    .setProductList(productList).build()

            billingClient!!.queryProductDetailsAsync(
                queryProductDetailsParams,
                ProductDetailsResponseListener { billingResult: BillingResult?, productDetailsList: QueryProductDetailsResult? ->
                    // check billingResult
                    if (billingResult!!.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        // process returned productDetailsList
                        for (product in productDetailsList!!.getProductDetailsList()) {
                            object : ASRunner() {
                                public override fun run() {
                                    val btn =
                                        findViewById(R.id.button_90) as Button?
                                    if (btn != null) {
                                        btn.setEnabled(true)
                                        btn.setText(product.getName())
                                        btn.setOnClickListener(View.OnClickListener { view: View? ->
                                            val productDetailsParamsList =
                                                ArrayList<ProductDetailsParams?>()
                                            productDetailsParamsList
                                                .add(
                                                    ProductDetailsParams.newBuilder()
                                                        .setProductDetails(product)
                                                        .build()
                                                )

                                            val billingFlowParams =
                                                BillingFlowParams.newBuilder()
                                                    .setProductDetailsParamsList(
                                                        productDetailsParamsList
                                                    )
                                                    .setIsOfferPersonalized(true)
                                                    .build()

                                            // Launch the billing flow
                                            billingClient!!.launchBillingFlow(
                                                activity!!,
                                                billingFlowParams
                                            )
                                        })
                                    }
                                }
                            }.runInMainThread()
                        }
                    }
                })
        }

    public override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        PageContainer.getInstance().cleanBillingPage()
        showShortToast("返回")
        return true
    }
}
