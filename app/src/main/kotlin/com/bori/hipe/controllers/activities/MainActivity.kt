package com.bori.hipe.controllers.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bori.hipe.controllers.fragments.root.LoginFragment

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(){

    //used to calculate angle to rotate big fab
    companion object {
        private const val LOGIN_FRAGMENT_TAG = "LOGIN_FRAGMENT_TAG"
        private const val CONTENT_VIEW_ID = 10101010
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rootView = FrameLayout(this)
        rootView.id = CONTENT_VIEW_ID
        setContentView(rootView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT))

    }

    override fun onResume() {
        super.onResume()
        supportFragmentManager.beginTransaction().add(CONTENT_VIEW_ID,LoginFragment()).commit()
    }

}