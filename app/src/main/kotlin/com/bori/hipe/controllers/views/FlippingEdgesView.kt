package com.bori.hipe.controllers.views

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import com.bori.hipe.util.extensions.zeroIfNegative



class FlippingEdgesView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : Button(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "FlippingEdgesView.kt"
        private const val BUTTON_LINES_ANIMATOR_DURATION = 300L
        private const val BALANCER =  0.95f
    }

    private enum class Mode {
        BUTTON_MODE, LOADING_MODE, FLIPPING_MODE,CHANGING_TEXT_MODE
    }

    private var hasShown: Boolean = false
    var mVisibleRectF = RectF(0.2f,0.0f,0.8f,1f)
    var circleRatio = 0.45f

    private var mode: Mode = Mode.BUTTON_MODE

    private var textWidth: Float = 0f

    private val leftArcPath = Path()
    private val rightArcPath = Path()

    private var topBorderOfShape = 0f
    private var bottomBorderOfShape = 0f
    private var leftBorderOfShape = 0f
    private var rightBorderOfShape = 0f

    private var drawDiametr = 0f

    private var circleLength = 0f
    private var degree = 0f
    private var halfTopBottomLineLength = 0f

    private var isEndingAnimation = false
    private var hasToShowCircle = false

    private var animatedValue = 0f
    private lateinit var animator: ValueAnimator
    private lateinit var colorAnimator: ObjectAnimator

    private val evaluator = ArgbEvaluator()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    private val rightBorderRect = RectF()
    private val leftBorderRect = RectF()

    private var lineLength: Float = 0f
    private var linePathLength:Float = 0f

    private var _afterStop :( (FlippingEdgesView) -> Unit )? = null
    private var _beforeStart :( (FlippingEdgesView) -> Unit )? = null

    var strokeWidth: Float = 10f
        get() = paint.strokeWidth
        set(value) {
            paint.strokeWidth = value
            field = value
        }

    var mainText: String = "FIRST"
        set(value) {
            textWidth = textPaint.measureText(value)
            field = value
        }

    var secondaryText: String = "SECOND"
        set(value) {
            textWidth = textPaint.measureText(value)
            field = value
        }

    var mTextSize: Float = 40f
        set(value) {
            textPaint.textSize = value
            field = value
        }

    var circleColor:Int = 0xff334455.toInt()
        set(value){
            circlePaint.color = value
            field = value
        }

    var colors = intArrayOf(Color.GRAY, 0xffee5350.toInt())
        set(value) {
            colorAnimator.setIntValues(*value)
            colorAnimator.setupStartValues()
            colorAnimator.start()
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
        textWidth = textPaint.measureText(mainText)

        circlePaint.style = Paint.Style.FILL
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

        topBorderOfShape = if(mVisibleRectF.top > 0f)
            height * mVisibleRectF.top
        else
            paint.strokeWidth/2f

        bottomBorderOfShape = if(mVisibleRectF.bottom < 1f)
            height *mVisibleRectF.bottom
        else
            height - paint.strokeWidth/2f

        drawDiametr = bottomBorderOfShape - topBorderOfShape

        rightBorderOfShape = width * mVisibleRectF.right
        leftBorderOfShape = width * mVisibleRectF.left

        circleLength = (Math.PI * drawDiametr * circleRatio).toFloat()
        lineLength = rightBorderOfShape - leftBorderOfShape - drawDiametr*2f + circleLength
        halfTopBottomLineLength = (lineLength-circleLength)/2f

        linePathLength = rightBorderOfShape - drawDiametr + circleLength

        rightBorderRect.set(rightBorderOfShape-drawDiametr*1.5f, topBorderOfShape, rightBorderOfShape - drawDiametr*0.5f, bottomBorderOfShape)
        leftBorderRect.set(leftBorderOfShape+drawDiametr*0.5f, topBorderOfShape, leftBorderOfShape+drawDiametr*1.5f, bottomBorderOfShape)

        animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = BUTTON_LINES_ANIMATOR_DURATION
        animator.interpolator = null
        animator.repeatCount = 0
        animator.addUpdateListener { animation ->

            animatedValue = animation.animatedValue as Float
            invalidate()
            requestLayout()
        }
        
        animator.addListener(object : Animator.AnimatorListener{

            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                if(mode == Mode.FLIPPING_MODE){
                    if(!isEndingAnimation) {
                        animator.start()
                        return
                    } else if(!hasToShowCircle && isEndingAnimation) {
                        hasToShowCircle = true
                        animator.start()
                        return
                    } else{
                        mode = Mode.LOADING_MODE
                        animator.reverse()
                        return
                    }
                } else if (mode == Mode.LOADING_MODE){
                    if(isEndingAnimation){
                        isEndingAnimation = false
                        hasToShowCircle = false
                        mode = Mode.BUTTON_MODE
                        animatedValue = 1f
                    }else{
                        mode = Mode.FLIPPING_MODE
                        animator.start()
                        return
                    }
                } else if(mode == Mode.CHANGING_TEXT_MODE){
                    mode = Mode.BUTTON_MODE
                    mainText = secondaryText
                }

                _afterStop?.invoke(this@FlippingEdgesView)
                _afterStop = null
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
                _beforeStart?.invoke(this@FlippingEdgesView)
                _beforeStart = null
            }
            
        })

        colorAnimator = ObjectAnimator.ofObject(paint, "color", evaluator, Color.GRAY, 0xffee5350.toInt())
        colorAnimator.duration = 1000
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
        canvas.save()

        leftArcPath.reset()
        rightArcPath.reset()

        val value = when (mode) {

            Mode.BUTTON_MODE -> linePathLength
            Mode.FLIPPING_MODE -> 360f
            Mode.LOADING_MODE -> halfTopBottomLineLength
            Mode.CHANGING_TEXT_MODE -> ((bottomBorderOfShape-topBorderOfShape) + mTextSize)/2f

        } * animatedValue

        when (mode) {

            Mode.BUTTON_MODE -> {

                if (value > rightBorderOfShape - drawDiametr) {

                    degree = 360 * circleRatio * (((value - (rightBorderOfShape - drawDiametr)))/circleLength)

                    canvas.drawLine(-lineLength + value, topBorderOfShape, rightBorderOfShape - drawDiametr, topBorderOfShape, paint)
                    canvas.drawLine(width + lineLength - value, bottomBorderOfShape, leftBorderOfShape + drawDiametr, bottomBorderOfShape, paint)


                    leftArcPath.arcTo(leftBorderRect, 90f, degree, false)
                    rightArcPath.arcTo(rightBorderRect, 270f, degree, false)

                    canvas.drawPath(leftArcPath, paint)
                    canvas.drawPath(rightArcPath, paint)

                } else {

                    canvas.drawLine(-lineLength + value, topBorderOfShape, value, topBorderOfShape, paint)
                    canvas.drawLine(width + lineLength - value, bottomBorderOfShape, width - value, bottomBorderOfShape, paint)
                }
                textPaint.alpha = (animatedValue * 255f).toInt()
                canvas.drawText(mainText, (width / 2f) * 1.01f, (height / 2f + mTextSize / 2f + drawDiametr * (1f - animatedValue) / 2f) * BALANCER, textPaint)

            }
            
            Mode.LOADING_MODE -> {
                
                canvas.drawLine(leftBorderOfShape + drawDiametr + value, topBorderOfShape, rightBorderOfShape - drawDiametr - value, topBorderOfShape, paint)
                canvas.drawLine(rightBorderOfShape - drawDiametr - value, bottomBorderOfShape, leftBorderOfShape + drawDiametr + value, bottomBorderOfShape, paint)
                
                leftArcPath.reset()
                rightArcPath.reset()

                leftBorderRect.left = leftBorderOfShape  + drawDiametr*0.5f + value
                leftBorderRect.right = leftBorderOfShape + drawDiametr*1.5f + value
                leftArcPath.arcTo(leftBorderRect, 90f, degree, false)

                rightBorderRect.left = rightBorderOfShape - drawDiametr*1.5f - value
                rightBorderRect.right = rightBorderOfShape - drawDiametr*0.5f - value
                rightArcPath.arcTo(rightBorderRect, 270f, degree, false)

                canvas.drawPath(leftArcPath, paint)
                canvas.drawPath(rightArcPath, paint)

                textPaint.alpha = ( (1f - animatedValue*3f).zeroIfNegative() * 255f).toInt()
                canvas.drawText(mainText, (width / 2f) * 1.01f, (height / 2f + mTextSize / 2f) * BALANCER, textPaint)

            }
            
            Mode.FLIPPING_MODE -> {

                leftArcPath.reset()
                rightArcPath.reset()

                leftBorderRect.right = leftBorderOfShape + drawDiametr*1.5f + halfTopBottomLineLength
                leftBorderRect.left = leftBorderOfShape  + drawDiametr*0.5f + halfTopBottomLineLength

                rightBorderRect.left = rightBorderOfShape - drawDiametr*1.5f - halfTopBottomLineLength
                rightBorderRect.right = rightBorderOfShape - drawDiametr*0.5f - halfTopBottomLineLength

                leftArcPath.arcTo(leftBorderRect, 90f + value, degree, false)
                rightArcPath.arcTo(rightBorderRect, 270f + value, degree, false)

                canvas.drawPath(leftArcPath, paint)
                canvas.drawPath(rightArcPath, paint)

                if (hasToShowCircle) {
                    
                    circlePaint.alpha = (255f * (1 - animatedValue)).zeroIfNegative().toInt()
                    canvas.drawCircle(width / 2f, height / 2f, animatedValue*drawDiametr/2, circlePaint)

                }
            
            }

            Mode.CHANGING_TEXT_MODE->{

                canvas.drawLine(-lineLength + linePathLength, topBorderOfShape, rightBorderOfShape - drawDiametr, topBorderOfShape, paint)
                canvas.drawLine(width + lineLength - linePathLength, bottomBorderOfShape, leftBorderOfShape + drawDiametr, bottomBorderOfShape, paint)

                leftArcPath.arcTo(leftBorderRect, 90f, degree, false)
                rightArcPath.arcTo(rightBorderRect, 270f, degree, false)

                canvas.drawPath(leftArcPath, paint)
                canvas.drawPath(rightArcPath, paint)

                canvas.drawText(
                        mainText,
                        (width / 2f) * 1.01f,
                        (height/2f + mTextSize/2f - value) * BALANCER
                        , textPaint
                )

                canvas.drawText(
                        secondaryText,
                        (width / 2f) * 1.01f,
                        (bottomBorderOfShape + mTextSize - value) * BALANCER
                        , textPaint
                )

            }
        }

        canvas.restore()

    }

    fun show(hasToShow: Boolean,ifWillNotAnimate: ((FlippingEdgesView) -> Unit)? = null) :FlippingEdgesView {

        if (hasToShow) {
            if (hasShown) {
                ifWillNotAnimate?.invoke(this)
                return this
            }
            isClickable = true
            mode = Mode.BUTTON_MODE
            colorAnimator.start()
            animator.start()
            hasShown = true

        } else {
            if (!hasShown) {
                ifWillNotAnimate?.invoke(this)
                return this
            }
            isClickable = false
            mode = Mode.BUTTON_MODE
            animator.reverse()
            hasShown = false

        }

        return this
    }

    fun changeText(text:String) : FlippingEdgesView {
        if(mode == Mode.BUTTON_MODE && hasShown ) {
            secondaryText = text
            mode = Mode.CHANGING_TEXT_MODE
            animator.start()
        }
        else{
            mainText = text
        }

        return this
    }

    fun startLoading() : FlippingEdgesView {

        isEndingAnimation = false
        mode = Mode.LOADING_MODE
        animator.repeatCount = 0
        animator.start()
        return this

    }

    fun stopLoading():FlippingEdgesView {
        isEndingAnimation = true
        return this
    }

    fun beforeStart(lambda : (FlippingEdgesView) -> Unit) : FlippingEdgesView{
        _beforeStart = lambda
        return this
    }

    fun afterStop(lambda : (FlippingEdgesView) -> Unit) : FlippingEdgesView{
        _afterStop = lambda
        return this
    }

}