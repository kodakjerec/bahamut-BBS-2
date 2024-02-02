package com.kota.ASFramework.UI;

import android.widget.Toast;
import com.kota.ASFramework.PageController.ASNavigationController;
import com.kota.ASFramework.Thread.ASRunner;

/* loaded from: classes.dex */
public class ASToast {
    private static Toast previous_toast;
    public static void showShortToast(final String aToastMessage) {
        new ASRunner() { // from class: com.kota.ASFramework.UI.ASToast.1
            @Override // com.kota.ASFramework.Thread.ASRunner
            public void run() {
                if (previous_toast != null) {
                    previous_toast.cancel();
                }
                previous_toast = Toast.makeText(ASNavigationController.getCurrentController(), aToastMessage, Toast.LENGTH_SHORT);
                previous_toast.show();
            }
        }.runInMainThread();
    }

    public static void showLongToast(final String aToastMessage) {
        new ASRunner() { // from class: com.kota.ASFramework.UI.ASToast.2
            @Override // com.kota.ASFramework.Thread.ASRunner
            public void run() {
                if (previous_toast != null) {
                    previous_toast.cancel();
                }
                previous_toast = Toast.makeText(ASNavigationController.getCurrentController(), aToastMessage, Toast.LENGTH_LONG);
                previous_toast.show();
            }
        }.runInMainThread();
    }
}