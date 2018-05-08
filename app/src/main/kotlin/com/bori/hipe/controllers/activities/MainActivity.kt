package com.bori.hipe.controllers.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bori.hipe.controllers.fragments.base.HipeBaseFragment
import com.bori.hipe.controllers.fragments.root.LoginFragment


class MainActivity : AppCompatActivity(){

    //used to calculate angle to rotate big fab
    companion object {
        private const val CONTENT_VIEW_ID = 10101010
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rootView = FrameLayout(this)
        rootView.id = CONTENT_VIEW_ID
        setContentView(rootView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT))
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(CONTENT_VIEW_ID,LoginFragment()).commit()

    }

    override fun onBackPressed() {
        if(supportFragmentManager.fragments.isEmpty())
            super.onBackPressed()
        else {
            val found = supportFragmentManager.fragments.count{
                if(it is HipeBaseFragment){
                    if(it.shouldCallOnFragment) {
                        it.onBackPressed()
                        return@count true
                    }
                }
                return@count false
            }
            Log.d(TAG,"Fragments for back pressed in queue : $found")
            if (found == 0)
                super.onBackPressed()
        }
    }

}