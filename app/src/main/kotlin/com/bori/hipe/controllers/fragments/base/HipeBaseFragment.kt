package com.bori.hipe.controllers.fragments.base

import android.support.v4.app.Fragment
import android.util.Log
import android.view.View

open class HipeBaseFragment : Fragment() {

    private companion object {
        private const val TAG = "HipeBaseFragment"
    }

    lateinit var rootView: View
    lateinit var stageView: View

    var shouldCallOnFragment:Boolean = false
        protected set

    open fun onBackPressed(){
        Log.d(TAG, "HipeBaseFragment.onBackPressed")
        shouldCallOnFragment = false

    }

}