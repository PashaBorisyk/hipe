package com.bori.hipe.controllers.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Dialog
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.Bundle
import android.os.IBinder
import android.os.Vibrator
import android.support.design.widget.TabLayout
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ImageButton
import com.bori.hipe.HipeApplication
import com.bori.hipe.R
import com.bori.hipe.controllers.animators.TabViewBackgroundAnimation
import com.bori.hipe.controllers.fragments.AllChatsFragment
import com.bori.hipe.controllers.fragments.FragmentNewsFeed
import com.bori.hipe.controllers.fragments.SearchFragment
import com.bori.hipe.controllers.services.HipeService
import com.bori.hipe.controllers.views.BubblesView
import com.bori.hipe.util.Const
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "MainActivity"
private const val SCALE_TIME = 1.45f
private const val FAB_ANIMATION_DURATION = 150L
private const val FAB_ANIMATION_DELAY = 100L

class MainActivity : AppCompatActivity(), View.OnClickListener, ServiceConnection {

    var hipeService: HipeService? = null
        private set

    private var oldPosition: Int = 0

    private lateinit var fragments: Array<Fragment>
    private lateinit var views: Array<View>
    private lateinit var backgroundViews: Array<View>
    private lateinit var itemIndicators: Array<ImageButton>
    private lateinit var bubblesView: Array<BubblesView>

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var scaleAnimationIn: TabViewBackgroundAnimation
    private lateinit var scaleAnimationOut: TabViewBackgroundAnimation

    //used to calculate angle to rotate big fab
    private var pressCount: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences(Const.HIPE_APPLICATION_SHARED_PREFERENCES, MODE_PRIVATE)
//        if (sharedPreferences.getLong(Const.USER_ID, -1L) == -1L) {
//            Log.e(TAG, "onCreate: User not exists ")
//            startActivity(Intent(this, LoginActivity::class.java))
//            finish()
//        }

        Log.d(TAG, "onCreate: data: user - " + HipeApplication.NICKNAME)

        try {
            bindService(Intent(this, HipeService::class.java), this, BIND_AUTO_CREATE)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContentView(R.layout.activity_main)
        init()
    }

    override fun onPause() {
        super.onPause()
        fab_normal_new.animate().rotation(0f)
        pressCount = 0
    }

    override fun onResume() {
        super.onResume()
        if (fab_normal_new.visibility == View.INVISIBLE)
            fab_normal_new.visibility = View.VISIBLE

    }

    private fun init() {
        initTabs()
        initFabs()
        fab_normal_new.visibility = View.INVISIBLE
    }

    private fun initTabs() {

        val fragmentNewsFeed = FragmentNewsFeed()
        val searchFragment = SearchFragment()
        val fragmentNewsFeed2 = FragmentNewsFeed()
        val fragmentNewsFeed3 = FragmentNewsFeed()
        val allChatsFragment = AllChatsFragment()

        fragments = arrayOf(allChatsFragment, fragmentNewsFeed, fragmentNewsFeed2, searchFragment, fragmentNewsFeed3)
        views = Array(fragments.size) { return@Array initHostView(it) }
        bubblesView = Array(fragments.size) {
            val v = views[it].findViewById(R.id.bubbles) as BubblesView
            v.speed = 0.08f
            v.setBubblesCount(30)
            v.setSetBigRadiusWithViewSizes(true)
            return@Array v
        }

        itemIndicators = Array(fragments.size) {

            val v = views[it].findViewById(R.id.item_icon) as ImageButton
            v.isSelected = false
            v.tag = it
            (v.parent as View).tag = it
            v.setOnClickListener(this)
            (v.parent as View).setOnClickListener(this)
            return@Array v

        }
        backgroundViews = Array(fragments.size) {
            val v = views[it].findViewById<View>(R.id.background_circle_view)
            v.tag = it
            v.setOnClickListener(this)
            return@Array v
        }

        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)

        main_view_pager.adapter = viewPagerAdapter
        main_tab_layout.setupWithViewPager(main_view_pager, true)
        views.forEachIndexed { index, view ->
            main_tab_layout.getTabAt(index)?.customView = view
        }

        scaleAnimationIn = TabViewBackgroundAnimation(TabViewBackgroundAnimation.TYPE.SCALE_IN, SCALE_TIME)
        scaleAnimationIn.duration = 120
        scaleAnimationOut = TabViewBackgroundAnimation(TabViewBackgroundAnimation.TYPE.SCALE_OUT, SCALE_TIME)
        scaleAnimationOut.duration = 120

        oldPosition = fragments.size / 2
        backgroundViews[oldPosition].scaleX = SCALE_TIME
        backgroundViews[oldPosition].scaleY = SCALE_TIME
        main_tab_layout.addOnTabSelectedListener(tabSelectedListener)
        main_view_pager.setCurrentItem(fragments.size / 2, false)

