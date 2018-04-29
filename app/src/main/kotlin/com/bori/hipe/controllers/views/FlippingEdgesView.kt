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
import kotlin.math.absoluteValue


private const val TAG = "FlippingEdgesView."

class FlippingEdgesView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : Button(context, attrs, defStyleAttr) {

    enum class Mode {
        BUTTON, LOADING
    }

    private var mode: Mode = Mode.BUTTON

    var isVisible: Boolean = false

    private var textWidth: Float = 0f

    private val leftArcPath = Path()
    private val rightArcPath = Path()

    private var topBorder = 0f
    private var bottomBorder = 0f
    private var leftBorder = 0f
    private var rightBorder = 0f

    private var drawHeight = 0f
    private var drawWidth = 0f
    private var drawRadius = 0f

    private var circleLength = 0f
    private var topButtomLineLength = 0f

    private var revertingAnimation = false
    private lateinit var buttonLinesAnimator: ValueAnimator
    private lateinit var loadingLinesAnimator: ValueAnimator
    private lateinit var colorAnimator: ObjectAnimator
    private val decelerateInterpolator = DecelerateInterpolator()
    private val evaluator = ArgbEvaluator()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    private val rightBorderRect = RectF()
    private val leftBorderRect = RectF()

    private val xPositionsBottom: XPositions = XPositions(0f, 0f)
    private val xPositionsTop: XPositions = XPositions(0f, 0f)

    private var buttonModeAnimatedValue = 0f
    private var loadingModeAnimatedValue = 0f
    private var lineLength: Float = 0f

    var strokeWidth: Float = 10f
        get() = paint.strokeWidth
        set(value) {
            paint.strokeWidth = value
            field = value
        }

    var colorAnimationDuration: Long
        get() = colorAnimator.duration
        set(value) {
            colorAnimator.duration = value
        }

    var linesAnimationDuration: Long
        get() = buttonLinesAnimator.duration
        set(value) {
            buttonLinesAnimator.duration = value
        }

    var mText: String = "NEXT"
        set(value) {
            textWidth = textPaint.measureText(value)
            field = value
        }

    var mTextSize: Float = 40f
        set(value) {
            textPaint.textSize = value
            field = value
        }

