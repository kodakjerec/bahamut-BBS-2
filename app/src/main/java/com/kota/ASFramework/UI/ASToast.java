package com.kota.ASFramework.UI;

import android.widget.Toast;
import com.kota.ASFramework.PageController.ASNavigationController;
import com.kota.ASFramework.Thread.ASRunner;

public class ASToast {
    public static void showShortToast(final String aToastMessage) {
        new ASRunner() {
            public void run() {
                Toast.makeText(ASNavigationController.getCurrentController(), aToastMessage, 0).show();
            }
        }.runInMainThread();
    }

    public static void showLongToast(final String aToastMessage) {
        new ASRunner() {
            public void run() {
                Toast.makeText(ASNavigationController.getCurrentController(), aToastMessage, 1).show();
            }
        }.runInMainThread();
    }
}