        itemIndicators[2].isActivated = true
        //        bubblesView[2].startEffects();

    }

    private fun initFabs() {

        fab_mini_new_event.setOnClickListener(this)
        fab_mini_new_message.setOnClickListener(this)
        fab_normal_new.setOnClickListener(this)
        view_tint_main.setOnClickListener(this)

        fab_normal_new.isActivated = false
        fab_normal_new.setOnLongClickListener {
            Log.d(TAG, "onLongClick: ")

            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(300)
            //                wave_view.setRight(fab_normal_new.getRight()+wave_view.getWidth()/2);
            //                wave_view.setLeft(fab_normal_new.getLeft() - wave_view.getWidth()/2);
            //                wave_view.setTop(fab_normal_new.getTop() - wave_view.getHeight()/2);
            //                wave_view.setBottom(fab_normal_new.getBottom() + wave_view.getHeight()/2);
            wave_view.startAnim()
            showNewQuickEventDialog()

            true
        }

    }

    private fun showNewQuickEventDialog() {}

    private fun initHostView(i: Int) = when (i) {

        0 -> layoutInflater.inflate(R.layout.item_main_activity_events, main_tab_layout, false)
        1 -> layoutInflater.inflate(R.layout.item_main_activity_chats, main_tab_layout, false)
        2 -> layoutInflater.inflate(R.layout.item_main_activity_camera, main_tab_layout, false)
        3 -> layoutInflater.inflate(R.layout.item_main_activity_search, main_tab_layout, false)
        4 -> layoutInflater.inflate(R.layout.item_main_activity_map, main_tab_layout, false)
        else -> throw IndexOutOfBoundsException("Such number of tabs is illegal")
    }

    private fun animateFabsOn() {

        Log.d(TAG, "")

        fab_normal_new.isActivated = true
        view_tint_main.visibility = View.VISIBLE
        fab_mini_new_message.visibility = View.VISIBLE
        fab_mini_new_event.visibility = View.VISIBLE

        view_tint_main.animate()
                .alpha(0.7f).duration = FAB_ANIMATION_DURATION

        fab_normal_new.animate()
                .rotation((pressCount * 45).toFloat()).duration = FAB_ANIMATION_DURATION + 100

        fab_mini_new_event.animate()
                .alpha(1f)
                .translationY(-HipeApplication.pixelsPerDp * 60)
                .setStartDelay(FAB_ANIMATION_DELAY).duration = FAB_ANIMATION_DURATION

        fab_mini_new_message.animate()
                .alpha(1f)
                .translationY(-HipeApplication.pixelsPerDp * 42)
                .translationX(-HipeApplication.pixelsPerDp * 42)
                .setStartDelay(FAB_ANIMATION_DELAY).duration = FAB_ANIMATION_DURATION

//        main_tab_layout.animate()
//                .alpha(0f)
//                .translationY(HipeApplication.pixelsPerDp * 48).duration = FAB_ANIMATION_DURATION

    }

    private fun animateFabsOff() {

        fab_normal_new.isActivated = false

        view_tint_main.animate()
                .alpha(0.0f).duration = FAB_ANIMATION_DURATION

        fab_normal_new.animate()
                .rotation((pressCount * 45).toFloat())
                .setDuration(FAB_ANIMATION_DURATION)
                .setListener(fabAnimatorListener)

        fab_mini_new_event.animate()
                .alpha(0f)
                .translationY(0f)
                .setDuration(FAB_ANIMATION_DURATION).startDelay = FAB_ANIMATION_DELAY

        fab_mini_new_message.animate()
                .alpha(0f)
                .translationY(0f)
                .translationX(0f)
                .setDuration(FAB_ANIMATION_DURATION).startDelay = FAB_ANIMATION_DELAY

//        main_tab_layout.animate()
//                .alpha(1f)
//                .translationY(0f).duration = FAB_ANIMATION_DURATION

    }

    var fabAnimatorListener: Animator.AnimatorListener = object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            super.onAnimationEnd(animation)

            if (!fab_normal_new.isActivated) {
                view_tint_main.visibility = View.GONE
                fab_mini_new_message.visibility = View.GONE
                fab_mini_new_event.visibility = View.GONE
            }
        }
    }

    var tabSelectedListener: TabLayout.OnTabSelectedListener = object : TabLayout.OnTabSelectedListener {

        override fun onTabSelected(tab: TabLayout.Tab) {
            Log.d(TAG, "onTabSelected: ")
            animateButtons(tab.position)
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {
            Log.d(TAG, "onTabUnselected: ")
        }

        override fun onTabReselected(tab: TabLayout.Tab) {
            Log.d(TAG, "onTabReselected: ")
        }

    }

    private fun animateButtons(newPosition: Int) {
        Log.d(TAG, "animateButtons: ")

        if (newPosition != oldPosition) {

            Log.d(TAG, "onPageSelected() called with: newPosition = [$newPosition]$oldPosition")

            scaleAnimationIn.animatedView = backgroundViews[newPosition]
            scaleAnimationOut.animatedView = backgroundViews[oldPosition]
            backgroundViews[oldPosition].startAnimation(scaleAnimationOut)
            backgroundViews[newPosition].startAnimation(scaleAnimationIn)
            itemIndicators[oldPosition].isActivated = false

        }

        for (i in fragments.indices) {
            itemIndicators[i].isActivated = i == newPosition

        }

        itemIndicators[newPosition].isActivated = true
        oldPosition = newPosition

    }

    override fun onClick(view: View) {
        Log.d(TAG, "onClick: ")

        if (view.tag != null && view.tag is Int)
            main_view_pager.currentItem = view.tag as Int

        when (view.id) {

            R.id.fab_normal_new -> {
                pressCount++
                if (fab_normal_new.isActivated) animateFabsOff()
                else
                    animateFabsOn()
            }

            R.id.fab_mini_new_event -> {
                animateFabsOff()
                startActivity(Intent(this, CreateNewEventActivity::class.java))
            }

        }

    }

    override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
        Log.d(TAG, "onServiceConnected: ")

        Log.d(TAG, "onServiceConnected: service inited")
        hipeService = (iBinder as HipeService.MyBinder).hipeService

    }

    override fun onServiceDisconnected(componentName: ComponentName) {

    }

    private inner class ViewPagerAdapter(
            fm: FragmentManager
    ) : FragmentStatePagerAdapter(fm) {

        init {
            Log.d(TAG, "ViewPagerAdapter: ")
        }

        override fun getItem(position: Int): Fragment {
            Log.d(TAG, "getItem: ")
            return fragments[position]
        }

        override fun getCount() = fragments.size
    }

    class QuickEventDialog : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return Dialog(activity)
        }
    }
}