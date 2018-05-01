package com.bori.hipe.controllers.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class CircularRevavalView @JvmOverloads constructor(
        context:Context? = null,
        attributes:AttributeSet? = null,
        defStyleAttr:Int = 0) : View(context,attributes,defStyleAttr){

    var circleRadius = 0L
    var coveredView:View? = null
        set(value) {
            field = value
            field?.isDrawingCacheEnabled = true
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

    }

    override fun onDraw(canvas: Canvas?) {

        coveredView?.destroyDrawingCache()
        coveredView?.buildDrawingCache()
        val original = Bitmap.createBitmap(coveredView?.drawingCache)
        val originalCanvas = Canvas(original)
        original.eraseColor()
        super.onDraw(originalCanvas)

        canvas?:return

        val shader = BitmapShader(original, Shader.TileMode.CLAMP,Shader.TileMode.CLAMP)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader = shader
        canvas.drawColor(Color.GRAY)
        canvas.drawCircle(width.toFloat()/2f,height.toFloat()/2f,100f,paint)
        requestLayout()
        invalidate()

    }

}