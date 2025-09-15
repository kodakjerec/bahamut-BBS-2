package com.kota.ASFramework.PageController

import android.view.View
import android.view.animation.Animation
import com.kota.ASFramework.Thread.ASRunner

/* loaded from: classes.dex */
abstract class ASAnimationRunner(private val _animation: Animation) {

    abstract fun getTargetView(): View?

    abstract fun onAnimationStartFail()

    fun start() {
        ASRunner.runInNewThread {
            var success = false
            for (i in 0 until 10) {
                val targetView = getTargetView()
                if (targetView != null) {
                    animate()
                    success = true
                    break
                }
                try {
                    Thread.sleep(10L)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            if (!success) {
                fail()
            }
        }
    }

    private fun fail() {
        object : ASRunner() {
            override fun run() {
                onAnimationStartFail()
            }
        }.runInMainThread()
    }

    private fun animate() {
        object : ASRunner() {
            override fun run() {
                val targetView = getTargetView()
                targetView?.startAnimation(_animation)
            }
        }.runInMainThread()
    }
}
