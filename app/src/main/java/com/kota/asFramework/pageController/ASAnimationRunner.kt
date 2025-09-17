package com.kota.asFramework.pageController

import android.view.View
import android.view.animation.Animation
import com.kota.asFramework.thread.ASRunner
import com.kota.asFramework.thread.ASRunner.Companion.runInNewThread

abstract class ASAnimationRunner(animation: Animation?) {
    var animation: Animation? = null

    abstract val targetView: View?

    abstract fun onAnimationStartFail()

    init {
        this.animation = animation
    }

    fun start() {
        runInNewThread {
            var i = 0
            var success = false
            while (true) {
                if (i >= 10) {
                    break
                }
                val targetView1 = this@ASAnimationRunner.targetView
                if (targetView1 != null) {
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
        }
    }

    private fun fail() {
        object : ASRunner() {
            override fun run() {
                this@ASAnimationRunner.onAnimationStartFail()
            }
        }.runInMainThread()
    }

    private fun animate() {
        object : ASRunner() {
            override fun run() {
                val targetView1 = this@ASAnimationRunner.targetView
                targetView1?.startAnimation(this@ASAnimationRunner.animation)
            }
        }.runInMainThread()
    }
}