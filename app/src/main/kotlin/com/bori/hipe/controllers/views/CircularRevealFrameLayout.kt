package com.bori.hipe.controllers.views

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.bori.hipe.HipeApplication


class CircularRevealFrameLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
        : FrameLayout(context, attrs, defStyleAttr){

    companion object {
        private const val DURATION = 300L
        private const val TAG = "RevealFrameLayout.kt"
    }

    enum class State{
        SHOWN,HIDDEN,IS_SHOWING
    }

    var child: View? = null
        set(value) {
            if(state == State.HIDDEN)
                value?.visibility = View.GONE
            field = value
        }
    var state = State.HIDDEN
        private set

    private var isForward = true

    private var animatedValue = 0f

    private var fromY = 0f
    private var fromX = 0f

    private val drawingPath = Path()
    private val animator = ValueAnimator.ofFloat(0f,1f)
    private val paint = Paint()

    init {
        animator.duration = DURATION
        animator.interpolator = null
        animator.addUpdateListener {
            animatedValue = it.animatedValue as Float
            invalidate()
        }

        animator.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                state = if(isForward) {
                    State.SHOWN
                }
                else {
                    child?.visibility = GONE
                    State.HIDDEN
                }


                isForward = !isForward
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
                state = State.IS_SHOWING
                if(isForward)
                    child?.visibility = VISIBLE
            }

        })

        paint.color = Color.GRAY
        paint.style = Paint.Style.FILL
    }

    override fun drawChild(canvas: Canvas?, child: View?, drawingTime: Long): Boolean {
        canvas?:return super.drawChild(canvas, child, drawingTime)
        child?:return super.drawChild(canvas, child, drawingTime)

        val _x = fromX + (width/2f - fromX)*animatedValue
        val _y = fromY + (height/2f - fromY)*animatedValue

        val state = canvas.save()
        val radius = Math.sqrt((child.width*child.width).toDouble()+(child.height*child.height).toDouble())
        drawingPath.reset()
        drawingPath.addCircle(_x,_y, radius.toFloat()*animatedValue, Path.Direction.CW)

        canvas.clipPath(drawingPath)
        paint.alpha = ((1f - animatedValue)*55f).toInt()
        canvas.drawCircle(_x,_y,radius.toFloat()*animatedValue,paint)
        val isInvalidated = super.drawChild(canvas, child, drawingTime)

        canvas.restoreToCount(state)

        return isInvalidated

    }

    fun showIn(fromX:Float = 0f, fromY:Float = 0f, event: MotionEvent? = null) {

        if(event == null) {
            Log.d(TAG,"showIn(MotionEvent == null)")
            this.fromX = fromX
            this.fromY = fromY
        } else {
            this.fromX = event.rawX - (HipeApplication.screenWidth - width)
            this.fromY = event.rawY - (HipeApplication.screenHeight - height)
        }
        animator.start()

    }

    fun showOut(fromX: Float = this.fromX,fromY: Float = this.fromY){
        this.fromX = fromX
        this.fromY = fromY
        animator.reverse()
    }

}