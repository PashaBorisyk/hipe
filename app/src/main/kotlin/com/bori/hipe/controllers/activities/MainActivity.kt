package com.bori.hipe.controllers.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.bori.hipe.controllers.fragments.base.HipeBaseFragment
import com.bori.hipe.controllers.fragments.root.LoginFragment


class MainActivity : AppCompatActivity(){

    //used to calculate angle to rotate big fab
    companion object {
        private var CONTENT_VIEW_ID = 10101010
        private const val TAG = "MainActivity.kt"
    }

    private lateinit var rootView:RelativeLayout
    private lateinit var visibleStageView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootView = RelativeLayout(this)
        setContentView(rootView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT))
        val fragmentStage = FrameLayout(this)
        fragmentStage.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT)
        fragmentStage.id = CONTENT_VIEW_ID
        visibleStageView = fragmentStage
        rootView.addView(fragmentStage)
        supportFragmentManager
                .beginTransaction()
                .add(CONTENT_VIEW_ID,LoginFragment())
                .commit()

    }

    fun showNextFragment(hipeBaseFragment: HipeBaseFragment,destroyCurrent:Boolean = true){
        Log.d(TAG,"showNextFragment()")
        val stageView = FrameLayout(this)
        stageView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT)
        stageView.id = CONTENT_VIEW_ID++
        hipeBaseFragment.stageView = stageView
        rootView.addView(stageView,1,ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT))
        rootView.requestLayout()
        stageView.requestLayout()
        supportFragmentManager
                .beginTransaction()
                .add(stageView.id,hipeBaseFragment)
                .commit()

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