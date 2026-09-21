package com.kota.Bahamut.pages

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsResult
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.MyBillingClient
import com.kota.Bahamut.service.MyBillingClient.checkPurchaseHistoryCloud
import com.kota.Bahamut.service.MyBillingClient.checkPurchaseHistoryQuery
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.components.BahaText
import com.kota.Bahamut.ui.components.rememberDrawablePainter
import com.kota.Bahamut.ui.dialogs.BahaGlobalDialogHost
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.Bahamut.ui.theme.setBahamutContent
import com.kota.asFramework.thread.ASCoroutine
import com.kota.asFramework.ui.ASToast.showShortToast
import com.kota.telnetUI.TelnetPage

class BillingPage : TelnetPage() {
    private var billingClient: BillingClient? = null

    override val pageType: Int
        get() = BahamutPage.BAHAMUT_BILLING

    override val pageLayout: Int
        get() = 0

    override val isPopupPage: Boolean
        get() = true

    override val isKeepOnOffline: Boolean
        get() = true

    // Compose states
    var productDetailsState by mutableStateOf<ProductDetails?>(null)
    var alreadyBillingValueState by mutableStateOf("")

    override fun createPageView(context: Context): View {
        return ComposeView(context).apply {
            setBahamutContent {
                BillingPageContent()
                BahaGlobalDialogHost()
            }
        }
    }

    override fun onPageDidLoad() {
        billingClient = MyBillingClient.billingClient
        fetchProductList()
        checkPurchaseHistory()
    }

    private fun checkPurchaseHistory() {
        checkPurchaseHistoryQuery(forceCheck = true)
        checkPurchaseHistoryCloud { qty: Int? ->
            val totalMoney = ((qty ?: 0) * 90).toString()
            ASCoroutine.ensureMainThread {
                alreadyBillingValueState = totalMoney
            }
        }
    }

    private fun fetchProductList() {
        val productList = ArrayList<QueryProductDetailsParams.Product?>().apply {
            add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId("com.kota.billing.90")
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
        }

        val queryProductDetailsParams =
            QueryProductDetailsParams.newBuilder()
                .setProductList(productList).build()

        billingClient?.queryProductDetailsAsync(
            queryProductDetailsParams
        ) { billingResult: BillingResult?, productDetailsResult: QueryProductDetailsResult? ->
            if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {
                val detailsList = productDetailsResult?.productDetailsList
                if (!detailsList.isNullOrEmpty()) {
                    ASCoroutine.ensureMainThread {
                        productDetailsState = detailsList[0]
                    }
                }
            }
        }
    }

    private fun launchPurchase() {
        val product = productDetailsState ?: return
        val activity: Activity = navigationController ?: return

        val productDetailsParamsList = ArrayList<ProductDetailsParams?>().apply {
            add(
                ProductDetailsParams.newBuilder()
                    .setProductDetails(product)
                    .build()
            )
        }

        val billingFlowParams =
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .setIsOfferPersonalized(true)
                .build()

        billingClient?.launchBillingFlow(activity, billingFlowParams)
    }

    override fun onBackPressed(): Boolean {
        PageContainer.instance?.cleanBillingPage()
        return super.onBackPressed()
    }

    override fun onReceivedGestureRight(): Boolean {
        onBackPressed()
        PageContainer.instance?.cleanBillingPage()
        showShortToast("返回")
        return true
    }

    @Composable
    fun BillingPageContent() {
        val colors = AppTheme.colors
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.pageBackground)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = rememberDrawablePainter(resId = R.mipmap.ic_launcher),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier.size(100.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                BahaText(
                    text = stringResource(R.string.billing_page_note),
                    fontSize = AppTheme.fontSize.caption,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                BahaText(
                    text = stringResource(R.string.billing_page_note2),
                    color = colors.titleBarTitle,
                    fontSize = AppTheme.fontSize.body,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Purchase button (com.kota.billing.90)
                BahaButton(
                    text = productDetailsState?.name ?: stringResource(R.string.billing_page_button_90),
                    enabled = productDetailsState != null,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    onClick = { launchPurchase() }
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = colors.divider, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // Check purchase button
                BahaButton(
                    text = stringResource(R.string.billing_page_button_check_purchase_query),
                    onClick = {
                        checkPurchaseHistory()
                        showShortToast(getContextString(R.string.billing_page_result_success))
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Already billing value
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BahaText(
                        text = stringResource(R.string.billing_page_already_billing_text),
                        fontSize = AppTheme.fontSize.caption,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    BahaText(
                        text = alreadyBillingValueState,
                        fontSize = AppTheme.fontSize.caption,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Bottom toolbar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.toolbarDivider)
            )
            BahaButton(
                text = stringResource(R.string._back),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                onClick = {
                    onBackPressed()
                    PageContainer.instance?.cleanBillingPage()
                }
            )
        }
    }
}
