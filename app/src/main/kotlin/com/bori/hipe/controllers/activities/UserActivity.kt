package com.bori.hipe.controllers.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bori.hipe.R
import com.bori.hipe.controllers.rest.RestService
import com.bori.hipe.controllers.rest.callbacks.RestCallbackAdapter
import com.bori.hipe.controllers.rest.service.EventService
import com.bori.hipe.controllers.rest.service.UserService
import com.bori.hipe.models.Event
import com.bori.hipe.models.HipeImage
import com.bori.hipe.models.Tuple
import com.bori.hipe.models.User
import com.bori.hipe.util.Const
import com.bori.hipe.util.Status
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.assist.ImageScaleType
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_user.*

private const val CROSSFADE_DURATION: Long = 500
private const val CROSSFADE_DELAY: Long = 1500

private const val TAG = "UserActivity"
private const val GET_USER_BY_ID = 12L
private const val GET_EVENTS_BY_MEMBER_ID = 14L
private const val GET_FRIENDS_LIST_ID = 16L
private const val ADD_USER_TO_FRIEND_ID = 13L
private const val RECYCLER_VIEW_ANIMATION_DURATION = 100L

class UserActivity : AppCompatActivity() {

    lateinit var displayImageOptions: DisplayImageOptions
    private lateinit var dataAdapter: DataAdapter

    private enum class DataType {
        NONE, TYPE_USERS, TYPE_EVENTS
    }

    private var userID: Long = 0
    private val imageLoader = ImageLoader.getInstance()
    private var data_type = DataType.TYPE_USERS

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        init()

        userID = intent.getLongExtra(Const.ADVANCED_USER_ID, -1)

