package com.bori.hipe.controllers.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.View


class TriangleView : View {

    private var backGround: Int = 0
    private val mPaint: Paint
    private val mPath: Path

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    init {
        invalidateColor()
        mPaint = Paint()
        mPaint.style = Paint.Style.STROKE
        mPath = Path()
        Log.e(TAG, "init: " + backGround)
    }

    private fun invalidateColor() {
        backGround = (background as? ColorDrawable)?.color ?: 0xffff00
        setBackgroundColor(0x00000000)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mPaint.isAntiAlias = true
        mPaint.strokeWidth = 10f
        mPaint.color = backGround//getResources().getColor(R.color.colorPrimary)
        mPaint.style = Paint.Style.FILL

        mPath.reset()
        mPath.moveTo(0f, 0f)
        mPath.rLineTo((width / 2).toFloat(), height.toFloat())
        mPath.lineTo(width.toFloat(), 0f)
        mPath.lineTo(0f, 0f)

        canvas.drawPath(mPath, mPaint)
    }

    companion object {
        private val TAG = "TriangleView"
    }
}