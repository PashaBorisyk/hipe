package com.bori.hipe.controllers.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import java.util.*


/**
 * Created by pasha on 15.12.2016.
 */

class BubblesView : View {

    private val randomSpeed = FloatArray(OVALS_COUNT)
    private val gonePixels = IntArray(OVALS_COUNT)
    private var currentSmallRAdiuses = FloatArray(OVALS_COUNT)
    private var currentBigRadiuses = FloatArray(OVALS_COUNT)
    private var angles = FloatArray(OVALS_COUNT)

    private var centerX = 0f
    private var centerY = 0f
    private var bigRadius = 100f
    private var smallRadius = 30f
    var speed = 1f
    var minDimension = 0f
        private set

    private var setBigRadiusWithViewSizes = false
    var isSetSmallRadiusWithViewSizes = false

    private var paint = Paint()
    private var rects = Array(OVALS_COUNT) { RectF() }
    private var random = Random()

    private var bubblesViewAnimation = BubblesViewAnimation()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {

        paint.style = Paint.Style.FILL
        paint.color = 0xFFFA386D.toInt()
        paint.alpha = 0x50
        bubblesViewAnimation.duration = 1000

    }

    fun setSetBigRadiusWithViewSizes(setBigRadiusWithViewSizes: Boolean) {
        this.setBigRadiusWithViewSizes = setBigRadiusWithViewSizes
    }

    fun setBigRadius(newRadius: Float) {
        this.bigRadius = newRadius
    }

    fun setSmallRadius(smallRadius: Float) {
        this.smallRadius = smallRadius
    }

    fun setPaintsColor(color: Int) {
        paint.color = color
    }

    val paintColor: Int
        get() = paint.color

    var paintAlpha: Int
        get() = paint.alpha
        set(alpha) {
            paint.alpha = alpha
        }

    fun setBubblesCount(count: Int) {
        OVALS_COUNT = count
    }

    fun startEffects() {

        bubblesViewAnimation.reset()
        startAnimation(bubblesViewAnimation)

    }

    val isRunning: Boolean
        get() = bubblesViewAnimation.isRun

    fun stopEffects() {
        bubblesViewAnimation.cancel()
    }

    private fun randomize(i: Int) {

        gonePixels[i] = 0
        currentSmallRAdiuses[i] = smallRadius * random.nextFloat()
        currentBigRadiuses[i] = random.nextFloat() * bigRadius
        angles[i] = random.nextFloat() * DEGREE_360
        randomSpeed[i] = (random.nextFloat() + 1.0f) * speed

    }

    private fun calculate(deltaTime: Float, k: Int) {

        val currentSmallRadius = currentSmallRAdiuses[k]
        val currentBigRadius = currentBigRadiuses[k]
        val angle = angles[k]
        gonePixels[k] += (randomSpeed[k] * deltaTime).toInt()
        val currentGonePixels = gonePixels[k].toFloat()

        if (currentGonePixels >= currentBigRadius)
            randomize(k)

        val rectF = rects[k]

        rectF.left = (centerX + currentGonePixels * Math.cos(angle.toDouble()) - currentSmallRadius).toFloat()
        rectF.top = (centerY.toDouble() - currentGonePixels * Math.sin(angle.toDouble()) - currentSmallRadius.toDouble()).toFloat()
        rectF.right = (centerX.toDouble() + currentGonePixels * Math.cos(angle.toDouble()) + currentSmallRadius.toDouble()).toFloat()
        rectF.bottom = (centerY - currentGonePixels * Math.sin(angle.toDouble()) + currentSmallRadius).toFloat()

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (centerY == 0f) {

            centerX = (width / 2).toFloat()
            centerY = (height / 2).toFloat()

            minDimension = Math.min(centerX, centerY)

            if (isSetSmallRadiusWithViewSizes)
                smallRadius = minDimension * 0.4f

            if (setBigRadiusWithViewSizes) {
                bigRadius = minDimension - smallRadius * 0.5f

            }
        }

        for (i in 0 until OVALS_COUNT)
            canvas.drawOval(rects[i], paint)

    }

    private inner class BubblesViewAnimation internal constructor() : Animation(), Animation.AnimationListener {

        var isRun: Boolean = false
            private set
        private var shouldStop: Boolean = false
        private var current: Long = 0
        private var delta: Float = 0.toFloat()
        private var prev: Long = 0
        internal var repeatCount: Int = 0

        init {

            isRun = true
            shouldStop = false
            repeatCount = 1

            setAnimationListener(this)

        }

        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            super.applyTransformation(interpolatedTime, t)

            current = System.currentTimeMillis()
            delta = (current - prev).toFloat()
            prev = current
            for (i in 0 until BubblesView.OVALS_COUNT)
                calculate(delta, i)

            if (!isRun)
                this@BubblesView.alpha = 1 - interpolatedTime
            else if (this@BubblesView.alpha < 1)
                alpha = interpolatedTime

            requestLayout()

        }

        override fun cancel() {
            shouldStop = true
        }

        override fun reset() {
            super.reset()
            isRun = true
            shouldStop = false
            repeatCount = 1

        }

        override fun onAnimationStart(animation: Animation) {
            prev = System.currentTimeMillis()
            setRepeatCount(2)
        }

        override fun onAnimationEnd(animation: Animation) {
            alpha = 0f
        }

        override fun onAnimationRepeat(animation: Animation) {

            if (shouldStop) {
                isRun = false
            } else {
                setRepeatCount(repeatCount++)
            }

        }

    }

    companion object {

        private val TAG = "BubblesView"

        var OVALS_COUNT = 50
        private const val DEGREE_360 = (2 * Math.PI).toFloat()
    }

}
