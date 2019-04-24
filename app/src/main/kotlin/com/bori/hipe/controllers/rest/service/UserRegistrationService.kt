package com.bori.hipe.controllers.rest.service

import android.util.Log
import com.bori.hipe.controllers.rest.callback.StringCallback
import com.bori.hipe.controllers.rest.routes.UserRegistrationRoute

object UserRegistrationService {

    private const val TAG = "UserRegistrationService"

    lateinit var userRegistrationRoute: UserRegistrationRoute

    fun registerUserStepOne(
            requestID: Int,
            username: String,
            password: String,
            email: String
    ) {
        Log.d(TAG, "UserRegistrationService.registerUserStepOne")
        userRegistrationRoute.registerUserStepOne(username, password, email)
                .enqueue(StringCallback(requestID))
    }

    fun registerUserStepTwo(
            requestID: Int,
            registrationToken: String
    ) {
        Log.d(TAG, "UserRegistrationService.registerUserStepTwo")

        userRegistrationRoute.registerUserStepTwo(registrationToken)
                .enqueue(StringCallback(requestID))
    }


}