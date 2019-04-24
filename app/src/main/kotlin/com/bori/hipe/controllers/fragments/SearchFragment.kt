package com.bori.hipe.controllers.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bori.hipe.R
import com.bori.hipe.controllers.rest.callback.RestCallback
import com.bori.hipe.controllers.rest.callback.RestCallbackRepository
import com.bori.hipe.controllers.rest.service.UserService
import com.bori.hipe.models.Image
import com.bori.hipe.models.User
import com.bori.hipe.util.web.Status
import com.google.android.material.internal.CheckableImageButton
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.ImageScaleType
import kotlinx.android.synthetic.main.fragment_search.*
import java.util.*

private const val TAG = "SearchFragment.kt"
private const val GET_FRIENDS_LIST_ID = 16
private const val ADD_USER_TO_FRIENDS_ID = 17
private const val REMOVE_USER_FROM_FRIENDS_ID = 18
private const val FIND_USER_WITH_QUERY_ID = 19

private const val ANIMATiON_DURATION: Long = 100
private const val SEARCH_DELAY: Long = 200

private var prevQuery = ""
private val nickName: String? = null

class SearchFragment : androidx.fragment.app.Fragment() {

    private lateinit var v: View
    private lateinit var timer: Timer
    private lateinit var searchTask: TimerTask
    private lateinit var resultsAdapter: ResultsAdapter
    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        Log.d(TAG, "SearchFragment.onCreateView")

        val vb = View(context)
        vb.animate().alpha(0f).translationX(10f).rotation(10f).start()

        v = inflater.inflate(R.layout.fragment_search, container, false)

        resultsAdapter = ResultsAdapter()

        recyclerView = v.findViewById(R.id.result_list)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        recyclerView.adapter = resultsAdapter

        Log.e(TAG, "onCreateView: thisUserDoesNotExists")
        UserService.getFriendsIDs(
                requestID = GET_FRIENDS_LIST_ID,
                userID = User().id
        )

        searchTask = SearchTask()
        timer = Timer(false)
        timer.schedule(searchTask, SEARCH_DELAY, SEARCH_DELAY)
        RestCallbackRepository.registerCallback(RestCallback)

