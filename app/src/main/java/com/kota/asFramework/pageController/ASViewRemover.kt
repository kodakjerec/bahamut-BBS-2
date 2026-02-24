package com.kota.asFramework.pageController

import android.view.View
import android.view.ViewGroup
import com.kota.asFramework.thread.ASCoroutine

class ASViewRemover(private val parentViewGroup: ViewGroup?, private val targetView: View?) {
    fun start() {
        ASCoroutine.runInNewCoroutine {
            try {
                Thread.sleep(1000L)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            this@ASViewRemover.remove()
        }
    }

    private fun remove() {
        ASCoroutine.ensureMainThread {
            if (this@ASViewRemover.parentViewGroup != null && this@ASViewRemover.targetView != null) {
                this@ASViewRemover.parentViewGroup.removeView(this@ASViewRemover.targetView)
            }
        }
    }

    companion object {
        fun remove(parentView: ViewGroup?, targetView: View?) {
            val remover = ASViewRemover(parentView, targetView)
            remover.start()
        }
    }
}