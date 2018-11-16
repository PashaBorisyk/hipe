package com.bori.hipe

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import com.bori.hipe.controllers.location.LocationAccessor
import com.bori.hipe.controllers.messenger.WebSocketConnector
import com.bori.hipe.controllers.receiver.BootReciever
import com.bori.hipe.controllers.rest.routes.*
import com.bori.hipe.controllers.rest.service.*
import com.bori.hipe.controllers.services.HipeService
import com.bori.hipe.util.Const
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.orm.SugarApp
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class HipeApplication : SugarApp() {

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "HipeApplication.onCreate")

        pixelsPerDp = resources.displayMetrics.density
        sharedPreferences = getSharedPreferences(Const.HIPE_APPLICATION_SHARED_PREFERENCES, Context.MODE_PRIVATE)

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
        Log.d(TAG, "HipeApplication.initRetrofit")

        val restRequests = Retrofit.Builder()
                .baseUrl(SERVER_PATH)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        EventNewsService.eventNewsEnvoker = restRequests.create(EventNewsRouter::class.java)
        EventService.eventEvoke = restRequests.create(EventRouter::class.java)
        HipeImageService.hipeImageRouter = restRequests.create(HipeImageRouter::class.java)
        UserService.userRouter = restRequests.create(UserRouter::class.java)
        UserRegistrationService.userRegistrationRouter = restRequests
                .create(UserRegistrationRouter::class.java)

        val metrics: DisplayMetrics = resources.displayMetrics
        pixelsPerDp = metrics.density
        screenHeight = metrics.heightPixels
        screenWidth = metrics.widthPixels

    }

    override fun onTerminate() {
        super.onTerminate()
        Log.d(TAG, "HipeApplication.onTerminate")
    }

    companion object {

        private const val TAG = "HipeApplication"

        const val SERVER_PATH = "http://192.168.100.41:9000/"

        lateinit var sharedPreferences:SharedPreferences

        val IS_LOLLIPOP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        val IS_KIT_KAT = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        lateinit var username: String

        var pixelsPerDp = 1f
        var screenWidth = 1
        var screenHeight = 1
        const val THIS_USER_ID = 1L

    }

}