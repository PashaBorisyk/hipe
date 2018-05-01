package com.bori.hipe.controllers.fragments

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bori.hipe.HipeApplication
import com.bori.hipe.HipeApplication.Companion.IS_KIT_KAT
import com.bori.hipe.HipeApplication.Companion.IS_LOLLIPOP
import com.bori.hipe.R
import com.bori.hipe.controllers.rest.RestService
import com.bori.hipe.controllers.rest.callbacks.RestCallbackAdapter
import com.bori.hipe.controllers.rest.service.EventService
import com.bori.hipe.models.Event
import com.bori.hipe.models.HipeImage
import com.bori.hipe.models.Tuple
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.ImageScaleType

private const val TAG = "FragmentNewsFeed.kt"
private const val GET_EVENTS_ID = 1L

class FragmentNewsFeed : Fragment() {

    private var test = false

    private lateinit var v: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewAdapter: RecyclerViewAdapter
    private lateinit var views: LinkedHashMap<View, View>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView: ")

        views = linkedMapOf()

        v = inflater.inflate(R.layout.fragment_news_feed, container, false)
        recyclerView = findViewById(R.id.news_feed_recycler_view)
        recyclerViewAdapter = RecyclerViewAdapter()
        val linearLayoutManager = LinearLayoutManager(activity!!.applicationContext)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = recyclerViewAdapter

