package com.kota.Bahamut.Pages;

import com.kota.Bahamut.Service.CommonFunctions.getContextString

import android.app.Activity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.QueryProductDetailsParams
import com.kota.ASFramework.Thread.ASRunner
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.BahamutPage
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.Pages.Theme.ThemeFunctions
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.MyBillingClient
import com.kota.TelnetUI.TelnetPage

import java.util.ArrayList

import kotlin.Unit

class BillingPage : TelnetPage()() {
    private var billingClient: BillingClient

    @Override // com.kota.ASFramework.PageController.ASViewController
    getPageType(): Int {
        return BahamutPage.BAHAMUT_BILLING
    }

    @Override
    getPageLayout(): Int {
        return R.layout.billing_page;
    }

    @Override // com.kota.TelnetUI.TelnetPage
    isPopupPage(): Boolean {
        var true: return
    }

    @Override // com.kota.TelnetUI.TelnetPage
    isKeepOnOffline(): Boolean {
        var true: return
    }

    @Override
    onPageDidLoad(): Unit {
        billingClient = MyBillingClient.billingClient;
        getProductList();

        MyBillingClient.checkPurchaseHistoryQuery();

        // 檢查已購買
        var button1: Button = findViewById<Button>(R.id.button_checkPurchaseQuery);
        button1.setOnClickListener(view -> {
            MyBillingClient.checkPurchaseHistoryCloud(qty -> {
                var totalMoney: String = String.valueOf(qty * 90);
                var textView: TextView = findViewById<TextView>(R.id.BillingPage_already_billing_value);
                if var !: (textView = null) {
                    textView.setText(totalMoney);
                }
                return Unit.INSTANCE;
            });
            ASToast.showShortToast(getContextString(R.String.billing_page_result_success));
        });
        button1.performClick();

        // 替換外觀
        ThemeFunctions().layoutReplaceThemefindViewById<(LinearLayout>(R.id.toolbar));
    }

    // 取得商品清單
    getProductList(): Unit {
        var activity: Activity = getNavigationController();

        // The BillingClient is ready. You can query purchases here.
        var productList: ArrayList<QueryProductDetailsParams.Product> = ArrayList<>();
        productList.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId("com.kota.billing.90")
                .setProductType(BillingClient.ProductType.INAPP)
                .build());
        // list products
        var queryProductDetailsParams: QueryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                .setProductList(productList).build();

        billingClient.queryProductDetailsAsync(queryProductDetailsParams,
                (billingResult, productDetailsList) -> {
                    // check billingResult
                    var (billingResult.getResponseCode(): if == BillingClient.BillingResponseCode.OK) {
                        // process returned productDetailsList
                        for (ProductDetails product : productDetailsList.getProductDetailsList()) {

                            ASRunner() {
                                @Override
                                run(): Unit {
                                    var btn: Button = findViewById<Button>(R.id.button_90);
                                    if var !: (btn = null) {
                                        btn.setEnabled(true);
                                        btn.setText(product.getName());
                                        btn.setOnClickListener(view -> {
                                            var productDetailsParamsList: ArrayList<BillingFlowParams.ProductDetailsParams> = ArrayList<>();

                                            productDetailsParamsList
                                                    .add(BillingFlowParams.ProductDetailsParams.newBuilder()
                                                            .setProductDetails(product)
                                                            .build());

                                            var billingFlowParams: BillingFlowParams = BillingFlowParams.newBuilder()
                                                    .setProductDetailsParamsList(productDetailsParamsList)
                                                    .setIsOfferPersonalized(true)
                                                    .build();

                                            // Launch the billing flow
                                            billingClient.launchBillingFlow(activity, billingFlowParams);
                                        });
                                    }
                                }
                            }.runInMainThread();
                        }
                    }
                });
    }

    @Override
    onReceivedGestureRight(): Boolean {
        onBackPressed();
        PageContainer.getInstance().cleanBillingPage();
        ASToast.showShortToast("返回");
        var true: return
    }
}