    init {

        paint.color = Color.parseColor("#FF717171")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 7f
        paint.strokeCap = Paint.Cap.ROUND

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 50f
        textPaint.strokeWidth = 8f
        textWidth = textPaint.measureText(mText)
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

        drawHeight = (bottomBorder - topBorder).absoluteValue
        drawWidth = (rightBorder - leftBorder).absoluteValue
        circleLength = (Math.PI * (drawHeight)).toFloat()
        drawRadius = (drawHeight) / 2
        val _length = (drawWidth) + ((Math.PI.toFloat()) * 0.9f * drawRadius)

        rightBorderRect.set(rightBorder - drawRadius, topBorder, rightBorder + drawRadius, bottomBorder)
        leftBorderRect.set(leftBorder - drawRadius, topBorder, leftBorder + drawRadius, bottomBorder)

        xPositionsBottom.xEnd = width.toFloat()
        xPositionsTop.xEnd = 0f


        xPositionsTop.xBegin = -_length
        xPositionsBottom.xBegin = width + _length

        topButtomLineLength = rightBorder - leftBorder - drawRadius*2

        // 3/4 PI == 135 degrees
        lineLength = xPositionsBottom.length

        buttonLinesAnimator = ValueAnimator.ofFloat(0f, lineLength + leftBorder)
        buttonLinesAnimator.duration = 300
        buttonLinesAnimator.interpolator = decelerateInterpolator
        buttonLinesAnimator.addUpdateListener { animation ->
            buttonModeAnimatedValue = animation.animatedValue as Float
            invalidate()
            requestLayout()
        }


        loadingLinesAnimator = ValueAnimator.ofFloat(0f, topButtomLineLength/2)
        loadingLinesAnimator.duration = 300
        loadingLinesAnimator.interpolator = decelerateInterpolator
        loadingLinesAnimator.addUpdateListener { animation ->
            loadingModeAnimatedValue = animation.animatedValue as Float
            invalidate()
            requestLayout()
        }

        colorAnimator = ObjectAnimator.ofObject(paint, "color", evaluator, Color.GRAY, 0xffee5350.toInt())
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

        val path = xPositionsTop.xEnd + buttonModeAnimatedValue - rightBorder

        if (mode == Mode.BUTTON) {

            if (xPositionsTop.xEnd + buttonModeAnimatedValue > rightBorder) {

                canvas.drawLine(rightBorder - xPositionsTop.length + path, topBorder, rightBorder, topBorder, paint)
                canvas.drawLine(leftBorder + xPositionsBottom.length - path, bottomBorder, leftBorder, bottomBorder, paint)

                val degree = 360 * (path / circleLength)

                leftArcPath.reset()
                rightArcPath.reset()

                leftArcPath.arcTo(leftBorderRect, 90f, degree.toFloat(), false)
                rightArcPath.arcTo(rightBorderRect, 270f, degree.toFloat(), false)

                canvas.drawPath(leftArcPath, paint)
                canvas.drawPath(rightArcPath, paint)

            } else {

                canvas.drawLine(xPositionsTop.xBegin + buttonModeAnimatedValue, topBorder, xPositionsTop.xEnd + buttonModeAnimatedValue, topBorder, paint)
                canvas.drawLine(xPositionsBottom.xBegin - buttonModeAnimatedValue, bottomBorder, xPositionsBottom.xEnd - buttonModeAnimatedValue, bottomBorder, paint)

            }

            val ratio = buttonModeAnimatedValue / (lineLength + leftBorder)
            textPaint.alpha = (ratio * 255f).toInt()
            canvas.drawText(mText, (width / 2f) * 1.01f, (height / 2f + mTextSize / 2f + drawHeight * (1f - ratio) / 2f) * 0.97f, textPaint)

        } else if (mode == Mode.LOADING) {

            val padding = if (!revertingAnimation)
                -loadingModeAnimatedValue
            else
                (lineLength - loadingModeAnimatedValue)


            canvas.drawLine(
                    leftBorder + drawRadius - padding,
                    topBorder,
                    rightBorder - drawRadius + padding,
                    topBorder,
                    paint)

            canvas.drawLine(
                    leftBorder + drawRadius - padding,
                    bottomBorder,
                    rightBorder - drawRadius + padding,
                    bottomBorder,
                    paint)


            val circleLength = Math.PI * (drawHeight)
            val degree = 360 * (path / circleLength)

            leftArcPath.reset()
            rightArcPath.reset()

            leftBorderRect.left = leftBorder - padding
            leftBorderRect.right = leftBorder + drawRadius*2 - padding
            leftArcPath.arcTo(leftBorderRect, 90f, degree.toFloat(), false)

            rightBorderRect.left = rightBorder - drawRadius*2 + padding
            rightBorderRect.right = rightBorder + padding
            rightArcPath.arcTo(rightBorderRect, 270f, degree.toFloat(), false)

            canvas.drawPath(leftArcPath, paint)
            canvas.drawPath(rightArcPath, paint)

        }
    }

    fun show(hasToShow: Boolean) {

        if (hasToShow) {

            if (isVisible)
                return
            isClickable = true
            colorAnimator.start()
            buttonLinesAnimator.start()
            isVisible = true
        } else {

            if (!isVisible)
                return
            isClickable = false
            colorAnimator.reverse()
            buttonLinesAnimator.reverse()
            isVisible = false

        }
    }

    fun showLoading() {
        mode = Mode.LOADING
        loadingLinesAnimator.start()

    }

    data class XPositions(var xBegin: Float, var xEnd: Float) {

        val length: Float
            get() = (xEnd - xBegin).absoluteValue

    }
}