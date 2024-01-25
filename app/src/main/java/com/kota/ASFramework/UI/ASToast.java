package com.kota.ASFramework.UI;

import android.widget.Toast;
import com.kota.ASFramework.PageController.ASNavigationController;
import com.kota.ASFramework.Thread.ASRunner;

/* loaded from: classes.dex */
public class ASToast {
    public static void showShortToast(final String aToastMessage) {
        new ASRunner() { // from class: com.kumi.ASFramework.UI.ASToast.1
            @Override // com.kumi.ASFramework.Thread.ASRunner
            public void run() {
                Toast.makeText(ASNavigationController.getCurrentController(), aToastMessage, 0).show();
            }
        }.runInMainThread();
    }

    public static void showLongToast(final String aToastMessage) {
        new ASRunner() { // from class: com.kumi.ASFramework.UI.ASToast.2
            @Override // com.kumi.ASFramework.Thread.ASRunner
            public void run() {
                Toast.makeText(ASNavigationController.getCurrentController(), aToastMessage, 1).show();
            }
        }.runInMainThread();
    }
}