        val halfOfScreen = HipeApplication.screenHeight / 2
        val doubledMargin = 15 * HipeApplication.pixelsPerDp * 2
        val startWidth = HipeApplication.screenWidth - doubledMargin
        val maxMargin = doubledMargin * 0.95f

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                for ((holder, image) in views) {

                    holder.requestLayout()
                    image.requestLayout()
                    image.layoutParams.width = (
                            startWidth +
                                    Math.cos(
                                            (holder.y + image.height / 2 - halfOfScreen)
                                                    / halfOfScreen.toDouble()
                                    ) * maxMargin)
                            .toInt()
                    image.requestLayout()
                }

            }
        })

        RestService.registerCallback(recyclerViewAdapter.restCallbackAdapter)
        recyclerViewAdapter.update()

        return v
    }

    override fun onDestroyView() {
        super.onDestroyView()
        RestService.unregisterCallback(recyclerViewAdapter.restCallbackAdapter)
    }

    private fun test() {
    }

    private fun <T : View> findViewById(id: Int) = v.findViewById<T>(id)

    private inner class RecyclerViewAdapter internal constructor() : RecyclerView.Adapter<RecyclerViewAdapter.VH>(), View.OnClickListener {

        private val TYPE_VERTICAL = 0
        private val TYPE_HORIZONTAL = 1
        private val TYPE_EMPTY = 2
        private val TYPE_HEADER = 3

        private val events = mutableListOf<Tuple<Event, HipeImage>>()

        private val layoutInflater: LayoutInflater
        private val imageLoader = ImageLoader.getInstance()
        private val displayImageOptions: DisplayImageOptions

        init {

            layoutInflater = activity!!.layoutInflater

            displayImageOptions = DisplayImageOptions.Builder()
                    .cacheOnDisk(true)
                    .cacheInMemory(true)
                    .imageScaleType(ImageScaleType.NONE_SAFE).build()

        }

        fun update() = EventService.getEvents(
                requestID = GET_EVENTS_ID,
                latitude = 10.0,
                longtitude = 10.0,
                plastReadEventId = 0
        )

        override fun onViewAttachedToWindow(holder: VH) {
            super.onViewAttachedToWindow(holder)
            holder.eventPhoto ?: return
            views[holder.rootView!!] = holder.imageContainer!!
        }

        override fun onViewDetachedFromWindow(holder: VH) {
            super.onViewDetachedFromWindow(holder)
            views.remove(holder.rootView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {

            if (viewType == TYPE_HORIZONTAL)
                return VH(layoutInflater.inflate(R.layout.item_news_feed_horizontal, parent, false), viewType)

            if (viewType == TYPE_VERTICAL)
                return VH(layoutInflater.inflate(R.layout.item_news_feed_vertical, parent, false), viewType)

            if (viewType == TYPE_EMPTY)
                return VH(layoutInflater.inflate(R.layout.item_news_feed_no_image, parent, false), viewType)

            if (viewType == TYPE_HEADER)
                return VH(layoutInflater.inflate(R.layout.status_bar_header, parent, false), viewType)

            return VH(layoutInflater.inflate(R.layout.item_news_feed_no_image, parent, false), viewType)

        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onBindViewHolder(holder: VH, position: Int) {

            if (position == 0)
                return

            val entry = events[position - 1]

            imageLoader.displayImage(entry._1.creatorsImageUrl, holder.userPhoto, displayImageOptions)

            if (getItemViewType(position) != TYPE_EMPTY)
                imageLoader.displayImage(entry._2.urlLarge, holder.eventPhoto, displayImageOptions)

            holder.nickName?.text = entry._1.creatorNickname
            holder.description?.text = entry._1.description

            holder.buttonAdd?.tag = position - 1
            holder.buttonAdd?.setOnClickListener(this)

            holder.buttonChat?.tag = position - 1
            holder.buttonChat?.setOnClickListener(this)

            holder.buttonInfo?.tag = position - 1
            holder.buttonInfo?.setOnClickListener(this)

            if (IS_LOLLIPOP) {
                holder.rootCardView?.clipToOutline = false
                holder.imageCardView?.clipToOutline = false
            }
        }

        override fun getItemViewType(position: Int): Int {

            if (IS_KIT_KAT && position == 0)
                return TYPE_HEADER

            if (!events[position - 1]._2.exist)
                return TYPE_EMPTY

            val ratio = events[position - 1]._2.ratio
            return if (ratio > 1)
                TYPE_HORIZONTAL
            else
                TYPE_VERTICAL
        }

        override fun getItemCount() = events.size + 1

        internal var tag = -1

        override fun onClick(view: View) {

            if (view.tag != null && view.tag is Int)
                tag = view.tag as Int

            when (view.id) {

                R.id.button_expand_info -> {
                }

                R.id.button_chat -> {
                }

                R.id.button_add -> {
                }

                else -> {
                }
            }

        }

        internal inner class VH(itemView: View, itemViewType: Int) : RecyclerView.ViewHolder(itemView) {

            var nickName: TextView? = null
            var description: TextView? = null
            var userPhoto: ImageView? = null
            var buttonAdd: View? = null
            var buttonChat: View? = null
            var buttonInfo: View? = null
            var rootCardView: CardView? = null
            var eventPhoto: ImageView? = null
            var imageContainer: View? = null
            var imageCardView: CardView? = null
            var rootView: View? = null

            init {

                if (itemViewType != TYPE_HEADER) {

                    nickName = itemView.findViewById(R.id.nickname)
                    description = itemView.findViewById(R.id.description_time)
                    userPhoto = itemView.findViewById(R.id.user_photo)
                    buttonAdd = itemView.findViewById(R.id.button_add)
                    buttonChat = itemView.findViewById(R.id.button_chat)
                    buttonInfo = itemView.findViewById(R.id.button_expand_info)
                    rootCardView = itemView.findViewById(R.id.root_card_view)
                    imageCardView = itemView.findViewById(R.id.image_card_view)
                    imageContainer = itemView.findViewById(R.id.image_container)
                    rootView = itemView.findViewById(R.id.root)
                    eventPhoto = if (itemViewType != TYPE_EMPTY)
                        itemView.findViewById(R.id.event_photo)
                    else null
                }
            }
        }

        internal var restCallbackAdapter: RestCallbackAdapter = object : RestCallbackAdapter() {

            override fun onEventListResponse(requestID: Long, events: List<Tuple<Event, HipeImage>>?, serverStatus: Int) {
                super.onEventListResponse(requestID, events, serverStatus)
                Log.d(TAG, "onEventListResponse() called with: events = [$events], status = [$serverStatus]")
                events ?: return

                val oldSize = this@RecyclerViewAdapter.events.size + 1

                var added = 0

                events.forEach {
                    if (!this@RecyclerViewAdapter.events.contains(it)) {
                        this@RecyclerViewAdapter.events.add(it)
                        added++
                    }
                }
                notifyItemRangeInserted(oldSize, this@RecyclerViewAdapter.events.size - oldSize)

            }

            override fun onFailure(requestID: Long, t: Throwable) {
                Log.d(TAG, "onFailure() called with: t = [$t]")
            }

            override fun onOk(requestID: Long) {
                Log.d(TAG, "onOk() called")
            }
        }
    }
}
