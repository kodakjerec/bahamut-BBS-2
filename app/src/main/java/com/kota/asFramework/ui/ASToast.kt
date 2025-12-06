package com.kota.asFramework.ui

import android.widget.Toast
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.asFramework.thread.ASCoroutine

object ASToast {
    private var previousToast: Toast? = null
    @JvmStatic
    fun showShortToast(aToastMessage: String?) {
        ASCoroutine.runOnMain {
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
    }

    @JvmStatic
    fun showLongToast(aToastMessage: String?) {
        ASCoroutine.runOnMain {
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
    }
}