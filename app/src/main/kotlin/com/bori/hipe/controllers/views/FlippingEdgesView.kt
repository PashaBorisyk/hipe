package com.bori.hipe.controllers.views

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Button


private const val TAG = "FlippingEdgesView."

class FlippingEdgesView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : Button(context, attrs, defStyleAttr) {

    var isVisible:Boolean = false

    private val leftArcPath = Path()
    private val rightArcPath = Path()

    private var topBorder = 0f
    private var bottomBorder = 0f
    private var leftBorder = 0f
    private var rightBorder = 0f

    private lateinit var linesAnimator:ValueAnimator
    private lateinit var colorAnimator:ObjectAnimator
    private val decelerateInterpolator = DecelerateInterpolator()
    private val evaluator = ArgbEvaluator()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    private val rightBorderRect = RectF()
    private val leftBorderRect = RectF()

    private val xPositionsBottom: XPositions = XPositions(0f, 0f)
    private val xPositionsTop: XPositions = XPositions(0f, 0f)

    private var animatedValue = 0f
    private var lineLength: Float = 0f

    var strokeWidth:Float = 10f
        get() = paint.strokeWidth
        set(value){
            paint.strokeWidth = value
            field = value
        }


    var colorAnimationDuration:Long
        get() = colorAnimator.duration
        set(value){
            colorAnimator.duration = value
        }

    var linesAnimationDuration:Long
        get() = linesAnimator.duration
    set(value) {
        linesAnimator.duration = value
    }

    init {

        val attributes = intArrayOf(
                android.R.attr.paddingLeft,
                android.R.attr.paddingTop,
                android.R.attr.paddingBottom,
                android.R.attr.paddingRight
        )

        paint.color = Color.parseColor("#FF717171")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 7f
        paint.strokeCap = Paint.Cap.ROUND

        textPaint.textAlign= Paint.Align.LEFT
        textPaint.textSize = 50f
        textPaint.strokeWidth = 8f
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

        topBorder = height * 0.2f
        bottomBorder = height * 0.8f
        rightBorder = width * 0.7f
        leftBorder = width * 0.3f

        val dim = (bottomBorder - topBorder) / 2
        val _length = (rightBorder - leftBorder) + ((Math.PI.toFloat()) * 0.9f * dim)

        rightBorderRect.set(rightBorder - dim, topBorder, rightBorder + dim, bottomBorder)
        leftBorderRect.set(leftBorder - dim, topBorder, leftBorder + dim, bottomBorder)

        xPositionsBottom.xEnd = width.toFloat()
        xPositionsTop.xEnd = 0f

        xPositionsTop.xBegin = - _length
        xPositionsBottom.xBegin = width+_length

        // 3/4 PI == 135 degrees
        lineLength = xPositionsBottom.length

        linesAnimator = ValueAnimator.ofFloat(0f, lineLength + leftBorder)
        linesAnimator.duration = 300
        linesAnimator.interpolator = decelerateInterpolator
        linesAnimator.addUpdateListener { animation ->
            animatedValue = animation.animatedValue as Float
            invalidate()
            requestLayout()
        }

        colorAnimator= ObjectAnimator.ofObject(paint, "color", evaluator, Color.GRAY,0xffee5350.toInt())
        colorAnimator.duration = 2000
        colorAnimator.repeatMode = ObjectAnimator.REVERSE
        colorAnimator.repeatCount = ObjectAnimator.INFINITE
        colorAnimator.addUpdateListener {
            textPaint.color = it.animatedValue as Int
            invalidate()
            requestLayout()
        }

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas ?: return

        if (xPositionsTop.xEnd + animatedValue > rightBorder) {

            val path = xPositionsTop.xEnd + animatedValue - rightBorder

            canvas.drawLine(rightBorder-xPositionsTop.length+path,topBorder,rightBorder,topBorder,paint)
            canvas.drawLine(leftBorder+xPositionsBottom.length-path,bottomBorder,leftBorder,bottomBorder,paint)

            val circleLength = Math.PI*(bottomBorder - topBorder)
            val degree = 360*(path/circleLength)

            leftArcPath.reset()
            rightArcPath.reset()

            leftArcPath.arcTo(leftBorderRect, 90f, degree.toFloat(), false)
            rightArcPath.arcTo(rightBorderRect, 270f, degree.toFloat(), false)

            canvas.drawPath(leftArcPath, paint)
            canvas.drawPath(rightArcPath, paint)

        } else {

            canvas.drawLine(xPositionsTop.xBegin + animatedValue, topBorder, xPositionsTop.xEnd + animatedValue, topBorder, paint)
            canvas.drawLine(xPositionsBottom.xBegin - animatedValue, bottomBorder, xPositionsBottom.xEnd - animatedValue, bottomBorder, paint)

        }

        val textWidth = textPaint.measureText("NEXT")
        canvas.drawText("NEXT",width/2f-textWidth/2f,height/2f+15f,textPaint)

    }

    fun show() {

        if(isVisible){
            dismiss()
            return
        }
        colorAnimator.start()
        linesAnimator.start()
        isVisible = true

    }

    fun dismiss(){
        colorAnimator.reverse()
        linesAnimator.reverse()
        isVisible = false
    }

    data class XPositions(var xBegin: Float, var xEnd: Float) {

        val length: Float
            get() = Math.abs(xEnd - xBegin)

    }
}