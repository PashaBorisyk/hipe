package com.bori.hipe.controllers.views

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.bori.hipe.HipeApplication


class CircularRevavalView @JvmOverloads constructor(
        context:Context? = null,
        attributes:AttributeSet? = null,
        defStyleAttr:Int = 0) : View(context,attributes,defStyleAttr){

    companion object {
        private const val TAG = "CircularRevavalView.kt"
        private const val DURATION = 300L
    }

    enum class Direction{
        IN,OUT
    }

    var additionalView:View? = null

    private var animatedValue = 0f

    private val animator = ValueAnimator.ofFloat(0f,1f)

    private var fromX = 0f
    private var fromY = 0f
    private var radius = 0f

    var direction = Direction.IN
        private set

    private val mTransparentPaint: Paint = Paint()
    private val mSemiBlackPaint: Paint= Paint()
    private val mPath = Path()

    init {

        animator.duration = DURATION
        animator.repeatMode = ValueAnimator.RESTART
        animator.interpolator = null
        animator.addUpdateListener {
            animatedValue = it.animatedValue as Float
            invalidate()
            requestLayout()
        }

        animator.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {
                Log.d(TAG, "CircularRevavalView.onAnimationRepeat")
            }

            override fun onAnimationEnd(animation: Animator?) {
                Log.d(TAG, "CircularRevavalView.onAnimationEnd")

                if(direction == Direction.OUT) {
                    direction = Direction.IN

                } else if(direction == Direction.IN) {
                    direction = Direction.OUT
                }
            }

            override fun onAnimationCancel(animation: Animator?) {
                Log.d(TAG, "CircularRevavalView.onAnimationCancel")
            }

            override fun onAnimationStart(animation: Animator?) {
                Log.d(TAG, "CircularRevavalView.onAnimationStart")


                if(direction == Direction.IN){
                    additionalView?.visibility = GONE
                }else{
                    additionalView?.alpha = 0f
                    additionalView?.visibility = VISIBLE
                }


            }

        })

        mTransparentPaint.color = 0x00000000
        mTransparentPaint.strokeWidth = 10f

        mSemiBlackPaint.color = Color.TRANSPARENT
        mSemiBlackPaint.strokeWidth = 10f

    }

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        Log.d(TAG, "CircularRevavalView.onMeasure")

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
        radius = Math.sqrt((width*width+height*height).toDouble()).toFloat() / 2f

        requestLayout()
        invalidate()

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        mPath.reset()

        val _x = fromX + (width/2f - fromX)*animatedValue
        val _y = fromY + (height/2f - fromY)*animatedValue

        mPath.addCircle(_x, _y, radius*animatedValue, Path.Direction.CW)
        mPath.fillType = Path.FillType.INVERSE_EVEN_ODD

        mTransparentPaint.alpha = (5f * (1f - animatedValue)).toInt()
        canvas?.drawCircle(_x, _y, radius*animatedValue, mTransparentPaint)

        if(direction == Direction.OUT)
            additionalView?.alpha = (1f - animatedValue*4)

        canvas?.drawPath(mPath, mSemiBlackPaint)
        canvas?.clipPath(mPath)
        canvas?.drawColor(Color.parseColor("#FFf6f6f6"))

    }

    fun showIn(fromX:Float = 0f, fromY:Float = 0f, event:MotionEvent? = null) {
        Log.d(TAG, "CircularRevavalView.showIn")


        if(event == null) {
            this.fromX = fromX
            this.fromY = fromY
        } else {
            this.fromX = event.rawX - (HipeApplication.screenWidth - width)
            this.fromY = event.rawY - (HipeApplication.screenHeight - height)
        }
        animator.start()

    }

    fun showOut(fromX: Float = this.fromX,fromY: Float = this.fromY){
        Log.d(TAG, "CircularRevavalView.showOut")

        this.fromX = fromX
        this.fromY = fromY
        animator.reverse()
    }

}