        RestService.registerCallback(restCallbackAdapter)
        UserService.getUserById(GET_USER_BY_ID, userID)
        EventService.getByMemberId(GET_EVENTS_BY_MEMBER_ID, userID)
        UserService.getFriendsList(GET_FRIENDS_LIST_ID, userID)

    }

    override fun onDestroy() {
        super.onDestroy()
        RestService.unregisterCallback(restCallbackAdapter)
    }

    private fun init() {

        displayImageOptions = DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED).build()

        user_activity_data_view.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        dataAdapter = DataAdapter()
        user_activity_data_view.adapter = dataAdapter
        user_activity_data_view.setOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
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

        if (id == R.id.user_activity_button_invite || id == R.id.invite_indicator) {
            if (id == R.id.user_activity_button_invite)
                invite_indicator.callOnClick()

        } else if (id == R.id.user_activity_button_follow || id == R.id.follow_indicator) {

            UserService.addUserToFriend(requestID = ADD_USER_TO_FRIEND_ID,
                    userId = User.thisUser.id,
                    advancedUserId = userID)

        } else if (id == R.id.user_activity_button_show_events || id == R.id.user_events_indicator) {

            animateDataView()
            data_type = DataType.TYPE_EVENTS
            user_contacts_indicator.isSelected = false
            user_events_indicator.setColorFilter(ContextCompat.getColor(applicationContext, R.color.colorAccent))
        } else if (id == R.id.user_activity_button_show_contacts || id == R.id.user_contacts_indicator) {
            animateDataView()
            data_type = DataType.TYPE_USERS
            user_contacts_indicator.isSelected = true
            user_events_indicator.setColorFilter(ContextCompat.getColor(applicationContext, R.color.colorPrimaryDark))
        } else if (id == R.id.button_back)
            onBackPressed()

    }

    private fun animateDataView() {

        user_activity_data_view.animate()
                .alpha(0f)
                .setDuration(RECYCLER_VIEW_ANIMATION_DURATION)
                .setListener(recyclerViewAnimationListener).startDelay = 0

    }

    private var recyclerViewAnimationListener: AnimatorListenerAdapter = object : AnimatorListenerAdapter() {

        override fun onAnimationEnd(animation: Animator) {
            super.onAnimationEnd(animation)

            dataAdapter.notifyDataSetChanged()
            user_activity_data_view.animate()
                    .setDuration(RECYCLER_VIEW_ANIMATION_DURATION)
                    .alpha(1f)
                    .setListener(null).startDelay = 0
        }

    }

    private var restCallbackAdapter: RestCallbackAdapter = object : RestCallbackAdapter() {

        override fun onFailure(requestID: Long, t: Throwable) {
            Log.d(TAG, "onFailure() called with: t = [$t]")

        }

        override fun onOk(requestID: Long) {
            Log.d(TAG, "onOk() called")
        }

        override fun onUserResponse(requestID: Long, user: Tuple<User, HipeImage>?, serverStatus: Int) {

            Log.d(TAG, "onUserListResponse() called with: status = [$serverStatus]")

            user ?: return

            val loadedUser = user._1
            imageLoader.displayImage(user._2.urlMedium, user_photo, displayImageOptions)
            nickname.text = loadedUser.nickName
            name_surname.text = "${loadedUser.name} ${loadedUser.surname}"
            user_status.text = loadedUser.status

        }

        override fun onEventListResponse(requestID: Long, events: List<Tuple<Event, HipeImage>>?, serverStatus: Int) {
            Log.d(TAG, "onEventListResponse requestID = [${requestID}], events = [${events}], serverStatus = [${serverStatus}]")

            if (requestID == GET_EVENTS_BY_MEMBER_ID) {

                if (serverStatus == Status.OK) {
                    dataAdapter.events.clear()
                    dataAdapter.events.addAll(events ?: emptyList())
                    events_count.text = events?.size.toString()
                    dataAdapter.notifyDataSetChanged()
                }

            }

        }

        override fun onUserListResponse(requestID: Long, users: List<Tuple<User, HipeImage>>?, serverStatus: Int) {
            Log.d(TAG, "onUserListResponse requestID = [${requestID}], events = [${users}], serverStatus = [${serverStatus}]")

            if (requestID == GET_FRIENDS_LIST_ID) {

                if (serverStatus == Status.OK) {
                    dataAdapter.users.clear()
                    dataAdapter.users.addAll(users ?: emptyList())
                    dataAdapter.notifyDataSetChanged()
                }

            }

        }

        override fun onSimpleResponse(requestID: Long, response: Any?, serverCode: Int) {
            Log.d(TAG, "onLongListResponse() called with: strings = ")

            if (serverCode == Status.OK) {
                //user added
                follow_indicator.isSelected = true
                Toast.makeText(this@UserActivity, "пользователь законтачен", Toast.LENGTH_SHORT).show()
            }

            if (serverCode == Status.ACCEPTED) {
                //user removed
                follow_indicator.isSelected = false
            }

        }

    }

    private inner class DataAdapter : RecyclerView.Adapter<DataAdapter.VH>() {

        val events = arrayListOf<Tuple<Event, HipeImage>>()
        val users = arrayListOf<Tuple<User, HipeImage>>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {

            if (data_type == DataType.TYPE_USERS) {
                return VH(layoutInflater.inflate(R.layout.item_row_user_small, parent, false), data_type)
            }
            if (data_type == DataType.TYPE_EVENTS) {
                return VH(layoutInflater.inflate(R.layout.item_row_event_small, parent, false), data_type)
            }

            return VH(View(applicationContext),data_type)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {

            when (data_type) {

                DataType.TYPE_USERS -> {
                    val user = users[position]
                    holder.userNick.text = user._1.nickName
                    holder.userNameSurname.text = user._1.name + " " + user._1.surname

                }
                DataType.TYPE_EVENTS -> {
                    val event = events[position]
                    holder.eventName.text = event._1.localName

                }

                DataType.NONE -> Log.e(TAG, "Fuck")

            }

            val image = if (data_type == DataType.TYPE_EVENTS)
                events[position]._2
            else
                users[position]._2

            imageLoader.displayImage(image.urlSmall, holder.userPhoto, displayImageOptions)

        }

        override fun getItemCount() = when (data_type) {
            DataType.TYPE_EVENTS -> events.size
            DataType.TYPE_USERS -> users.size
            else -> 10
        }

        override fun getItemViewType(position: Int) = data_type.ordinal

        internal inner class VH(rootView: View, dataType: DataType) : RecyclerView.ViewHolder(rootView) {

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
                            Log.d(TAG, "Loading started")
                        }

                        override fun onLoadingFailed(s: String, view: View, failReason: FailReason) {
                            Log.d(TAG, "Loading finished")
                        }

                        override fun onLoadingComplete(s: String, view: View, bitmap: Bitmap) {

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

                        override fun onLoadingCancelled(s: String, view: View) {}

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
                    super.onAnimationEnd(animation)

                    if (imageView != null)
                        imageView?.setImageBitmap(null)

                }
            }
        }
    }
}