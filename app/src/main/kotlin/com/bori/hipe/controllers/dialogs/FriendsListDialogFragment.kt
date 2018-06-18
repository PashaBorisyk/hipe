package com.bori.hipe.controllers.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.CheckableImageButton
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bori.hipe.R
import com.bori.hipe.controllers.activities.CreateNewEventActivity
import com.bori.hipe.controllers.rest.RestService
import com.bori.hipe.controllers.rest.callbacks.RestCallbackAdapter
import com.bori.hipe.controllers.rest.service.UserService
import com.bori.hipe.models.HipeImage
import com.bori.hipe.models.Tuple
import com.bori.hipe.models.User
import de.hdodenhof.circleimageview.CircleImageView

private const val GET_FRIENDS_LIST_ID = 14L
private const val TAG = "FriendsLstDlgFrg"

class FriendsListDialogFragment : DialogFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: Adapter
    private var addedIds = hashSetOf<Long>()

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d(TAG, "onCreateDialog() called with: savedInstanceState = [$savedInstanceState]")

        val builder = AlertDialog.Builder(activity!!)
        val rootView = activity!!.layoutInflater.inflate(R.layout.dialog_friends_list, null, false)
        recyclerView = rootView.findViewById(R.id.dialog_fragment_friends_list)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = Adapter()
        recyclerView.adapter = adapter
        RestService.registerCallback(adapter.restCallback)

        builder.setCustomTitle(activity!!.layoutInflater.inflate(R.layout.add_friends_dialog_title, null, false))
        builder.setView(rootView)
        builder.setPositiveButton("OK") { _, _ ->
            this@FriendsListDialogFragment.dialog.dismiss()
        }
        return builder.create()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        RestService.unregisterCallback(adapter.restCallback)
    }

    private inner class Adapter internal constructor() : RecyclerView.Adapter<Adapter.VH>(), View.OnClickListener {

        private val users = arrayListOf<Tuple<User, HipeImage>>()
        private val layoutInflater: LayoutInflater

        init {
            Log.d(TAG, "Adapter() called")

            addedIds = (activity as CreateNewEventActivity).addedUsers

            UserService.getFriendsList(GET_FRIENDS_LIST_ID, User.thisUser.id)
            layoutInflater = activity!!.layoutInflater

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(layoutInflater.inflate(R.layout.item_user_add_row, parent, false))
        }

        @SuppressLint("RestrictedApi")
        override fun onBindViewHolder(holder: VH, position: Int) {
            val entry = users[position]

//            ImageLoader.getInstance().displayImage(entry.smallImage.url, holder.userPhoto)

            holder.checkableImageButton.isChecked = addedIds.contains(entry._1.id)

            holder.nickame.text = entry._1.username
            holder.rootView.tag = position
            holder.rootView.setOnClickListener(this)

        }

        override fun getItemCount() = users.size

        @SuppressLint("RestrictedApi")
        override fun onClick(view: View) {
            Log.d(TAG, "onClick() called with: animatedView = [$view]")

            if (view.tag != null && view.tag is Int) {
                val tag = view.tag as Int

                val userId = users[tag]._1.id

                if (addedIds.contains(userId)) {

                    addedIds.remove(userId)
                    (view.findViewById<CheckableImageButton>(R.id.checkable_user_added)).isChecked = false

                } else {

                    addedIds.add(userId)
                    (view.findViewById<CheckableImageButton>(R.id.checkable_user_added)).isChecked = true

                }

            }

            if (view.tag != null && view.tag is Int) {

            }

        }

        val restCallback = object : RestCallbackAdapter() {

            override fun onUserListResponse(requestID: Long, users: List<Tuple<User, HipeImage>>?, serverStatus: Int) {
                Log.d(TAG, "onUserListResponse() called with: users = [$users],  serverStatus = [$serverStatus]")
                users ?: return
                if (users.isNotEmpty()) {

                    Log.e(TAG, "onUserListResponse:  users list not null ")

                    this@Adapter.users.addAll(users)
                    notifyDataSetChanged()

                }
            }

            override fun onFailure(requestID: Long, t: Throwable) {
                Log.d(TAG, "onFailure: ")
            }

            override fun onOk(requestID: Long) {
                Log.d(TAG, "onOk: ")
            }
        }

        internal inner class VH(val rootView: View) : RecyclerView.ViewHolder(rootView) {
            val userPhoto: CircleImageView = rootView.findViewById(R.id.user_photo)
            val nickame: TextView = rootView.findViewById(R.id.nickname)
            val checkableImageButton: CheckableImageButton = rootView.findViewById(R.id.checkable_user_added)

        }

    }

}