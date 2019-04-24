package com.bori.hipe.controllers.rest.routes

import com.bori.hipe.controllers.rest.routes.Route.EMAIL
import com.bori.hipe.controllers.rest.routes.Route.PASSWORD
import com.bori.hipe.controllers.rest.routes.Route.REGISTRATION_TOKEN
import com.bori.hipe.controllers.rest.routes.Route.USERNAME
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface UserRegistrationRoute  {

    @POST("/user_registration/create_registration/")
    fun registerUserStepOne(
            @Query(USERNAME) username: String,
            @Query(PASSWORD) password: String,
            @Query(EMAIL) email:String
    ): Call<String>

    @POST("/user_registration/confirm_registration_and_create_user/")
    fun registerUserStepTwo(
            @Query(REGISTRATION_TOKEN) registrationToken: String
    ): Call<String>

}