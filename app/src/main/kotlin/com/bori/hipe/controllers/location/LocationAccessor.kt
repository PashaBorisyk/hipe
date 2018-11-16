package com.bori.hipe.controllers.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log

object LocationAccessor : LocationListener {

    private const val TAG = "LocationAccessor"
    private const val ACCESS_FINE_LOCATION_PERMISSION_CODE = 12345

    private var lastMills = 0
    private var locationManager: LocationManager? = null
    private lateinit var loc: Location
    private lateinit var accessedProvider: String

    private var location: Location? = null

    override fun onLocationChanged(location: Location) {
        Log.d(TAG, "onLocationChanged: ")
        loc = location
    }

    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {
        Log.d(TAG, "onStatusChanged() called with: s = [$s], i = [$i], bundle = [$bundle]")
    }

    override fun onProviderEnabled(s: String) {
        Log.d(TAG, "onProviderEnabled() called with: s = [$s]")
        accessedProvider = s
    }

    override fun onProviderDisabled(s: String) {
        Log.d(TAG, "onProviderDisabled() called with: s = [$s]")
    }

    fun init(context: Context) {
        Log.d(TAG, "init: ")
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private fun accessCurrentLocation(c: Context): Location? {
        Log.d(TAG, "LocationAccessor.accessCurrentLocation")

        accessedProvider = LocationManager.NETWORK_PROVIDER

        if (ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(c as AppCompatActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), ACCESS_FINE_LOCATION_PERMISSION_CODE)
            return null
        }

        location = locationManager!!.getLastKnownLocation(accessedProvider)
        return location
    }

    fun getCurrentLocation(c: Context): Location? {
        Log.d(TAG, "LocationAccessor.getCurrentLocation")

        if (System.currentTimeMillis() - lastMills > 3600000)
            return accessCurrentLocation(c)

        return location
    }

}
