package com.kota.Bahamut.Pages;

import static com.kota.Bahamut.Service.CommonFunctions.getContextString;
import static com.kota.Bahamut.Service.MyBillingClient.getBillingClient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchaseHistoryParams;
import com.kota.ASFramework.Thread.ASRunner;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.PageContainer;
import com.kota.Bahamut.R;
import com.kota.Telnet.UserSettings;
import com.kota.TelnetUI.TelnetPage;
import com.kota.TelnetUI.TextView.TelnetTextViewNormal;

import java.util.ArrayList;
import java.util.List;

public class BillingPage extends TelnetPage {
    private BillingClient billingClient;
    UserSettings _settings;
    @Override // com.kota.ASFramework.PageController.ASViewController
    public int getPageType() {
        return 18;
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
        billingClient = getBillingClient();
        getProductList();
        _settings = new UserSettings(getContext());

        // 檢查已購買
        Button button1 = (Button)findViewById(R.id.button_checkPurchaseQuery);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPurchaseHistoryQuery();
            }
        });
        button1.performClick();
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
                new ProductDetailsResponseListener() {
                    @Override
                    public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> list) {
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
                                        btn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                ArrayList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = new ArrayList<>();

                                                productDetailsParamsList.add(BillingFlowParams.ProductDetailsParams.newBuilder()
                                                        .setProductDetails(product)
                                                        .build());

                                                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                                        .setProductDetailsParamsList(productDetailsParamsList)
                                                        .setIsOfferPersonalized(true)
                                                        .build();

                                                // Launch the billing flow
                                                BillingResult billingResult1 = billingClient.launchBillingFlow(activity, billingFlowParams);
                                            }
                                        });
                                    }
                                }
                            }.runInMainThread();
                        }
                    }
                });
    }

    // 重新確認已購買的商品, 並填入金額
    public void checkPurchaseHistoryQuery() {
        billingClient.queryPurchaseHistoryAsync(
                QueryPurchaseHistoryParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                new PurchaseHistoryResponseListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onPurchaseHistoryResponse(@NonNull BillingResult billingResult, @Nullable List<PurchaseHistoryRecord> list) {
                        // 統計總金額
                        int totalValue = 0;
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                            // 如果有成功購買紀錄, 但是沒有開啟VIP, 則開啟
                            if (list.toArray().length>0) {
                                if (!_settings.getPropertiesVIP()) {
                                    _settings.setPropertiesVIP(true);
                                }
                                for(PurchaseHistoryRecord purchaseHistoryRecord: list) {
                                    String productName = purchaseHistoryRecord.getProducts().get(0);
                                    if (productName.contains("com.kota.billing.")) {
                                        String strPrice = productName.replaceAll("com.kota.billing.", "");
                                        Integer donatePrice = Integer.parseInt(strPrice);
                                        Integer donateQuality = purchaseHistoryRecord.getQuantity();
                                        totalValue += donatePrice * donateQuality;
                                    }
                                }
                                ASToast.showShortToast(getContextString(R.string.billing_page_result_success));
                            }
                        } else {
                            ASToast.showShortToast(getContextString(R.string.billing_page_result_error_check) + billingResult.getResponseCode());
                        }

                        TelnetTextViewNormal textView = (TelnetTextViewNormal) findViewById(R.id.BillingPage_already_billing_value);
                        textView.setText(Integer.toString(totalValue));
                    }
                }
        );
    }

    public boolean onReceivedGestureRight() {
        onBackPressed();
        PageContainer.getInstance().cleanBillingPage();
        ASToast.showShortToast("返回");
        return true;
    }

}
