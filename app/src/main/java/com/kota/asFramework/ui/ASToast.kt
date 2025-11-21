package com.kota.asFramework.ui

import android.widget.Toast
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.asFramework.thread.ASRunner

object ASToast {
    private var previousToast: Toast? = null
    @JvmStatic
    fun showShortToast(aToastMessage: String?) {
        object : ASRunner() {
            // from class: com.kota.asFramework.ui.ASToast.1
            // com.kota.asFramework.thread.ASRunner
            override fun run() {
                if (previousToast != null) {
                    previousToast?.cancel()
                }
                previousToast = Toast.makeText(
                    ASNavigationController.currentController,
                    aToastMessage,
                    Toast.LENGTH_SHORT
                )
                previousToast?.show()
            }
        }.runInMainThread()
    }

    @JvmStatic
    fun showLongToast(aToastMessage: String?) {
        object : ASRunner() {
            // from class: com.kota.asFramework.ui.ASToast.2
            // com.kota.asFramework.thread.ASRunner
            override fun run() {
                if (previousToast != null) {
                    previousToast?.cancel()
                }
                previousToast = Toast.makeText(
                    ASNavigationController.currentController,
                    aToastMessage,
                    Toast.LENGTH_LONG
                )
                previousToast?.show()
            }
        }.runInMainThread()
    }
}