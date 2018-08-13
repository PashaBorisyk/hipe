package com.bori.hipe.controllers.rest.service

import com.bori.hipe.controllers.rest.StringCallback
import com.bori.hipe.controllers.rest.routes.UserRegistrationRouter

object UserRegistrationService {

    private const val TAG = "UserRegistrationService.kt"

    lateinit var userRegistrationRouter:UserRegistrationRouter

    fun registerUserStepOne(
            username: String,
            password: String,
            requestID: Long
    ){
        userRegistrationRouter.registerUserStepOne(username,password)
                .enqueue(StringCallback(requestID))
    }

    fun registerUserStepTwo(
            username: String,
            email: String,
            publicToken:String,
            requestID:Long
    ){
        userRegistrationRouter.registerUserStepTwo(username,email,publicToken)
                .enqueue(StringCallback(requestID))
    }


}