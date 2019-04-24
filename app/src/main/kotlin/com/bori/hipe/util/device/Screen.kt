package com.bori.hipe.util.device

import android.util.DisplayMetrics

class Screen private constructor(metrics: DisplayMetrics) {

    val pixelsPerDp: Float = metrics.density
    val screenHeight: Int = metrics.heightPixels
    val screenWidth: Int = metrics.widthPixels

    companion object {

        private var screen:Screen? = null

        fun init(metrics: DisplayMetrics) {
            if (screen == null)
                screen = Screen(metrics)
            else throw IllegalStateException("screen was already initialized")
        }

        fun getInstance() : Screen {
            val thisScreen = screen
            thisScreen?:throw IllegalStateException("Screen was not initialized. You should call init() before")
            return thisScreen
        }

    }

}