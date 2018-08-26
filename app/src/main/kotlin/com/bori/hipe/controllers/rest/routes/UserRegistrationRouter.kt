package com.bori.hipe.controllers.rest.routes

import com.bori.hipe.util.Const
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface UserRegistrationRouter {

    @POST("/user_registration/step_one/")
    fun registerUserStepOne(
            @Query(Const.USERNAME) username: String,
            @Query(Const.PASSWORD) password: String
    ): Call<String>

    @POST("/user_registration/step_two/")
    fun registerUserStepTwo(
            @Query(Const.USERNAME) username: String,
            @Query(Const.EMAIL) email: String,
            @Query(Const.USER_PUBLIC_TOKEN) publicToken:String
    ): Call<String>


}