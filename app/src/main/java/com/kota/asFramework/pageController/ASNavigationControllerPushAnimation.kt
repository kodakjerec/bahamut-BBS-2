package com.kota.asFramework.pageController

import android.view.animation.Animation
import com.kota.asFramework.thread.ASCoroutine

open class ASNavigationControllerPushAnimation(
    private val sourceViewController: ASViewController?,
    private val targetViewController: ASViewController?
) {
    private var isSourceFinished = false
    private var isTargetFinished = false
    private var isFinished = false

    fun start(animated: Boolean) {
        if (!animated) {
            finish()
        } else {
            animate()
        }
    }

    private fun animate() {
        val animationDuration = 250
        if (this.sourceViewController != null && this.sourceViewController.pageView != null) {
            val sourceAnimation = ASPageAnimation.fadeOutToLeftAnimation
            sourceAnimation?.duration = animationDuration.toLong()
            sourceAnimation?.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                }

                // android.view.animation.Animation.AnimationListener
                override fun onAnimationRepeat(animation: Animation?) {
                }

                // android.view.animation.Animation.AnimationListener
                override fun onAnimationEnd(animation: Animation?) {
                    synchronized(this@ASNavigationControllerPushAnimation) {
                        this@ASNavigationControllerPushAnimation.sourceViewController.pageView?.onPageAnimationFinished()
                        this@ASNavigationControllerPushAnimation.isSourceFinished = true
                        this@ASNavigationControllerPushAnimation.checkFinished()
                    }
                }
            })
            this.sourceViewController.pageView?.onPageAnimationStart()
            this.sourceViewController.pageView?.startAnimation(sourceAnimation)
        } else {
            synchronized(this) {
                this.isSourceFinished = true
                checkFinished()
            }
        }
        if (this.targetViewController != null && this.targetViewController.pageView != null) {
            val targetAnimation = ASPageAnimation.fadeInFromRightAnimation
            targetAnimation?.duration = animationDuration.toLong()
            targetAnimation?.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                }

                // android.view.animation.Animation.AnimationListener
                override fun onAnimationRepeat(animation: Animation?) {
                }

                // android.view.animation.Animation.AnimationListener
                override fun onAnimationEnd(animation: Animation?) {
                    synchronized(this@ASNavigationControllerPushAnimation) {
                        this@ASNavigationControllerPushAnimation.targetViewController.pageView?.onPageAnimationFinished()
                        this@ASNavigationControllerPushAnimation.isTargetFinished = true
                        this@ASNavigationControllerPushAnimation.checkFinished()
                    }
                }
            })
            this.targetViewController.pageView?.onPageAnimationStart()
            this.targetViewController.pageView?.startAnimation(targetAnimation)
            return
        }
        synchronized(this) {
            this.isTargetFinished = true
            checkFinished()
        }
    }

    private fun checkFinished() {
        if (this.isSourceFinished && this.isTargetFinished) {
            finish()
        }
    }

    private fun finish() {
        if (!this.isFinished) {
            ASCoroutine.runOnMain {
                this@ASNavigationControllerPushAnimation.onAnimationFinished()
            }
            this.isFinished = true
        }
    }

    open fun onAnimationFinished() {
    }
}