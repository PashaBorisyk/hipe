package com.bori.hipe.controllers.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log

//import com.bori.hipe.controllers.Services.AlkosService;


class BootReciever : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        Log.e(TAG, "onReceive: " + intent.action)

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.e(TAG, "onReceive: DEVICE_REBOOTED")
            //            context.startService(new Intent(context, AlkosService.class));
        } else if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {

            Log.e(TAG, "onReceive: NETWORK STATE CHANGED")

            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo

            isConnected = networkInfo != null && networkInfo.isConnected
            Log.e(TAG, "onReceive:  is connected = " + isConnected)

            //            Intent serviceIntent = new Intent(context,AlkosService.class);

            //            if (isConnected)
            //                context.startService(serviceIntent);

            //            else
            //                context.stopService(serviceIntent);

        }


    }

    companion object {

        var isConnected = false
        private val TAG = "BootReciever"
    }

}
