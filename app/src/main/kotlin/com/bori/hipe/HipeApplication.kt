package com.bori.hipe

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import com.bori.hipe.controllers.location.LocationAccessor
import com.bori.hipe.controllers.messenger.WebSocketConnector
import com.bori.hipe.controllers.receiver.BootReciever
import com.bori.hipe.controllers.rest.routes.EventNewsRouter
import com.bori.hipe.controllers.rest.routes.EventRouter
import com.bori.hipe.controllers.rest.routes.HipeImageRouter
import com.bori.hipe.controllers.rest.routes.UserRouter
import com.bori.hipe.controllers.rest.service.EventNewsService
import com.bori.hipe.controllers.rest.service.EventService
import com.bori.hipe.controllers.rest.service.HipeImageService
import com.bori.hipe.controllers.rest.service.UserService
import com.bori.hipe.controllers.services.HipeService
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.orm.SugarApp
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class HipeApplication : SugarApp() {

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "onCreate: ")

        pixelsPerDp = resources.displayMetrics.density

        val networkInfo = (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
        BootReciever.isConnected = networkInfo != null && networkInfo.isConnected

        LocationAccessor.init(this)
        startService(Intent(this, HipeService::class.java))

        val imageLoaderConfiguration = ImageLoaderConfiguration.Builder(this)
                .diskCacheFileCount(50)
                .threadPriority(Thread.MAX_PRIORITY)
                .writeDebugLogs()
                .diskCacheSize(1024 * 1024 * 10)
                .threadPoolSize(20).build()

        ImageLoader.getInstance().init(imageLoaderConfiguration)
        initRetrofit()
        WebSocketConnector.createConncetion()
    }

    private fun initRetrofit() {

        val restRequests = Retrofit.Builder()
                .baseUrl(SERVER_PATH)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        EventNewsService.eventNewsEnvoker = restRequests.create(EventNewsRouter::class.java)
        EventService.eventEvoke = restRequests.create(EventRouter::class.java)
        HipeImageService.hipeImageRouter = restRequests.create(HipeImageRouter::class.java)
        UserService.userRouter = restRequests.create(UserRouter::class.java)

        val metrics: DisplayMetrics = resources.displayMetrics
        pixelsPerDp = metrics.density
        screenHeight = metrics.heightPixels
        screenWidth = metrics.widthPixels

    }

    override fun onTerminate() {
        super.onTerminate()
        Log.e(TAG, "onTerminate: ")
    }

    companion object {

        private const val TAG = "HipeApplication"
        const val SERVER_PATH = "http://192.168.0.31:9000/"

        val LOLLIPOP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        val KIT_KAT = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        var NICKNAME: String? = null

        var pixelsPerDp = 1f
        var screenWidth = 1
        var screenHeight = 1
        const val THIS_USER_ID = 1L

    }

}