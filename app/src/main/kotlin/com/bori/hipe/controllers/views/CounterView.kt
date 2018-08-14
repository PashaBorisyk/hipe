package com.bori.hipe.controllers.views

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.TextView

class CounterView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private companion object {
        private const val ENDING_DURATION = 1000
    }

    private val TAG = "CounterView.kt"
    private var animatedValue: Float = 1f

    private val ovalRect = RectF()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private lateinit var updatedText: TextView

    private lateinit var valueAnimator:ValueAnimator
    private lateinit var colorAnimator:ObjectAnimator


    init {

        paint.color = Color.parseColor("#FF717171")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 15f
        paint.strokeCap = Paint.Cap.ROUND

    }

    fun start(duration: Long, textView: TextView) {

        val evaluator = ArgbEvaluator()

        valueAnimator = ValueAnimator.ofFloat(1f, 0f).run {

            this.duration = duration
            this.interpolator = null
            this.repeatCount = 0

            this.addUpdateListener { animation ->

                this@CounterView.animatedValue = animation.animatedValue as Float

                val rest = duration * this@CounterView.animatedValue

                val mills = (rest.toInt() % 1000).toString().run {
                    when (this.length) {
                        1 -> return@run "00" + this
                        2 -> return@run "0" + this
                        else -> return@run this
                    }
                }
                val secs = ((rest / 1000).toInt() % 60).toString().run {
                    when (this.length) {
                        1 -> return@run "0" + this
                        else -> return@run this
                    }
                }
                val mins = ((rest / (1000 * 60)).toInt() % 60).toString().run {
                    when (this.length) {
                        1 -> return@run "0" + this
                        else -> return@run this
                    }
                }

                textView.text = "$mins:$secs:$mills"

                invalidate()
                requestLayout()
            }

            this.start()
            this
        }

        colorAnimator = ObjectAnimator.ofObject(
                paint,
                "color",
                evaluator,
                Color.parseColor("#009a00"),
                Color.GRAY, Color.parseColor("#9a0000")).run {

            this.duration = duration
            this.repeatMode = ObjectAnimator.RESTART
            this.repeatCount = 1
            this.addUpdateListener {
                paint.color = it.animatedValue as Int
                invalidate()
            }
            this?.start()
            this
        }

    }

    fun done() {

        updatedText.animate().alpha(0f).duration = (ENDING_DURATION * animatedValue).toLong()
        colorAnimator.removeAllUpdateListeners()
        valueAnimator.removeAllUpdateListeners()



    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (widthMode) {
            View.MeasureSpec.EXACTLY -> widthSize
            View.MeasureSpec.AT_MOST -> Math.min(300, widthSize)
            View.MeasureSpec.UNSPECIFIED -> 300
            else -> 100
        }
        val height = when (heightMode) {
            View.MeasureSpec.EXACTLY -> heightSize
            View.MeasureSpec.AT_MOST -> Math.min(300, heightSize)
            View.MeasureSpec.UNSPECIFIED -> 300
            else -> 100
        }

        setMeasuredDimension(width, height)

        val radius = (Math.min(width, height) / 2f) - paint.strokeWidth
        val centreX = width / 2f
        val centreY = height / 2f
        ovalRect.set(
                centreX - radius,
                centreY - radius,
                centreX + radius,
                centreY + radius
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        Log.d(TAG, animatedValue.toString())
        canvas.drawArc(ovalRect, -90f, -360f * animatedValue, false, paint)


    }

}