package com.bori.hipe.controllers.fragments.root

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bori.hipe.R
import com.bori.hipe.controllers.fragments.base.HipeBaseFragment
import com.bori.hipe.controllers.rest.callback.RestCallback
import com.bori.hipe.controllers.rest.callback.RestCallbackRepository
import com.bori.hipe.controllers.rest.service.EventService
import com.bori.hipe.controllers.rest.service.UserService
import com.bori.hipe.models.Event
import com.bori.hipe.models.Image
import com.bori.hipe.models.User
import com.bori.hipe.util.Const
import com.bori.hipe.util.extensions.setContentView
import com.bori.hipe.util.web.Status
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.assist.ImageScaleType
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_user.*

class UserFragment : HipeBaseFragment() {

    companion object {
        private const val TAG = "UserFragment"
        private const val CROSSFADE_DURATION: Long = 500
        private const val CROSSFADE_DELAY: Long = 1500

        private const val GET_USER_BY_ID = 12
        private const val GET_EVENTS_BY_MEMBER_ID = 14
        private const val GET_FRIENDS_LIST_ID = 16
        private const val ADD_USER_TO_FRIEND_ID = 13
        private const val RECYCLER_VIEW_ANIMATION_DURATION = 100L

    }

    lateinit var displayImageOptions: DisplayImageOptions
    private lateinit var dataAdapter: DataAdapter

    private enum class DataType {
        NONE, TYPE_USERS, TYPE_EVENTS
    }

    private var userID: Int = 0
    private val imageLoader = ImageLoader.getInstance()
    private var dataType = DataType.TYPE_USERS

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        android.util.Log.d(TAG, "UserFragment.onCreateView")

        setContentView(R.layout.activity_user, inflater, container)
        init()

        userID = arguments!![Const.ADVANCED_USER_ID] as Int

        RestCallbackRepository.registerCallback(restCallback)
        UserService.getById(GET_USER_BY_ID, userID)
        EventService.getByMemberID(GET_EVENTS_BY_MEMBER_ID, userID)
        UserService.getFriends(GET_FRIENDS_LIST_ID, userID)

