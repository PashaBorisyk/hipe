package com.bori.hipe.controllers.animators

import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation

import com.bori.hipe.controllers.animators.TabViewBackgroundAnimation.TYPE.SCALE_OUT

private const val TAG = "BackgroundAnimation"

class TabViewBackgroundAnimation(

        val type: TYPE = SCALE_OUT,
        val scaleTimes: Float = 0f,
        _duration: Long = 200,
        _view: View? = null

) : Animation(), Animation.AnimationListener {

    var animatedView = _view

    init {
        setAnimationListener(this)
        duration = _duration
    }

    enum class TYPE {
        SCALE_IN,
        SCALE_OUT
    }

    override fun applyTransformation(interpolatedTime_: Float, t: Transformation) {

        var interpolatedTime = interpolatedTime_
        super.applyTransformation(interpolatedTime, t)

        if (type == SCALE_OUT)
            interpolatedTime = 1 - interpolatedTime

        interpolatedTime *= scaleTimes
        animatedView?.scaleY = interpolatedTime
        animatedView?.scaleX = interpolatedTime

    }

    override fun onAnimationStart(animation: Animation) {
        Log.e(TAG, "onAnimationStart: " + animatedView?.pivotY + " " + animatedView?.pivotX)
    }

    override fun onAnimationEnd(animation: Animation) {

    }

    override fun onAnimationRepeat(animation: Animation) {

    }

}