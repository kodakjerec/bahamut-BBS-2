package com.kota.asFramework.pageController

import android.view.View
import android.view.ViewGroup
import com.kota.asFramework.thread.ASRunner
import com.kota.asFramework.thread.ASRunner.Companion.runInNewThread

class ASViewRemover(private val parentViewGroup: ViewGroup?, private val targetView: View?) {
    fun start() {
        runInNewThread {
            try {
                Thread.sleep(1000L)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            this@ASViewRemover.remove()
        }
    }

    private fun remove() {
        object : ASRunner() {
            override fun run() {
                if (this@ASViewRemover.parentViewGroup != null && this@ASViewRemover.targetView != null) {
                    this@ASViewRemover.parentViewGroup.removeView(this@ASViewRemover.targetView)
                }
            }
        }.runInMainThread()
    }

    companion object {
        fun remove(parentView: ViewGroup?, targetView: View?) {
            val remover = ASViewRemover(parentView, targetView)
            remover.start()
        }
    }
}