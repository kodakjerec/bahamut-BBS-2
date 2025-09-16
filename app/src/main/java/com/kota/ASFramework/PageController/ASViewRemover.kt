package com.kota.ASFramework.PageController

import android.view.View
import android.view.ViewGroup
import com.kota.ASFramework.Thread.ASRunner
import com.kota.ASFramework.Thread.ASRunner.Companion.runInNewThread

/* loaded from: classes.dex */
class ASViewRemover(private val _parent_view: ViewGroup?, private val _target_view: View?) {
    fun start() {
        runInNewThread(Runnable {
            try {
                Thread.sleep(1000L)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            this@ASViewRemover.remove()
        })
    }

    private fun remove() {
        object : ASRunner() {
            // from class: com.kota.ASFramework.PageController.ASViewRemover.2
            // com.kota.ASFramework.Thread.ASRunner
            public override fun run() {
                if (this@ASViewRemover._parent_view != null && this@ASViewRemover._target_view != null) {
                    this@ASViewRemover._parent_view.removeView(this@ASViewRemover._target_view)
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