        return rootView
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d(TAG, "UserFragment.onDestroy")
        RestCallbackRepository.unregisterCallback(restCallback)
    }

    private fun init() {

        Log.d(TAG, "UserFragment.init")
        displayImageOptions = DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED).build()

        user_activity_data_view.layoutManager = androidx.recyclerview.widget.StaggeredGridLayoutManager(2, androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL)
        dataAdapter = DataAdapter()
        user_activity_data_view.adapter = dataAdapter
        user_activity_data_view.setOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                user_photo.imageMatrix.postScale(0.5f, 0.5f)
                user_photo.invalidate()
            }
        })

        user_activity_button_follow.setOnClickListener(this::onClick)
        user_activity_button_invite.setOnClickListener(this::onClick)
        user_activity_button_show_events.setOnClickListener(this::onClick)
        user_activity_button_show_contacts.setOnClickListener(this::onClick)
        follow_indicator.setOnClickListener(this::onClick)
        user_contacts_indicator.setOnClickListener(this::onClick)
        user_events_indicator.setOnClickListener(this::onClick)
        button_back.setOnClickListener(this::onClick)

    }

    fun onClick(v: View) {

        val id = v.id


        Log.d(TAG, "UserFragment.onClick")
        if (id == R.id.user_activity_button_invite || id == R.id.invite_indicator) {
            if (id == R.id.user_activity_button_invite)
                invite_indicator.callOnClick()

        } else if (id == R.id.user_activity_button_follow || id == R.id.follow_indicator) {

        } else if (id == R.id.user_activity_button_show_events || id == R.id.user_events_indicator) {

            animateDataView()
            dataType = DataType.TYPE_EVENTS
            user_contacts_indicator.isSelected = false
            user_events_indicator.setColorFilter(ContextCompat.getColor(context!!, R.color.colorAccent))
        } else if (id == R.id.user_activity_button_show_contacts || id == R.id.user_contacts_indicator) {
            animateDataView()
            dataType = DataType.TYPE_USERS
            user_contacts_indicator.isSelected = true
            user_events_indicator.setColorFilter(ContextCompat.getColor(context!!, R.color.colorPrimaryDark))
        } else if (id == R.id.button_back)
            activity?.onBackPressed()

    }

    private fun animateDataView() {
        Log.d(TAG, "UserFragment.animateDataView")

        user_activity_data_view.animate()
                .alpha(0f)
                .setDuration(RECYCLER_VIEW_ANIMATION_DURATION)
                .setListener(recyclerViewAnimationListener).startDelay = 0

    }

    private var recyclerViewAnimationListener: AnimatorListenerAdapter = object : AnimatorListenerAdapter() {

        override fun onAnimationEnd(animation: Animator) {

            Log.d(TAG, "UserFragment.onAnimationEnd")
            super.onAnimationEnd(animation)

            dataAdapter.notifyDataSetChanged()
            user_activity_data_view.animate()
                    .setDuration(RECYCLER_VIEW_ANIMATION_DURATION)
                    .alpha(1f)
                    .setListener(null).startDelay = 0
        }

    }

    private var restCallback: RestCallback = object : RestCallback() {

        override fun onFailure(requestID: Int, t: Throwable) {

            Log.d(TAG, "UserFragment.onFailure")
            Log.d(TAG, "onFailure() called with: t = [$t]")

        }

        override fun onUserResponse(requestID: Int, user: Pair<User, Image>?, responseStatus: Int) {

            android.util.Log.d(TAG, "UserFragment.onUserResponse")

            Log.d(TAG, "onUserResponse() called with: status = [$responseStatus]")

            user ?: return

            val loadedUser = user.first
            imageLoader.displayImage(user.second.urlMedium, user_photo, displayImageOptions)
            nickname.text = loadedUser.username
            name_surname.text = "${loadedUser.name} ${loadedUser.surname}"
            user_status.text = loadedUser.status

        }

        override fun onEventResponse(requestID: Int, events: List<Pair<Event, Image>>?, responseStatus: Int) {

            android.util.Log.d(TAG, "UserFragment.onEventResponse")


            if (requestID == GET_EVENTS_BY_MEMBER_ID) {

                if (responseStatus == Status.OK) {
                    dataAdapter.events.clear()
                    dataAdapter.events.addAll(events ?: emptyList())
                    events_count.text = events?.size.toString()
                    dataAdapter.notifyDataSetChanged()
                }

            }

        }

        override fun onUserResponse(requestID: Int, users: List<Pair<User, Image>>?, responseStatus: Int) {

            Log.d(TAG, "UserFragment.onUserResponse")

            if (requestID == GET_FRIENDS_LIST_ID) {

                if (responseStatus == Status.OK) {
                    dataAdapter.users.clear()
                    dataAdapter.users.addAll(users ?: emptyList())
                    dataAdapter.notifyDataSetChanged()
                }

            }

        }

        override fun onSimpleResponse(requestID: Int, response: Any?, responseStatus: Int) {
            Log.d(TAG, "UserFragment.onSimpleResponse")
            if (responseStatus == Status.OK) {
                //user added
                follow_indicator.isSelected = true
                Toast.makeText(context, "пользователь законтачен", Toast.LENGTH_SHORT).show()
            }

            if (responseStatus == Status.ACCEPTED) {
                //user removed
                follow_indicator.isSelected = false
            }

        }

    }

    private inner class DataAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<DataAdapter.VH>() {

        val events = arrayListOf<Pair<Event, Image>>()
        val users = arrayListOf<Pair<User, Image>>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {

            android.util.Log.d(TAG, "DataAdapter.onCreateViewHolder")


            if (dataType == DataType.TYPE_USERS) {
                return VH(layoutInflater.inflate(R.layout.item_row_user_small, parent, false), dataType)
            }
            if (dataType == DataType.TYPE_EVENTS) {
                return VH(layoutInflater.inflate(R.layout.item_row_event_small, parent, false), dataType)
            }

            return VH(View(context), dataType)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            android.util.Log.d(TAG, "DataAdapter.onBindViewHolder")

            when (dataType) {

                DataType.TYPE_USERS -> {
                    val user = users[position]
                    holder.userNick.text = user.first.username
                    holder.userNameSurname.text = user.first.name + " " + user.first.surname

                }
                DataType.TYPE_EVENTS -> {
                    val event = events[position]
                    holder.eventName.text = event.first.localName

                }

                DataType.NONE -> Log.e(TAG, "Fuck")

            }

            val image = if (dataType == DataType.TYPE_EVENTS)
                events[position].second
            else
                users[position].second

            imageLoader.displayImage(image.urlSmall, holder.userPhoto, displayImageOptions)

        }

        override fun getItemCount(): Int {
            Log.d(TAG, "DataAdapter.getItemCount")
            return when (dataType) {

                DataType.TYPE_EVENTS -> events.size
                DataType.TYPE_USERS -> users.size
                else -> 10
            }

        }

        override fun getItemViewType(position: Int) = dataType.ordinal

        internal inner class VH(rootView: View, dataType: DataType) : androidx.recyclerview.widget.RecyclerView.ViewHolder(rootView) {

            lateinit var userPhoto: CircleImageView
            lateinit var userNick: TextView
            lateinit var userNameSurname: TextView

            lateinit var eventImage1: ImageView
            lateinit var eventImage2: ImageView
            lateinit var eventName: TextView
            lateinit var photoCount: TextView

            lateinit var imageLoadingListener: ImageLoadingListener
            lateinit var myAnimatorListener: MyAnimatorListener

            init {

                if (dataType == DataType.TYPE_EVENTS) {

                    eventImage1 = rootView.findViewById(R.id.event_photo1)
                    eventImage1.tag = 1
                    eventImage2 = rootView.findViewById(R.id.event_photo2)
                    eventImage2.tag = null
                    eventName = rootView.findViewById(R.id.event_name)
                    photoCount = rootView.findViewById(R.id.event_photo_count)

                    myAnimatorListener = MyAnimatorListener()

                    imageLoadingListener = object : ImageLoadingListener {

                        override fun onLoadingStarted(s: String, view: View) {

                            Log.d(TAG, "VH.onLoadingStarted")
                            Log.d(TAG, "Loading started")
                        }

                        override fun onLoadingFailed(s: String, view: View, failReason: FailReason) {
                            android.util.Log.d(TAG, "VH.onLoadingFailed")
                        }

                        override fun onLoadingComplete(s: String, view: View, bitmap: Bitmap) {
                            android.util.Log.d(TAG, "VH.onLoadingComplete")

                            if (view.tag != null) {
                                myAnimatorListener.imageView = eventImage2
                                eventImage1.setImageBitmap(bitmap)
                                eventImage1.animate().alpha(1f).setStartDelay(CROSSFADE_DELAY).setDuration(CROSSFADE_DURATION).setListener(null)
                                eventImage2.animate().alpha(0f).setStartDelay(CROSSFADE_DELAY).setDuration(CROSSFADE_DURATION).setListener(myAnimatorListener)
                            } else {
                                myAnimatorListener.imageView = eventImage1
                                eventImage2.setImageBitmap(bitmap)
                                eventImage2.animate().alpha(1f).setStartDelay(CROSSFADE_DELAY).setDuration(CROSSFADE_DURATION).setListener(null)
                                eventImage1.animate().alpha(0f).setStartDelay(CROSSFADE_DELAY).setDuration(CROSSFADE_DURATION).setListener(myAnimatorListener)

                            }

                        }

                        override fun onLoadingCancelled(s: String, view: View) {
                            Log.d(TAG, "VH.onLoadingCancelled")

                        }

                    }

                } else if (dataType == DataType.TYPE_USERS) {

                    userPhoto = rootView.findViewById(R.id.user_photo)
                    userNick = rootView.findViewById(R.id.nickname)
                    userNameSurname = rootView.findViewById(R.id.user_name_surname)

                }

            }

            private inner class MyAnimatorListener : AnimatorListenerAdapter() {

                var imageView: ImageView? = null

                override fun onAnimationEnd(animation: Animator) {

                    Log.d(TAG, "MyAnimatorListener.onAnimationEnd")
                    super.onAnimationEnd(animation)

                    if (imageView != null)
                        imageView?.setImageBitmap(null)

                }
            }
        }
    }


}