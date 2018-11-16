package com.bori.hipe.controllers.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation


class WaveView : View {

    private val rectF: RectF
    private val paint: Paint
    private var centerX: Int = 0
    private var centerY: Int = 0
    private val gonePX = 0f
    private var min: Float = 0.toFloat()

    private var myAnimation: MyAnimation? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        centerX = width / 2
        centerY = height / 2

        min = Math.min(centerX, centerY).toFloat()

        paint = Paint()
        paint.color = 0xfffd3b51.toInt()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0f
        rectF = RectF(centerX - gonePX, centerY - gonePX, centerX + gonePX, centerY + gonePX)

        myAnimation = MyAnimation()
        myAnimation!!.duration = 500
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.d(TAG, "WaveView.onDraw")

        if (centerX == 0) {
            centerX = width / 2
            centerY = height / 2
            min = Math.min(centerX, centerY).toFloat()

        }
        canvas.drawOval(rectF, paint)
    }

    fun startAnim() {
        Log.d(TAG, "WaveView.startAnim")

        visibility = View.VISIBLE
        myAnimation!!.reset()
        startAnimation(myAnimation)
    }

    fun onUpdate(deltaSecs: Float) {
        Log.d(TAG, "WaveView.onUpdate")

        val d = min * deltaSecs
        rectF.left = centerX - d
        rectF.top = centerY - d
        rectF.right = centerX + d
        rectF.bottom = centerY + d

        if (deltaSecs >= 0.5f)
            paint.strokeWidth = (1 - deltaSecs) * min
        else
            paint.strokeWidth = deltaSecs * min

        requestLayout()
    }

    private inner class MyAnimation internal constructor() : Animation(), Animation.AnimationListener {

        init {
            setAnimationListener(this)
        }

        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            super.applyTransformation(interpolatedTime, t)
            Log.d(TAG, "MyAnimation.applyTransformation")

            onUpdate(interpolatedTime)
        }

        override fun onAnimationStart(animation: Animation) {
            Log.d(TAG, "MyAnimation.onAnimationStart")
        }

        override fun onAnimationEnd(animation: Animation) {
            Log.d(TAG, "MyAnimation.onAnimationEnd")

            visibility = View.GONE
        }

        override fun onAnimationRepeat(animation: Animation) {
            Log.d(TAG, "MyAnimation.onAnimationRepeat")

            visibility = View.GONE
        }
    }

    companion object {
        private const val TAG = "WaveView"
    }

}
