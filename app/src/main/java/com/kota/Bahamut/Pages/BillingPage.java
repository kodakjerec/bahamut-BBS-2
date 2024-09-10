package com.kota.Bahamut.Pages;

import static com.kota.Bahamut.Service.CommonFunctions.getContextString;

import android.app.Activity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.kota.ASFramework.Thread.ASRunner;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.BahamutPage;
import com.kota.Bahamut.PageContainer;
import com.kota.Bahamut.Pages.Theme.ThemeFunctions;
import com.kota.Bahamut.R;
import com.kota.Bahamut.Service.MyBillingClient;
import com.kota.TelnetUI.TelnetPage;

import java.util.ArrayList;

public class BillingPage extends TelnetPage {
    private BillingClient billingClient;
    @Override // com.kota.ASFramework.PageController.ASViewController
    public int getPageType() {
        return BahamutPage.BAHAMUT_BILLING;
    }

    @Override
    public int getPageLayout() {
        return R.layout.billing_page;
    }

    @Override // com.kota.TelnetUI.TelnetPage
    public boolean isPopupPage() {
        return true;
    }

    @Override // com.kota.TelnetUI.TelnetPage
    public boolean isKeepOnOffline() {
        return true;
    }

    @Override
    public void onPageDidLoad() {
        billingClient = MyBillingClient.billingClient;
        getProductList();

        MyBillingClient.checkPurchaseHistoryQuery();

        // 檢查已購買
        Button button1 = (Button)findViewById(R.id.button_checkPurchaseQuery);
        button1.setOnClickListener(view -> {
            MyBillingClient.checkPurchaseHistoryCloud(qty -> {
                String totalMoney = String.valueOf(qty * 90);
                TextView textView = (TextView) findViewById(R.id.BillingPage_already_billing_value);
                textView.setText(totalMoney);
                return null;
            });
            ASToast.showShortToast(getContextString(R.string.billing_page_result_success));
        });
        button1.performClick();

        // 替換外觀
        new ThemeFunctions().layoutReplaceTheme((LinearLayout)findViewById(R.id.toolbar));
    }

    // 取得商品清單
    public void getProductList() {
        Activity activity = getNavigationController();

        // The BillingClient is ready. You can query purchases here.
        ArrayList<QueryProductDetailsParams.Product> productList = new ArrayList<>();
        productList.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId("com.kota.billing.90")
                .setProductType(BillingClient.ProductType.INAPP)
                .build());
        // list products
        QueryProductDetailsParams queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                .setProductList(productList).build();

        billingClient.queryProductDetailsAsync(queryProductDetailsParams,
                (billingResult, list) -> {
                    // check billingResult
                    // process returned productDetailsList
                    for(ProductDetails product: list) {
                        String btnName = product.getProductId().replace("com.kota.billing.","");
                        new ASRunner() {
                            @Override
                            public void run() {

                                Button btn = null;
                                if (btnName.equals("90")) {
                                    btn = (Button) findViewById(R.id.button_90);
                                    btn.setEnabled(true);
                                    btn.setText(product.getName());
                                }
                                if (btn!=null) {
                                    btn.setOnClickListener(view -> {
                                        ArrayList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = new ArrayList<>();

                                        productDetailsParamsList.add(BillingFlowParams.ProductDetailsParams.newBuilder()
                                                .setProductDetails(product)
                                                .build());

                                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
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
                });
    }

    @Override
    public boolean onReceivedGestureRight() {
        onBackPressed();
        PageContainer.getInstance().cleanBillingPage();
        ASToast.showShortToast("返回");
        return true;
    }
}
