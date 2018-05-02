package com.bori.hipe.controllers.fragments.base

import android.support.v4.app.Fragment
import android.view.View

open class HipeBaseFragment : Fragment() {

    lateinit var rootView: View

    var shouldCallOnFragment:Boolean = false
        protected set

    open fun onBackPressed(){
        shouldCallOnFragment = false

    }

}