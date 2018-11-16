package com.bori.hipe.controllers.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageView

private const val TAG = "SpinningScaleView"
private const val DEGREE_360 = Math.PI.toFloat() * 2

class SpinningScaleView : ImageView {

    companion object {
        var catched = false
    }

    private val spinningAndScaleContext = SpinningAndScaleSingletonContext()
    var test = false
    private var scaleOutListener: AnimatorListenerAdapter? = null
    var run = true
        private set

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {

        visibility = if (test)
            View.VISIBLE
        else
            View.GONE

    }

    fun catchContext() = spinningAndScaleContext

    private fun startSpinningAndScaling(cycleDuration: Long) {
        Log.d(TAG, "SpinningScaleView.startSpinningAndScaling")

        run = true

        scaleOutListener = object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                Log.d(TAG, "onAnimationEnd: ")

                rotation = 0f
                doRotation(cycleDuration)

            }

        }

        doRotation(cycleDuration)

    }

    private fun doRotation(cycleDuration: Long) {
        Log.d(TAG, "SpinningScaleView.doRotation")

        if (run) {
            animate()
                    .rotation(359f)
                    .setDuration(cycleDuration)
                    .setListener(rotationListener)
                    .start()
        }
    }

    private var rotationListener: AnimatorListenerAdapter = object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            super.onAnimationEnd(animation)
            Log.d(TAG, "SpinningScaleView.onAnimationEnd")

            if (run) {
                animate()
                        .scaleX(1.3f)
                        .scaleY(1.3f)
                        .setListener(scaleInListener)
            }
        }
    }

    private var scaleInListener: AnimatorListenerAdapter = object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            super.onAnimationEnd(animation)
            Log.d(TAG, "SpinningScaleView.onAnimationEnd")
            if (run) {
                animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setListener(scaleOutListener)
            }
        }
    }

    inner class SpinningAndScaleSingletonContext internal constructor() {

        private var backingView: View? = null

        fun start(duration: Long, withView: View?): SpinningAndScaleSingletonContext {
            Log.d(TAG, "SpinningAndScaleSingletonContext.start")


            if (!catched) {
                backingView = withView
                catched = true
                this@SpinningScaleView.startSpinningAndScaling(duration)
                visibility = View.VISIBLE

                val animator = animate()
                animator.duration = duration
                animator.setListener(null)
                animator.alpha(1f).start()

                withView?.apply {

                    val anim = this.animate()
                    anim.duration = duration
                    anim.alpha(1f).start()

                }

            } else
                throw IllegalStateException("Context should be released before running again!")
            return this
        }

        fun stopAndRelease() {
            Log.d(TAG, "SpinningAndScaleSingletonContext.stopAndRelease")

            catched = false

            animate()
                    .alpha(0f)
                    .setListener(animatorListener)
                    .start()

            backingView?.apply {

                val anim = this.animate()
                anim.alpha(0f).start()

            }
        }

        private val animatorListener = object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                Log.d(TAG, "SpinningAndScaleSingletonContext.onAnimationEnd")

                run = false
                visibility = View.GONE
                backingView?.visibility = View.GONE
            }
        }
    }
}
