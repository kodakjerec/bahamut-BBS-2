package com.kota.ASFramework.PageController

import android.view.animation.Animation
import com.kota.ASFramework.Thread.ASRunner

/* loaded from: classes.dex */
open class ASNavigationControllerPopAnimation(
    private val _source_controller: ASViewController?,
    private val _target_controller: ASViewController?
) {
    private var _source_finished = false
    private var _target_finished = false
    private var _finished = false

    fun start(animated: Boolean) {
        if (!animated) {
            finish()
        } else {
            animate()
        }
    }

    private fun animate() {
        val animation_duration = 250
        if (this._source_controller != null && this._source_controller.getPageView() != null) {
            val _source_animation = ASPageAnimation.getFadeOutTtRightAnimation()
            _source_animation.setDuration(animation_duration.toLong())
            _source_animation.setAnimationListener(object : Animation.AnimationListener {
                // from class: com.kota.ASFramework.PageController.ASNavigationControllerPopAnimation.1
                // android.view.animation.Animation.AnimationListener
                override fun onAnimationStart(animation: Animation?) {
                }

                // android.view.animation.Animation.AnimationListener
                override fun onAnimationRepeat(animation: Animation?) {
                }

                // android.view.animation.Animation.AnimationListener
                override fun onAnimationEnd(animation: Animation?) {
                    synchronized(this@ASNavigationControllerPopAnimation) {
                        this@ASNavigationControllerPopAnimation._source_controller.getPageView()
                            .onPageAnimationFinished()
                        this@ASNavigationControllerPopAnimation._source_finished = true
                        this@ASNavigationControllerPopAnimation.checkFinished()
                    }
                }
            })
            this._source_controller.getPageView().onPageAnimationStart()
            this._source_controller.getPageView().startAnimation(_source_animation)
        } else {
            synchronized(this) {
                this._source_finished = true
                checkFinished()
            }
        }
        if (this._target_controller != null && this._target_controller.getPageView() != null) {
            val _target_animation = ASPageAnimation.getFadeInFromLeftAnimation()
            _target_animation.setDuration(animation_duration.toLong())
            _target_animation.setAnimationListener(object : Animation.AnimationListener {
                // from class: com.kota.ASFramework.PageController.ASNavigationControllerPopAnimation.2
                // android.view.animation.Animation.AnimationListener
                override fun onAnimationStart(animation: Animation?) {
                }

                // android.view.animation.Animation.AnimationListener
                override fun onAnimationRepeat(animation: Animation?) {
                }

                // android.view.animation.Animation.AnimationListener
                override fun onAnimationEnd(animation: Animation?) {
                    synchronized(this@ASNavigationControllerPopAnimation) {
                        this@ASNavigationControllerPopAnimation._target_controller.getPageView()
                            .onPageAnimationFinished()
                        this@ASNavigationControllerPopAnimation._target_finished = true
                        this@ASNavigationControllerPopAnimation.checkFinished()
                    }
                }
            })
            this._target_controller.getPageView().onPageAnimationStart()
            this._target_controller.getPageView().startAnimation(_target_animation)
            return
        }
        synchronized(this) {
            this._target_finished = true
            checkFinished()
        }
    }

    private fun checkFinished() {
        if (this._source_finished && this._target_finished) {
            finish()
        }
    }

    private fun finish() {
        if (!this._finished) {
            object : ASRunner() {
                // from class: com.kota.ASFramework.PageController.ASNavigationControllerPopAnimation.3
                // com.kota.ASFramework.Thread.ASRunner
                public override fun run() {
                    this@ASNavigationControllerPopAnimation.onAnimationFinished()
                }
            }.runInMainThread()
            this._finished = true
        }
    }

    open fun onAnimationFinished() {
    }
}