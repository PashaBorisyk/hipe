package com.bori.hipe

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Build
import android.util.Log
import com.bori.hipe.util.device.LocationAccessor
import com.bori.hipe.controllers.messenger.WebSocketConnector
import com.bori.hipe.controllers.receiver.BootReciever
import com.bori.hipe.controllers.rest.configuration.RetrofitConfiguration
import com.bori.hipe.controllers.rest.routes.*
import com.bori.hipe.controllers.services.HipeService
import com.bori.hipe.util.Const
import com.bori.hipe.util.device.Screen
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.orm.SugarApp


class MainApplication : SugarApp() {

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "MainApplication.onCreate")

        pixelsPerDp = resources.displayMetrics.density
        sharedPreferences = getSharedPreferences(Const.HIPE_APPLICATION_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        val networkInfo = (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
        BootReciever.isConnected = networkInfo != null && networkInfo.isConnected

        startService(Intent(this, HipeService::class.java))

        val imageLoaderConfiguration = ImageLoaderConfiguration.Builder(this)
                .diskCacheFileCount(50)
                .threadPriority(Thread.MAX_PRIORITY)
                .writeDebugLogs()
                .diskCacheSize(1024 * 1024 * 10)
                .threadPoolSize(20).build()

        ImageLoader.getInstance().init(imageLoaderConfiguration)
        globalInit()
        WebSocketConnector.createConncetion()
    }

    private fun globalInit() {
        Log.d(TAG, "MainApplication.globalInit")

        RetrofitConfiguration.init(SERVER_PATH)
        Screen.init(resources.displayMetrics)

    }

    override fun onTerminate() {
        super.onTerminate()
        Log.d(TAG, "MainApplication.onTerminate")
    }

    companion object {

        private const val TAG = "MainApplication"

        const val SERVER_PATH = "http://10.0.2.2:9000/"

        var sharedPreferences: SharedPreferences? = null

        val IS_LOLLIPOP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        val IS_KIT_KAT = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        lateinit var username: String

        var pixelsPerDp = 1f
        var screenWidth = 1
        var screenHeight = 1

        private var token: String? = null

        fun getToken(): String {
            if (token != null)
                return token!!
            token = sharedPreferences?.getString(Route.TOKEN,null)
            token ?: return Const.TOKEN
            return token!!
        }

    }

}