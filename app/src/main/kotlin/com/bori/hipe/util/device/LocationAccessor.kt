package com.bori.hipe.util.device

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

object LocationAccessor {

    private const val TAG = "LocationAccessor"
    private const val ACCESS_FINE_LOCATION_PERMISSION_CODE = 12345
    private const val MIN_REFRESH_TIME = 60*1000L
    private const val MIN_DISTANCE = 10f

    private lateinit var loc: Location

    private var locationManager: LocationManager? = null
    private var accessedProvider: String? = null

    fun init(c: Context) {
        Log.d(TAG, "init: ")
        locationManager = c.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(c as AppCompatActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), ACCESS_FINE_LOCATION_PERMISSION_CODE)
        } else {
            locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_REFRESH_TIME, MIN_DISTANCE, locationListener)
        }
    }

    fun getCurrentLocation(c:Context): Location? {
        Log.d(TAG, "LocationAccessor.getCurrentLocation")
        if (ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(c as AppCompatActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), ACCESS_FINE_LOCATION_PERMISSION_CODE)
        }
        return locationManager!!.getLastKnownLocation(accessedProvider)
    }

    private val locationListener = object : LocationListener {

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
            if (accessedProvider == s) {
                accessedProvider = null
            }
        }
    }

}
