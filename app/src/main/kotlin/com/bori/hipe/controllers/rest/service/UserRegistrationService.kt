package com.bori.hipe.controllers.rest.service

import android.util.Log
import com.bori.hipe.controllers.rest.StringCallback
import com.bori.hipe.controllers.rest.routes.UserRegistrationRouter

object UserRegistrationService {

    private const val TAG = "UserRegistrationService"

    lateinit var userRegistrationRouter:UserRegistrationRouter

    fun registerUserStepOne(
            username: String,
            password: String,
            requestID: Long
    ){
        Log.d(TAG, "UserRegistrationService.registerUserStepOne")
        userRegistrationRouter.registerUserStepOne(username,password)
                .enqueue(StringCallback(requestID))
    }

    fun registerUserStepTwo(
            username: String,
            email: String,
            publicToken:String,
            requestID:Long
    ){
        Log.d(TAG, "UserRegistrationService.registerUserStepTwo")

        userRegistrationRouter.registerUserStepTwo(username,email,publicToken)
                .enqueue(StringCallback(requestID))
    }


}