package com.bori.hipe.controllers.fragments.base

import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import io.reactivex.subjects.PublishSubject

open class HipeBaseFragment : Fragment() {

    private companion object {
        private const val TAG = "HipeBaseFragment"
    }

    lateinit var rootView: View
    lateinit var stageView: View

    var shouldCallOnFragment: Boolean = false
        protected set

    private val onSurfaceTextureAvailable = PublishSubject.create<Pair<Int, PermissionResult>>()

    open fun onBackPressed() {
        Log.d(TAG, "HipeBaseFragment.onBackPressed")
        shouldCallOnFragment = false

    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        Log.d(TAG, "CameraFragment.onRequestPermissionsResult")
        // If request is cancelled, the result arrays are empty.
        if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            onSurfaceTextureAvailable.onNext(requestCode to PermissionResult.GRANTED)
        } else {
            onSurfaceTextureAvailable.onNext(requestCode to PermissionResult.DECLINED)
        }
        return
    }


    protected fun requestPermissionsFast(permissionId: Int, permissionName: String) {

        Log.d(TAG, "CameraFragment.requestPermissions")
        if (ContextCompat.checkSelfPermission(activity!!, permissionName) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission is not granted")
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                            activity!!, permissionName)) {
                Log.d(TAG, "Explonation needed")
            } else {
                ActivityCompat.requestPermissions(
                        activity!!, arrayOf(permissionName), permissionId
                )

            }
        }

    }

    protected enum class PermissionResult {
        GRANTED, DECLINED
    }

}