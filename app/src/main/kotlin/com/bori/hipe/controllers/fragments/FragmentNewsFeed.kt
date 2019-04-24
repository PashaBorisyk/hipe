package com.bori.hipe.controllers.fragments

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bori.hipe.MainApplication
import com.bori.hipe.MainApplication.Companion.IS_KIT_KAT
import com.bori.hipe.MainApplication.Companion.IS_LOLLIPOP
import com.bori.hipe.R
import com.bori.hipe.controllers.rest.callback.RestCallback
import com.bori.hipe.controllers.rest.callback.RestCallbackRepository
import com.bori.hipe.controllers.rest.service.EventService
import com.bori.hipe.models.Event
import com.bori.hipe.models.Image
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.ImageScaleType

private const val TAG = "FragmentNewsFeed.kt"
private const val GET_EVENTS_ID = 1

class FragmentNewsFeed : androidx.fragment.app.Fragment() {

    private enum class RowType {
        VERTICAL,
        HORIZONTAL,
        EMPTY,
        HEADER,
    }

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
        val linearLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity!!.applicationContext)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = recyclerViewAdapter

        val halfOfScreen = MainApplication.screenHeight / 2
        val doubledMargin = 15 * MainApplication.pixelsPerDp * 2
        val startWidth = MainApplication.screenWidth - doubledMargin
        val maxMargin = doubledMargin * 0.95f

        recyclerView.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
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

        RestCallbackRepository.registerCallback(recyclerViewAdapter.RestCallback)
        recyclerViewAdapter.update()

        return v
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "FragmentNewsFeed.onDestroyView")

        RestCallbackRepository.unregisterCallback(recyclerViewAdapter.RestCallback)
    }

    private fun test() {
    }

    private fun <T : View> findViewById(id: Int) = v.findViewById<T>(id)

    private inner class RecyclerViewAdapter internal constructor() : androidx.recyclerview.widget.RecyclerView.Adapter<RecyclerViewAdapter.VH>(), View.OnClickListener {

        private val events = mutableListOf<Pair<Event, Image>>()

        private val layoutInflater: LayoutInflater
        private val imageLoader = ImageLoader.getInstance()
        private val displayImageOptions: DisplayImageOptions

        init {

            Log.d(TAG, "RecyclerViewAdapter.")

            layoutInflater = activity!!.layoutInflater

            displayImageOptions = DisplayImageOptions.Builder()
                    .cacheOnDisk(true)
                    .cacheInMemory(true)
                    .imageScaleType(ImageScaleType.NONE_SAFE).build()

        }

        fun update() = EventService.get(
                requestID = GET_EVENTS_ID,
                latitude = 10.0,
                longitude = 10.0,
                lastReadEventID = 0
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

        override fun onCreateViewHolder(parent: ViewGroup, iViewType: Int): VH {

            val viewType = RowType.values()[iViewType]

            if (viewType == RowType.HORIZONTAL)
                return VH(layoutInflater.inflate(R.layout.item_news_feed_horizontal, parent, false), viewType)

            if (viewType == RowType.VERTICAL)
                return VH(layoutInflater.inflate(R.layout.item_news_feed_vertical, parent, false), viewType)

            if (viewType == RowType.EMPTY)
                return VH(layoutInflater.inflate(R.layout.item_news_feed_no_image, parent, false), viewType)

            if (viewType == RowType.HEADER)
                return VH(layoutInflater.inflate(R.layout.status_bar_header, parent, false), viewType)

            return VH(layoutInflater.inflate(R.layout.item_news_feed_no_image, parent, false), viewType)

        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onBindViewHolder(holder: VH, position: Int) {

            if (position == 0)
                return

            val entry = events[position - 1]

            imageLoader.displayImage(entry.first.creatorsImageUrl, holder.userPhoto, displayImageOptions)

            if (RowType.values()[position] != RowType.EMPTY)
                imageLoader.displayImage(entry.second.urlLarge, holder.eventPhoto, displayImageOptions)

            holder.nickName?.text = entry.first.ownerUsername
            holder.description?.text = entry.first.description

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
                return RowType.HEADER.ordinal

            val ratio = events[position - 1].second.ratio
            return if (ratio > 1)
                RowType.HORIZONTAL.ordinal
            else
                RowType.VERTICAL.ordinal
        }

        override fun getItemCount() = events.size + 1

        internal var tag = -1

        override fun onClick(view: View) {
            Log.d(TAG, "RecyclerViewAdapter.onClick")

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

        internal inner class VH(itemView: View, rowType: RowType) : RecyclerView.ViewHolder(itemView) {

            var nickName: TextView? = null
            var description: TextView? = null
            var userPhoto: ImageView? = null
            var buttonAdd: View? = null
            var buttonChat: View? = null
            var buttonInfo: View? = null
            var rootCardView: androidx.cardview.widget.CardView? = null
            var eventPhoto: ImageView? = null
            var imageContainer: View? = null
            var imageCardView: androidx.cardview.widget.CardView? = null
            var rootView: View? = null

            init {

                if (rowType != RowType.HEADER) {

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
                    eventPhoto = if (rowType != RowType.EMPTY)
                        itemView.findViewById(R.id.event_photo)
                    else null
                }
            }
        }

        internal var RestCallback: RestCallback = object : RestCallback() {

            override fun onEventResponse(requestID: Int, events: List<Pair<Event, Image>>?, responseStatus: Int) {
                super.onEventResponse(requestID, events, responseStatus)
                Log.d(TAG, "onEventResponse() called with: events = [$events], status = [$responseStatus]")
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

            override fun onFailure(requestID: Int, t: Throwable) {
                Log.d(TAG, "onFailure() called with: t = [$t]")
            }

        }
    }
}
