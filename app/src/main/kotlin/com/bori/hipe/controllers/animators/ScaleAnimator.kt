package com.bori.hipe.controllers.animators

import android.util.Log
import android.view.View
import android.view.animation.ScaleAnimation

/**
 * Created by pasha on 28.11.2016.
 */

class ScaleAnimator(fromX: Float, toX: Float, fromY: Float, toY: Float, duration: Long) {

    private companion object {
        private const val TAG = "ScaleAnimator"
    }

    private val scaleAnimation: ScaleAnimation = ScaleAnimation(fromX, toX, fromY, toY)

    init {
        scaleAnimation.fillAfter = true
        scaleAnimation.duration = duration
    }

    fun startScaleAnimation(v: View) {
        Log.d(TAG, "ScaleAnimator.startScaleAnimation")
        scaleAnimation.reset()
        v.startAnimation(scaleAnimation)
    }

}
