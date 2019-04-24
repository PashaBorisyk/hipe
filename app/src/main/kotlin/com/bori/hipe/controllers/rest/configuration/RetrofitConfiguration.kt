package com.bori.hipe.controllers.rest.configuration

import com.bori.hipe.controllers.rest.routes.EventRoute
import com.bori.hipe.controllers.rest.routes.ImageRoutes
import com.bori.hipe.controllers.rest.routes.UserRegistrationRoute
import com.bori.hipe.controllers.rest.routes.UserRoutes
import com.bori.hipe.controllers.rest.service.EventService
import com.bori.hipe.controllers.rest.service.ImageService
import com.bori.hipe.controllers.rest.service.UserRegistrationService
import com.bori.hipe.controllers.rest.service.UserService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

object RetrofitConfiguration {

    fun init(serverPath:String){

        val retrofit = Retrofit.Builder()
                .baseUrl(serverPath)
                .addConverterFactory(createConverterFactory())
                .client(createClient())
                .build()

        initRoutes(retrofit)

    }

    private fun createClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient
                .Builder()
                .addInterceptor(loggingInterceptor)
                .build()
    }

    private fun createConverterFactory() = JacksonConverterFactory
            .create(ObjectMapper().registerModule(KotlinModule()))

    private fun initRoutes(retrofit: Retrofit){

        EventService.eventRouter = retrofit
                .create(EventRoute::class.java)

        ImageService.imageRoutes = retrofit
                .create(ImageRoutes::class.java)

        UserService.userRouter = retrofit
                .create(UserRoutes::class.java)

        UserRegistrationService.userRegistrationRoute = retrofit
                .create(UserRegistrationRoute::class.java)
    }
}