        return v

    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "SearchFragment.onDestroyView")

        RestCallbackRepository.unregisterCallback(RestCallback)
        searchTask.cancel()
        timer.cancel()
        timer.purge()
    }

    private inner class SearchTask : TimerTask() {

        override fun run() {
            Log.d(TAG, "SearchTask.run")

            try {
                activity!!.runOnUiThread {

                    val query = this@SearchFragment.search_query.text.toString()

                    if (query != prevQuery) {

                        Log.d(TAG, "Query : $query")

                        val delay = if (resultsAdapter.users.isEmpty()) 0L
                        else 1000L

                        recyclerView.animate().alpha(0f).setStartDelay(delay).duration = ANIMATiON_DURATION
                        progress_bar_container.animate().alpha(1f).setStartDelay(delay).duration = ANIMATiON_DURATION
                        UserService.find(
                                requestID = FIND_USER_WITH_QUERY_ID,
                                query = query
                        )

                        prevQuery = query

                    }
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

    }

    private var RestCallback: RestCallback = object : RestCallback() {

        override fun onFailure(requestID: Int, t: Throwable) {
            Log.d(TAG, "onFailure() called with: t = [$t]")
        }

        override fun onUserResponse(requestID: Int, users: List<Pair<User, Image>>?, responseStatus: Int) {
            Log.d(TAG, "onUserResponse() called with: users = [${users?.size}], status = [$responseStatus]")

            if (Status.isSuccessful(responseStatus)) {

                if (requestID == GET_FRIENDS_LIST_ID) {
                    Log.e(TAG, "onUserResponse: CONST_STATUS_FREINDS_LIST_ACCESSED")

                    val longs = users
                            ?.map { it.first.id }
                            ?.toSet() ?: emptySet()

                    resultsAdapter.friendsIds.clear()
                    resultsAdapter.friendsIds.addAll(longs)
                    resultsAdapter.notifyDataSetChanged()

                } else if (requestID == FIND_USER_WITH_QUERY_ID) {
                    Log.e(TAG, "onUserResponse: USERS_FOUND : ${users?.size}")

                    progress_bar_container.animate().alpha(0f).setStartDelay(0).duration = ANIMATiON_DURATION

                    if (users == null || users.isEmpty()) {
                        no_result_text_view.animate().alpha(1f).duration = ANIMATiON_DURATION
                        recyclerView.animate().alpha(0f).setStartDelay(0).duration = ANIMATiON_DURATION
                    } else {
                        no_result_text_view.animate().alpha(0f).duration = ANIMATiON_DURATION
                        recyclerView.animate().alpha(1f).setStartDelay(0).duration = ANIMATiON_DURATION
                    }

                    resultsAdapter.setUsers(users ?: emptyList())
                }
            }
        }

        override fun onSimpleResponse(requestID: Int, response: Any?, responseStatus: Int) {
            Log.e(TAG, "onSimpleResponse: $response")

            response ?: return
            val id = response as Int

            resultsAdapter.usersStack.remove(id)

            if (requestID == ADD_USER_TO_FRIENDS_ID && responseStatus == Status.OK)
                resultsAdapter.friendsIds.add(id)

            if (requestID == REMOVE_USER_FROM_FRIENDS_ID && responseStatus == Status.ACCEPTED) {
                Log.d(TAG, "User with id: $id deleted")
                resultsAdapter.friendsIds.remove(id)
            }

            resultsAdapter.notifyDataSetChanged()

        }

        override fun onIntCollectionResponse(requestID: Int, ids: Collection<Int>, responseStatus: Int) {
            Log.d(TAG, "onIntCollectionResponse requestID = [$requestID], ids = [$ids], serverStatus = [$responseStatus]")

            if (responseStatus == Status.OK) {

                Log.d(TAG, "onIntCollectionResponse: friends list is == " + ids)
                resultsAdapter.friendsIds.clear()
                resultsAdapter.friendsIds.addAll(ids)
                Log.e(TAG, "onIntCollectionResponse: now my list is == " + resultsAdapter.friendsIds)
                resultsAdapter.notifyDataSetChanged()

            }
        }
    }


    private inner class ResultsAdapter internal constructor() : androidx.recyclerview.widget.RecyclerView.Adapter<ResultsAdapter.VH>(), View.OnClickListener {

        val inflater: LayoutInflater = activity!!.layoutInflater
        val users = mutableListOf<Pair<User, Image>>()
        val friendsIds = arrayListOf<Int>()
        val usersStack = arrayListOf<Int>()
        val imageLoader = ImageLoader.getInstance()!!
        val displayImageOptions = DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED).build()!!


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(inflater.inflate(R.layout.item_user_add_to_friends_row, parent, false))
        }

        @SuppressLint("RestrictedApi")
        override fun onBindViewHolder(holder: VH, position: Int) {
            Log.d(TAG, "ResultsAdapter.onBindViewHolder")

            val entry = users[position]

            holder.nickName.text = entry.first.username
            holder.nameSurname.text = entry.first.name + " " + entry.first.surname
            holder.rootView.tag = entry.first.id
            imageLoader.displayImage(entry.second.urlSmall, holder.userPhoto, displayImageOptions)


            if (usersStack.contains(entry.first.id)) {
                holder.checkableImageButton.animate().alpha(0f).duration = ANIMATiON_DURATION
                holder.progressBar.animate().alpha(1f).duration = ANIMATiON_DURATION

            } else {
                holder.progressBar.animate().alpha(0f).duration = ANIMATiON_DURATION
                holder.checkableImageButton.animate().alpha(1f).duration = ANIMATiON_DURATION
                holder.checkableImageButton.isChecked = friendsIds.contains(entry.first.id)
                holder.checkableImageButton.tag = position
            }
        }

        override fun getItemCount(): Int = users.size

        fun setUsers(users: List<Pair<User, Image>>) {
            Log.d(TAG, "ResultsAdapter.setUsers")

            val mutableUsersIn = users.toMutableList()

            val iterator = this.users.iterator()

            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (!mutableUsersIn.contains(entry)) {
                    notifyItemRemoved(this.users.indexOf(entry))
                    iterator.remove()
                } else {
                    mutableUsersIn.remove(entry)
                }

            }

            this.users.addAll(mutableUsersIn)
            notifyItemRangeInserted(this.users.size - 1, mutableUsersIn.size)

        }

        override fun onClick(view: View) {
            Log.d(TAG, "ResultsAdapter.onClick")

            if (view.tag != null && view.tag is Int) {

                val tag = view.tag as Int

                if (view is CheckableImageButton) {

                    usersStack.add(users[tag].first.id)
                    notifyDataSetChanged()
                    Log.d(TAG, "onClick: before" + usersStack)

                    return
                }
            }
            if (view.tag != null && view.tag is Long) {

//                val id = view.tag as Long
//                val intent = Intent(activity, UserActivity::class.java)
//                intent.putExtra(Const.ADVANCED_USER_ID, id)
//                startActivity(intent)

            }

        }

        internal inner class VH(var rootView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(rootView) {

            var nickName: TextView
            var nameSurname: TextView
            var userPhoto: ImageView
            var checkableImageButton: CheckableImageButton
            var progressBar: ProgressBar

            init {
                rootView.setOnClickListener(this@ResultsAdapter)

                checkableImageButton = rootView.findViewById(R.id.checkable_user_add_to_friends)
                checkableImageButton.setOnClickListener(this@ResultsAdapter)
                progressBar = rootView.findViewById(R.id.add_user_progress_bar)
                nickName = rootView.findViewById(R.id.nickname)
                nameSurname = rootView.findViewById(R.id.name_surname)
                userPhoto = rootView.findViewById(R.id.user_photo)

            }
        }

    }

}