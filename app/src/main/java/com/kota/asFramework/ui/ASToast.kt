package com.kota.asFramework.ui

import android.widget.Toast
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.asFramework.thread.ASRunner

/* loaded from: classes.dex */
object ASToast {
    private var previous_toast: Toast? = null
    @JvmStatic
    fun showShortToast(aToastMessage: String?) {
        object : ASRunner() {
            // from class: com.kota.ASFramework.UI.ASToast.1
            // com.kota.ASFramework.Thread.ASRunner
            override fun run() {
                if (previous_toast != null) {
                    previous_toast!!.cancel()
                }
                previous_toast = Toast.makeText(
                    ASNavigationController.getCurrentController(),
                    aToastMessage,
                    Toast.LENGTH_SHORT
                )
                previous_toast!!.show()
            }
        }.runInMainThread()
    }

    @JvmStatic
    fun showLongToast(aToastMessage: String?) {
        object : ASRunner() {
            // from class: com.kota.ASFramework.UI.ASToast.2
            // com.kota.ASFramework.Thread.ASRunner
            override fun run() {
                if (previous_toast != null) {
                    previous_toast!!.cancel()
                }
                previous_toast = Toast.makeText(
                    ASNavigationController.getCurrentController(),
                    aToastMessage,
                    Toast.LENGTH_LONG
                )
                previous_toast!!.show()
            }
        }.runInMainThread()
    }
}