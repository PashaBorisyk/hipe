package com.bori.hipe.controllers.fragments.base

import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.reactivex.subjects.PublishSubject

open class HipeBaseFragment : androidx.fragment.app.Fragment() {

    private companion object {
        private const val TAG = "HipeBaseFragment"
    }

    lateinit var rootView: View
    lateinit var stageView: View

    var shouldCallOnFragment: Boolean = false
        protected set

    protected val permissionPublishSubject = PublishSubject.create<Triple<Int, String, Int>>()

    open fun onBackPressed() {
        Log.d(TAG, "HipeBaseFragment.onBackPressed")
        shouldCallOnFragment = false

    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        Log.d(TAG, "CameraFragment.onRequestPermissionsResult")

        permissionPublishSubject.onNext(Triple(requestCode, permissions[0], grantResults[0]))

    }

    protected fun requestPermissionsFast(
            requestCode: Int, permissionName: String
    ) {

        Log.d(TAG, "CameraFragment.requestPermissions")


        if (ContextCompat.checkSelfPermission(activity!!, permissionName) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(activity!!, arrayOf(permissionName), requestCode)
        else
            permissionPublishSubject.onNext(Triple(requestCode, permissionName, PackageManager.PERMISSION_GRANTED))

    }

}