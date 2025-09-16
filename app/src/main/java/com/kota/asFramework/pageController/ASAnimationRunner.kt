package com.kota.asFramework.pageController

import android.view.View
import android.view.animation.Animation
import com.kota.asFramework.thread.ASRunner
import com.kota.asFramework.thread.ASRunner.Companion.runInNewThread

/* loaded from: classes.dex */
abstract class ASAnimationRunner(animation: Animation?) {
    var _animation: Animation? = null

    abstract val targetView: View?

    abstract fun onAnimationStartFail()

    init {
        this._animation = animation
    }

    fun start() {
        runInNewThread(Runnable {
            var i = 0
            var success = false
            while (true) {
                if (i >= 10) {
                    break
                }
                val target_view = this@ASAnimationRunner.targetView
                if (target_view != null) {
                    this@ASAnimationRunner.animate()
                    success = true
                    break
                }
                i++
                try {
                    Thread.sleep(10L)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            if (!success) {
                this@ASAnimationRunner.fail()
            }
        })
    }

    private fun fail() {
        object : ASRunner() {
            // from class: com.kota.ASFramework.PageController.ASAnimationRunner.2
            // com.kota.ASFramework.Thread.ASRunner
            public override fun run() {
                this@ASAnimationRunner.onAnimationStartFail()
            }
        }.runInMainThread()
    }

    private fun animate() {
        object : ASRunner() {
            // from class: com.kota.ASFramework.PageController.ASAnimationRunner.3
            // com.kota.ASFramework.Thread.ASRunner
            public override fun run() {
                val target_view = this@ASAnimationRunner.targetView
                if (target_view != null) {
                    target_view.startAnimation(this@ASAnimationRunner._animation)
                }
            }
        }.runInMainThread()
    }
}