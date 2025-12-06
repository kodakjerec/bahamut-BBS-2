package com.kota.asFramework.pageController

import android.view.animation.Animation
import com.kota.asFramework.thread.ASCoroutine

open class ASNavigationControllerPopAnimation(
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
            val fadeOutAnimation = ASPageAnimation.fadeOutTtRightAnimation
            fadeOutAnimation?.duration = animationDuration.toLong()
            fadeOutAnimation?.setAnimationListener(object : Animation.AnimationListener {
                // from class: com.kota.asFramework.pageController.ASNavigationControllerPopAnimation.1
                // android.view.animation.Animation.AnimationListener
                override fun onAnimationStart(animation: Animation?) {
                }

                // android.view.animation.Animation.AnimationListener
                override fun onAnimationRepeat(animation: Animation?) {
                }

                // android.view.animation.Animation.AnimationListener
                override fun onAnimationEnd(animation: Animation?) {
                    synchronized(this@ASNavigationControllerPopAnimation) {
                        this@ASNavigationControllerPopAnimation.sourceViewController.pageView?.onPageAnimationFinished()
                        this@ASNavigationControllerPopAnimation.isSourceFinished = true
                        this@ASNavigationControllerPopAnimation.checkFinished()
                    }
                }
            })
            this.sourceViewController.pageView?.onPageAnimationStart()
            this.sourceViewController.pageView?.startAnimation(fadeOutAnimation)
        } else {
            synchronized(this) {
                this.isSourceFinished = true
                checkFinished()
            }
        }
        if (this.targetViewController != null && this.targetViewController.pageView != null) {
            val targetAnimation = ASPageAnimation.fadeInFromLeftAnimation
            targetAnimation?.duration = animationDuration.toLong()
            targetAnimation?.setAnimationListener(object : Animation.AnimationListener {
                // from class: com.kota.asFramework.pageController.ASNavigationControllerPopAnimation.2
                // android.view.animation.Animation.AnimationListener
                override fun onAnimationStart(animation: Animation?) {
                }

                // android.view.animation.Animation.AnimationListener
                override fun onAnimationRepeat(animation: Animation?) {
                }

                // android.view.animation.Animation.AnimationListener
                override fun onAnimationEnd(animation: Animation?) {
                    synchronized(this@ASNavigationControllerPopAnimation) {
                        this@ASNavigationControllerPopAnimation.targetViewController.pageView?.onPageAnimationFinished()
                        this@ASNavigationControllerPopAnimation.isTargetFinished = true
                        this@ASNavigationControllerPopAnimation.checkFinished()
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
                this@ASNavigationControllerPopAnimation.onAnimationFinished()
            }
            this.isFinished = true
        }
    }

    open fun onAnimationFinished() {
    }
}