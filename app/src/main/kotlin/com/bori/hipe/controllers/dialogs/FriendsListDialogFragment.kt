package com.bori.hipe.controllers.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.bori.hipe.R
import com.bori.hipe.controllers.activities.CreateNewEventActivity
import com.bori.hipe.controllers.rest.callback.RestCallback
import com.bori.hipe.controllers.rest.callback.RestCallbackRepository
import com.bori.hipe.controllers.rest.service.UserService
import com.bori.hipe.models.Image
import com.bori.hipe.models.User
import com.google.android.material.internal.CheckableImageButton
import de.hdodenhof.circleimageview.CircleImageView

private const val GET_FRIENDS_LIST_ID = 14
private const val TAG = "FriendsLstDlgFrg"

class FriendsListDialogFragment : androidx.fragment.app.DialogFragment() {

    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var adapter: Adapter
    private var addedIds = hashSetOf<Int>()

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d(TAG, "FriendsListDialogFragment.onCreateDialog")

        val builder = AlertDialog.Builder(activity!!)
        val rootView = activity!!.layoutInflater.inflate(R.layout.dialog_friends_list, null, false)
        recyclerView = rootView.findViewById(R.id.dialog_fragment_friends_list)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        adapter = Adapter()
        recyclerView.adapter = adapter
        RestCallbackRepository.registerCallback(adapter.restCallback)

        builder.setCustomTitle(activity!!.layoutInflater.inflate(R.layout.add_friends_dialog_title, null, false))
        builder.setView(rootView)
        builder.setPositiveButton("OK") { _, _ ->
            this@FriendsListDialogFragment.dialog?.dismiss()
        }
        return builder.create()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "FriendsListDialogFragment.onDestroyView")

        RestCallbackRepository.unregisterCallback(adapter.restCallback)
    }

    private inner class Adapter internal constructor() : androidx.recyclerview.widget.RecyclerView.Adapter<Adapter.VH>(), View.OnClickListener {

        private val users = arrayListOf<Pair<User, Image>>()
        private val layoutInflater: LayoutInflater

        init {
            Log.d(TAG, "Adapter() called")

            addedIds = (activity as CreateNewEventActivity).addedUsers

            UserService.getFriends(GET_FRIENDS_LIST_ID, User().id)
            layoutInflater = activity!!.layoutInflater

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(layoutInflater.inflate(R.layout.item_user_add_row, parent, false))
        }

        @SuppressLint("RestrictedApi")
        override fun onBindViewHolder(holder: VH, position: Int) {
            val entry = users[position]

//            ImageLoader.getInstance().displayImage(entry.smallImage.url, holder.userPhoto)

            holder.checkableImageButton.isChecked = addedIds.contains(entry.first.id)

            holder.nickame.text = entry.first.username
            holder.rootView.tag = position
            holder.rootView.setOnClickListener(this)

        }

        override fun getItemCount() = users.size

        @SuppressLint("RestrictedApi")
        override fun onClick(view: View) {
            Log.d(TAG, "onClick() called with: animatedView = [$view]")

            if (view.tag != null && view.tag is Int) {
                val tag = view.tag as Int

                val userID = users[tag].first.id

                if (addedIds.contains(userID)) {

                    addedIds.remove(userID)
                    (view.findViewById<CheckableImageButton>(R.id.checkable_user_added)).isChecked = false

                } else {

                    addedIds.add(userID)
                    (view.findViewById<CheckableImageButton>(R.id.checkable_user_added)).isChecked = true

                }

            }

            if (view.tag != null && view.tag is Int) {

            }

        }

        val restCallback = object : RestCallback() {

            override fun onUserResponse(requestID: Int, users: List<Pair<User, Image>>?, responseStatus: Int) {
                Log.d(TAG, "onUserResponse() called with: users = [$users],  serverStatus = [$responseStatus]")
                users ?: return
                if (users.isNotEmpty()) {

                    Log.e(TAG, "onUserResponse:  users list not null ")

                    this@Adapter.users.addAll(users)
                    notifyDataSetChanged()

                }
            }

            override fun onFailure(requestID: Int, t: Throwable) {
                Log.e(TAG, "onFailure: ",t)
            }

        }

        internal inner class VH(val rootView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(rootView) {
            val userPhoto: CircleImageView = rootView.findViewById(R.id.user_photo)
            val nickame: TextView = rootView.findViewById(R.id.nickname)
            val checkableImageButton: CheckableImageButton = rootView.findViewById(R.id.checkable_user_added)
        }

    }

}