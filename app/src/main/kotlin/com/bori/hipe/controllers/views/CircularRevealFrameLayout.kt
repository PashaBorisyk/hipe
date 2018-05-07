package com.bori.hipe.controllers.views

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout


class CircularRevealFrameLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
        : FrameLayout(context, attrs, defStyleAttr){

    companion object {
        private const val DURATION = 300L
    }

    enum class State{
        SHOWN,HIDDEN,IS_SHOWING
    }


    private var state = State.SHOWN

    private var animatedValue = 0f

    val drawingPath = Path()
    val animator = ValueAnimator.ofFloat(0f,1f)
    val paint = Paint()

    init {
        animator.duration = DURATION
        animator.interpolator = null
        animator.addUpdateListener {
            animatedValue = it.animatedValue as Float
            requestLayout()
            invalidate()
        }

        animator.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {

            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

        })

        paint.color = Color.GRAY
        paint.style = Paint.Style.FILL
    }

    override fun drawChild(canvas: Canvas?, child: View?, drawingTime: Long): Boolean {
        canvas?:return super.drawChild(canvas, child, drawingTime)
        child?:return super.drawChild(canvas, child, drawingTime)

        val state = canvas.save()
        val radius = Math.sqrt((child.width*child.width).toDouble()+(child.height*child.height).toDouble())
        drawingPath.reset()
        drawingPath.addCircle(0f,0f, radius.toFloat()*animatedValue, Path.Direction.CW)

        canvas.clipPath(drawingPath)
        paint.alpha = ((1f - animatedValue)*255f).toInt()
        canvas.drawCircle(0f,0f,radius.toFloat()*animatedValue,paint)
        val isInvalidated = super.drawChild(canvas, child, drawingTime)

        canvas.restoreToCount(state)

        return isInvalidated

    }


    fun show(){
        state = State.IS_SHOWING
        animator.start()
    }

    fun hide(){
        animator.reverse()
    }

}