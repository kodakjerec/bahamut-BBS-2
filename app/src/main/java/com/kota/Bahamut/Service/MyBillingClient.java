package com.kota.Bahamut.Service;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryPurchaseHistoryParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.kota.ASFramework.UI.ASToast;
import com.kota.Bahamut.R;
import com.kota.Telnet.UserSettings;

import java.util.List;

public final class MyBillingClient {
    static UserSettings _settings;
    public static  BillingClient billingClient;
    @SuppressLint("StaticFieldLeak")
    private static Context myContext;

    // 購買結果
    private static final PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                for (Purchase purchase: list) {
                    handlePurchase(purchase);
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                ASToast.showShortToast(myContext.getString(R.string.billing_page_result_cancel));
            } else {
                ASToast.showShortToast(myContext.getString(R.string.billing_page_result_error));
            }
        }
    };
    // 確認購買交易，且程式已授予使用者商品
    private static void handlePurchase(Purchase purchases) {
        if (!purchases.isAcknowledged()) {
            billingClient.acknowledgePurchase(AcknowledgePurchaseParams
                    .newBuilder()
                    .setPurchaseToken(purchases.getPurchaseToken())
                    .build(), billingResult -> {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    for (String pur : purchases.getProducts()) {
                        //Calling Consume to consume the current purchase
                        // so user will be able to buy same product again
                        ConsumePurchase(purchases);
                    }
                }
            });
        }
    }

    // 購買後要回應訊息給google和使用者
    private static void ConsumePurchase(Purchase purchase) {
        ConsumeParams consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();

        ConsumeResponseListener consumeResponseListener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String s) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    if (!_settings.getPropertiesVIP()) {
                        _settings.setPropertiesVIP(true);
                    }
                    ASToast.showShortToast(myContext.getString(R.string.billing_page_result_success));
                }
            }
        };

        billingClient.consumeAsync(consumeParams, consumeResponseListener);
    }

    // 重新確認已購買的商品
    public static void checkPurchaseHistoryQuery() {
        UserSettings userSettings = new UserSettings(myContext);
        billingClient.queryPurchaseHistoryAsync(
                QueryPurchaseHistoryParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                new PurchaseHistoryResponseListener() {
                    @Override
                    public void onPurchaseHistoryResponse(@NonNull BillingResult billingResult, @Nullable List<PurchaseHistoryRecord> list) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                            // 如果有成功購買紀錄, 但是沒有開啟VIP, 則開啟
                            if (list.toArray().length>0) {
                                if (!_settings.getPropertiesVIP()) {
                                    _settings.setPropertiesVIP(true);
                                }
                            }
                        }
                    }
                }
        );
    }

    // 處理應用程式外的購買交易
    public static void checkPurchase() {
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(),
                new PurchasesResponseListener() {
                    @Override
                    public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                            for (Purchase purchase: list) {
                                handlePurchase(purchase);
                            }
                        }
                    }
                }
        );
    }

    public static void initBillingClient(Context fromContext) {
        myContext = fromContext;
        billingClient = BillingClient.newBuilder(myContext)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();
        _settings = new UserSettings(myContext);


        // 商店付款建立
        // initial
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                System.out.print("Billing Service disconnected");
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

            }
        });
    }

    public static BillingClient getBillingClient() {
        return billingClient;
    }

    public static void closeBillingClient() {
        billingClient.endConnection();
    }
}
