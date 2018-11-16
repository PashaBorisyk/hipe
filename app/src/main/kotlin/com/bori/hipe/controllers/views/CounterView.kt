package com.bori.hipe.controllers.views

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bori.hipe.R

class CounterView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {


    private companion object {
        private const val ENDING_DURATION = 700L
    }

    private val TAG = "CounterView.kt"
    private var animatedValue: Float = 1f

    private val ovalRect = RectF()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private lateinit var updatedText: TextView

    private lateinit var valueAnimator: ValueAnimator
    private lateinit var colorAnimator: ObjectAnimator
    private val evaluator = ArgbEvaluator()
    private var counterDuration = 0L

    private var userCenter = false
    private var drawBitmap = false

    private val cross:Drawable = context.resources.getDrawable(R.drawable.ic_icons8_delete)
    private val check:Drawable = context.resources.getDrawable(R.drawable.ic_1483821583_checkmark_24_light)
    private lateinit var resultDrawable: Drawable

    val image: ImageView = ImageView(context)

    init {

        paint.color = Color.parseColor("#FF717171")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 15f
        paint.strokeCap = Paint.Cap.ROUND

    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        Log.d(TAG, "CounterView.onMeasure")

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

        canvas.save()
        if(drawBitmap) {

            val realAngle = -360f * animatedValue * 1.6f
            val angle = if(realAngle > -360f) realAngle else -360f

            canvas.drawArc(ovalRect, -90f, angle, userCenter, paint)

            val realD = animatedValue.toDouble()*2.3f
            val d = if(realD - 1.3 < 0) 0 else (Math.sin((realD-1.3)*2.4)*Math.min(width,height)*0.9).toInt()
            val dx = ((width - d)/2)
            val dy = ((height - d)/2)

            resultDrawable.setBounds(dx, dy, dx+d, dy+d)
            resultDrawable.alpha = (255*animatedValue).toInt()
            resultDrawable.draw(canvas)
        }
        else{
            canvas.drawArc(ovalRect, -90f, -360f * animatedValue, userCenter, paint)
        }
        canvas.restore()

    }

    fun start(duration: Long, textView: TextView) {
        Log.d(TAG, "CounterView.start")

        updatedText = textView
        counterDuration = duration

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

    fun done(isSuccessful: Boolean) {
        Log.d(TAG, "CounterView.done")

        resultDrawable = if(isSuccessful) check else cross

        updatedText.animate().alpha(0f).duration = (ENDING_DURATION * animatedValue).toLong()
        colorAnimator.removeAllUpdateListeners()
        valueAnimator.removeAllUpdateListeners()

        valueAnimator = ValueAnimator.ofFloat(animatedValue, 0f).run {

            this.duration = (ENDING_DURATION * this@CounterView.animatedValue).toLong()
            this.repeatCount = 0

            this.addUpdateListener { animation ->

                this@CounterView.animatedValue = animation.animatedValue as Float

                this.addUpdateListener { animation ->

                    this@CounterView.animatedValue = animation.animatedValue as Float

                    val rest = counterDuration * this@CounterView.animatedValue

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

                    updatedText.text = "$mins:$secs:$mills"

                }
                invalidate()
                requestLayout()
            }

            this.start()
            this
        }

        valueAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
                Log.d(TAG, "CounterView.onAnimationRepeat")
            }

            override fun onAnimationEnd(animation: Animator?) {
                Log.d(TAG, "CounterView.onAnimationEnd")
                showResult(isSuccessful)
            }

            override fun onAnimationCancel(animation: Animator?) {
                Log.d(TAG, "CounterView.onAnimationCancel")
            }

            override fun onAnimationStart(animation: Animator?) {
                Log.d(TAG, "CounterView.onAnimationStart")
            }

        })

        colorAnimator = ObjectAnimator.ofObject(
                paint,
                "color",
                evaluator,
                colorAnimator.animatedValue, Color.parseColor("#9a0000")).run {

            this.duration = (ENDING_DURATION * this@CounterView.animatedValue).toLong()
            this.repeatMode = ObjectAnimator.RESTART
            this.repeatCount = 1
            this?.start()
            this
        }


    }

    private fun showResult(isSuccessful: Boolean) {
        Log.d(TAG, "CounterView.showResult")

        userCenter = true
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 1f

        valueAnimator.listeners.clear()
        valueAnimator.removeAllUpdateListeners()
        colorAnimator.removeAllUpdateListeners()

        colorAnimator = ObjectAnimator.ofObject(
                paint,
                "color",
                evaluator,
                colorAnimator.animatedValue, if (isSuccessful)
            Color.parseColor("#009a00")
        else
            Color.parseColor("#9a0000")
        ).run {

            this.duration = ENDING_DURATION
            this.repeatMode = ObjectAnimator.REVERSE
            this.repeatCount = 1
            this.startDelay = 200
            this?.start()
            this
        }

        valueAnimator = ValueAnimator.ofFloat(0f, 1f).run {

            this.duration = ENDING_DURATION
            this.repeatCount = 0
            this.startDelay = 200
            this.addUpdateListener { animation ->

                this@CounterView.animatedValue = animation.animatedValue as Float

                this.addUpdateListener { animation ->
                    this@CounterView.animatedValue = animation.animatedValue as Float
                }
                invalidate()
                requestLayout()
            }

            this.start()
            this
        }

        drawBitmap = true

        System.gc()

    }

}