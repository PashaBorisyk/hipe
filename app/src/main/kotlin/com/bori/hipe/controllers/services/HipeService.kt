package com.bori.hipe.controllers.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.util.Log

/**
 * Created by pashaborisyk on 27.03.2017.
 */

class HipeService : Service() {

    private var myBinder: MyBinder? = null

    override fun onCreate() {
        Log.d(TAG, "HipeService.create()")
        super.onCreate()
        myBinder = MyBinder(this)

    }


    override fun onBind(intent: Intent): Binder? {
        Log.d(TAG, "HipeService.onBind")

        return myBinder
    }

    inner class MyBinder constructor(val hipeService: HipeService) : Binder()

    companion object {
        private const val TAG = "HipeService"
    }

}